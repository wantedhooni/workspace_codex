package com.quant.portal.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.quant.portal.api.infrastructure.jpa.repository.PortfolioRepository;
import com.quant.portal.api.presentation.dto.portfolio.PortfolioCreateRequest;
import com.quant.portal.api.presentation.dto.portfolio.PortfolioUpdateRequest;
import com.quant.portal.domain.portfolio.entity.Portfolio;
import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditIntegrationTest {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPopulateCreatedAndUpdatedAuditFieldsFromAuthenticatedUser() {
        authenticate("audit-user");
        Portfolio created = portfolioService.create(new PortfolioCreateRequest("Audit Portfolio", CurrencyCode.KRW));

        entityManager.flush();
        entityManager.clear();

        Portfolio createdReloaded = portfolioRepository.findById(created.getId()).orElseThrow();
        assertThat(createdReloaded.getCreatedBy()).isEqualTo("audit-user");
        assertThat(createdReloaded.getUpdatedBy()).isEqualTo("audit-user");
        assertThat(createdReloaded.getCreatedAt()).isNotNull();
        assertThat(createdReloaded.getUpdatedAt()).isNotNull();

        authenticate("audit-admin");
        portfolioService.update(createdReloaded.getId(), new PortfolioUpdateRequest("Audit Portfolio Updated"));

        entityManager.flush();
        entityManager.clear();

        Portfolio updatedReloaded = portfolioRepository.findById(createdReloaded.getId()).orElseThrow();
        assertThat(updatedReloaded.getName()).isEqualTo("Audit Portfolio Updated");
        assertThat(updatedReloaded.getCreatedBy()).isEqualTo("audit-user");
        assertThat(updatedReloaded.getUpdatedBy()).isEqualTo("audit-admin");
        assertThat(updatedReloaded.getUpdatedAt()).isAfterOrEqualTo(updatedReloaded.getCreatedAt());
    }

    @Test
    void shouldPopulateAuditFieldsWithSystemOnBatchThreadName() {
        SecurityContextHolder.clearContext();

        String originalThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName("batch-audit-test");
        try {
            Portfolio created = portfolioService.create(new PortfolioCreateRequest("Batch Audit Portfolio", CurrencyCode.KRW));

            entityManager.flush();
            entityManager.clear();

            Portfolio createdReloaded = portfolioRepository.findById(created.getId()).orElseThrow();
            assertThat(createdReloaded.getCreatedBy()).isEqualTo("system");
            assertThat(createdReloaded.getUpdatedBy()).isEqualTo("system");
            assertThat(createdReloaded.getCreatedAt()).isNotNull();
            assertThat(createdReloaded.getUpdatedAt()).isNotNull();
        } finally {
            Thread.currentThread().setName(originalThreadName);
        }
    }

    @Test
    void shouldPopulateAuditFieldsWithAnonymousWithoutAuthentication() {
        SecurityContextHolder.clearContext();

        String originalThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName("audit-test-worker");
        try {
            Portfolio created = portfolioService.create(new PortfolioCreateRequest("Anonymous Audit Portfolio", CurrencyCode.KRW));

            entityManager.flush();
            entityManager.clear();

            Portfolio createdReloaded = portfolioRepository.findById(created.getId()).orElseThrow();
            assertThat(createdReloaded.getCreatedBy()).isEqualTo("anonymous");
            assertThat(createdReloaded.getUpdatedBy()).isEqualTo("anonymous");
            assertThat(createdReloaded.getCreatedAt()).isNotNull();
            assertThat(createdReloaded.getUpdatedAt()).isNotNull();
        } finally {
            Thread.currentThread().setName(originalThreadName);
        }
    }

    private static void authenticate(String username) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username,
                "secret",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
