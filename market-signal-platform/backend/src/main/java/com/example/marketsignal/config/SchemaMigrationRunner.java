package com.example.marketsignal.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 기존 데이터베이스 스키마를 최신 도메인 구조에 맞게 보정한다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 리포트 테이블의 누락 컬럼을 보정하고 기존 데이터를 안전하게 채운다.
     */
    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("alter table reports add column if not exists snapshot_date date");
        jdbcTemplate.execute("""
                update reports report
                set snapshot_date = coalesce(
                    (
                        select max(signal.snapshot_date)
                        from signals signal
                        where signal.report_id = report.id
                    ),
                    report.report_date
                )
                where report.snapshot_date is null
                """);
        jdbcTemplate.execute("alter table reports alter column snapshot_date set not null");
    }
}
