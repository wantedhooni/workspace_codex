package com.derivops.mvp.audit.infrastructure;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactoryOptions;
import java.time.OffsetDateTime;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AuditLogR2dbcWriter {

    private static final Logger log = LoggerFactory.getLogger(AuditLogR2dbcWriter.class);

    private final ConnectionPool connectionPool;
    private final DatabaseClient databaseClient;

    public AuditLogR2dbcWriter(
            @Value("${app.audit.r2dbc.url}") String url,
            @Value("${app.audit.r2dbc.username:}") String username,
            @Value("${app.audit.r2dbc.password:}") String password
    ) {
        ConnectionFactoryOptions.Builder builder = ConnectionFactoryOptions.parse(url).mutate();
        if (StringUtils.hasText(username)) {
            builder.option(ConnectionFactoryOptions.USER, username);
        }
        if (StringUtils.hasText(password)) {
            builder.option(ConnectionFactoryOptions.PASSWORD, password);
        }

        this.connectionPool = new ConnectionPool(ConnectionPoolConfiguration.builder(ConnectionFactories.get(builder.build()))
                .initialSize(1)
                .maxSize(10)
                .build());
        this.databaseClient = DatabaseClient.create(connectionPool);
    }

    public void insert(String actor, String action, String targetType, String targetId, String details) {
        OffsetDateTime createdAt = OffsetDateTime.now();
        GenericExecuteSpec spec = databaseClient.sql("""
                insert into audit_logs (
                    actor,
                    action,
                    target_type,
                    target_id,
                    details,
                    created_at
                ) values (
                    :actor,
                    :action,
                    :targetType,
                    :targetId,
                    :details,
                    :createdAt
                )
                """)
                .bind("actor", actor)
                .bind("action", action)
                .bind("targetType", targetType)
                .bind("createdAt", createdAt);

        spec = bindNullable(spec, "targetId", targetId);
        spec = bindNullable(spec, "details", details);

        Long rowsUpdated = spec
                .fetch()
                .rowsUpdated()
                .onErrorMap(ex -> new IllegalStateException("Failed to write audit log via R2DBC", ex))
                .block();

        if (rowsUpdated == null || rowsUpdated != 1L) {
            throw new IllegalStateException("Unexpected audit log insert result: " + rowsUpdated);
        }

        log.debug("Audit log persisted via R2DBC: actor={}, action={}, targetType={}", actor, action, targetType);
    }

    private GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, String value) {
        if (value == null) {
            return spec.bindNull(name, String.class);
        }
        return spec.bind(name, value);
    }

    @PreDestroy
    void disposePool() {
        connectionPool.dispose();
    }
}
