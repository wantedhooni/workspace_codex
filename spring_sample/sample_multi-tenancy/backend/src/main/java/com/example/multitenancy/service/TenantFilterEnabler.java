package com.example.multitenancy.service;

import com.example.multitenancy.security.TenantContext;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 현재 요청의 테넌트 식별자로 Hibernate 필터를 활성화한다.
 */
@Component
public class TenantFilterEnabler {

    private final EntityManager entityManager;

    public TenantFilterEnabler(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void enableCurrentTenantFilter() {
        String tenantId = TenantContext.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            return;
        }

        Session session = entityManager.unwrap(Session.class);
        if (session.getEnabledFilter("tenantFilter") == null) {
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
            return;
        }
        session.getEnabledFilter("tenantFilter").setParameter("tenantId", tenantId);
    }
}
