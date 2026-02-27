package com.quant.portal.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quant.portal.api.application.exception.ApiException;
import com.quant.portal.api.application.service.command.RegisterTransactionCommand;
import com.quant.portal.api.infrastructure.jpa.repository.HoldingRepository;
import com.quant.portal.api.infrastructure.jpa.repository.InstrumentRepository;
import com.quant.portal.api.infrastructure.jpa.repository.PortfolioRepository;
import com.quant.portal.api.infrastructure.jpa.repository.PortfolioTransactionRepository;
import com.quant.portal.api.presentation.dto.transaction.TransactionUpdateRequest;
import com.quant.portal.domain.portfolio.entity.Holding;
import com.quant.portal.domain.portfolio.entity.Instrument;
import com.quant.portal.domain.portfolio.entity.Portfolio;
import com.quant.portal.domain.portfolio.entity.PortfolioTransaction;
import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import com.quant.portal.domain.portfolio.enums.MarketCode;
import com.quant.portal.domain.portfolio.enums.TransactionType;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
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
class TransactionCommandServiceIntegrationTest {

    @Autowired
    private TransactionCommandService transactionCommandService;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private PortfolioTransactionRepository portfolioTransactionRepository;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCalculateWeightedAverageAndRealizedPnl() {
        Portfolio portfolio = portfolioRepository.save(new Portfolio("Core", CurrencyCode.KRW));
        Instrument instrument = instrumentRepository.save(new Instrument("AAPLX", "Apple", MarketCode.US, CurrencyCode.USD));

        transactionCommandService.register(new RegisterTransactionCommand(
                portfolio.getId(),
                null,
                TransactionType.DEPOSIT,
                LocalDate.of(2026, 2, 11),
                null,
                null,
                new BigDecimal("20000"),
                null,
                null,
                CurrencyCode.KRW,
                "initial deposit"
        ));

        transactionCommandService.register(new RegisterTransactionCommand(
                portfolio.getId(),
                instrument.getId(),
                TransactionType.BUY,
                LocalDate.of(2026, 2, 11),
                new BigDecimal("10"),
                new BigDecimal("1000"),
                null,
                new BigDecimal("10"),
                BigDecimal.ZERO,
                CurrencyCode.KRW,
                "buy aapl"
        ));

        PortfolioTransaction sellTransaction = transactionCommandService.register(new RegisterTransactionCommand(
                portfolio.getId(),
                instrument.getId(),
                TransactionType.SELL,
                LocalDate.of(2026, 2, 12),
                new BigDecimal("4"),
                new BigDecimal("1200"),
                null,
                new BigDecimal("10"),
                BigDecimal.ZERO,
                CurrencyCode.KRW,
                "partial sell"
        ));

        Portfolio reloadedPortfolio = portfolioRepository.findById(portfolio.getId()).orElseThrow();
        Holding holding = holdingRepository.findByPortfolioIdAndInstrumentId(portfolio.getId(), instrument.getId()).orElseThrow();

        assertThat(reloadedPortfolio.getCashBalance()).isEqualByComparingTo("14780.0000");
        assertThat(holding.getQuantity()).isEqualByComparingTo("6.00000000");
        assertThat(holding.getAverageCost()).isEqualByComparingTo("1001.0000");
        assertThat(sellTransaction.getRealizedPnl()).isEqualByComparingTo("786.0000");
    }

    @Test
    void shouldRejectWithdrawalWhenCashWouldBecomeNegative() {
        Portfolio portfolio = portfolioRepository.save(new Portfolio("Core", CurrencyCode.KRW));

        assertThatThrownBy(() -> transactionCommandService.register(new RegisterTransactionCommand(
                portfolio.getId(),
                null,
                TransactionType.WITHDRAW,
                LocalDate.of(2026, 2, 11),
                null,
                null,
                new BigDecimal("1"),
                null,
                null,
                CurrencyCode.KRW,
                "invalid withdraw"
        )))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Cash balance cannot be negative");
    }

    @Test
    void shouldPopulateTransactionAuditFieldsOnCreateAndUpdate() {
        authenticate("audit-user");

        Portfolio portfolio = portfolioRepository.save(new Portfolio("Core Audit", CurrencyCode.KRW));
        Instrument instrument = instrumentRepository.save(new Instrument("NVDA", "NVIDIA", MarketCode.US, CurrencyCode.USD));

        transactionCommandService.register(new RegisterTransactionCommand(
                portfolio.getId(),
                null,
                TransactionType.DEPOSIT,
                LocalDate.of(2026, 2, 11),
                null,
                null,
                new BigDecimal("20000"),
                null,
                null,
                CurrencyCode.KRW,
                "seed"
        ));

        PortfolioTransaction buyTransaction = transactionCommandService.register(new RegisterTransactionCommand(
                portfolio.getId(),
                instrument.getId(),
                TransactionType.BUY,
                LocalDate.of(2026, 2, 11),
                new BigDecimal("3"),
                new BigDecimal("1000"),
                null,
                new BigDecimal("10"),
                BigDecimal.ZERO,
                CurrencyCode.KRW,
                "initial buy"
        ));

        entityManager.flush();
        entityManager.clear();

        PortfolioTransaction createdReloaded = portfolioTransactionRepository.findById(buyTransaction.getId()).orElseThrow();
        assertThat(createdReloaded.getCreatedBy()).isEqualTo("audit-user");
        assertThat(createdReloaded.getUpdatedBy()).isEqualTo("audit-user");
        assertThat(createdReloaded.getCreatedAt()).isNotNull();
        assertThat(createdReloaded.getUpdatedAt()).isNotNull();

        authenticate("audit-admin");
        transactionCommandService.update(
                buyTransaction.getId(),
                new TransactionUpdateRequest(
                        instrument.getId(),
                        LocalDate.of(2026, 2, 11),
                        new BigDecimal("2"),
                        new BigDecimal("950"),
                        null,
                        new BigDecimal("10"),
                        BigDecimal.ZERO,
                        CurrencyCode.KRW,
                        "updated buy"
                )
        );

        entityManager.flush();
        entityManager.clear();

        PortfolioTransaction updatedReloaded = portfolioTransactionRepository.findById(buyTransaction.getId()).orElseThrow();
        assertThat(updatedReloaded.getCreatedBy()).isEqualTo("audit-user");
        assertThat(updatedReloaded.getUpdatedBy()).isEqualTo("audit-admin");
        assertThat(updatedReloaded.getUpdatedAt()).isAfterOrEqualTo(updatedReloaded.getCreatedAt());
    }

    @Test
    void shouldRejectNegativeFeeOnRegister() {
        Portfolio portfolio = portfolioRepository.save(new Portfolio("Core", CurrencyCode.KRW));

        assertThatThrownBy(() -> transactionCommandService.register(new RegisterTransactionCommand(
                portfolio.getId(),
                null,
                TransactionType.DEPOSIT,
                LocalDate.of(2026, 2, 11),
                null,
                null,
                new BigDecimal("1000"),
                new BigDecimal("-1"),
                BigDecimal.ZERO,
                CurrencyCode.KRW,
                "invalid negative fee"
        )))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("fee must be greater than or equal to zero")
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;
                    assertThat(apiException.getDetails()).isEqualTo(
                            java.util.Map.of("fee", "fee must be greater than or equal to zero")
                    );
                });
    }

    @Test
    void shouldRejectNegativeTaxOnUpdate() {
        Portfolio portfolio = portfolioRepository.save(new Portfolio("Core", CurrencyCode.KRW));

        PortfolioTransaction depositTransaction = transactionCommandService.register(new RegisterTransactionCommand(
                portfolio.getId(),
                null,
                TransactionType.DEPOSIT,
                LocalDate.of(2026, 2, 11),
                null,
                null,
                new BigDecimal("20000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                CurrencyCode.KRW,
                "seed"
        ));

        assertThatThrownBy(() -> transactionCommandService.update(
                depositTransaction.getId(),
                new TransactionUpdateRequest(
                        null,
                        LocalDate.of(2026, 2, 11),
                        null,
                        null,
                        new BigDecimal("20000"),
                        BigDecimal.ZERO,
                        new BigDecimal("-1"),
                        CurrencyCode.KRW,
                        "invalid negative tax"
                )
        ))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("tax must be greater than or equal to zero")
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;
                    assertThat(apiException.getDetails()).isEqualTo(
                            java.util.Map.of("tax", "tax must be greater than or equal to zero")
                    );
                });
    }

    @Test
    void shouldRejectAmountFieldForBuyRegister() {
        Portfolio portfolio = portfolioRepository.save(new Portfolio("Core", CurrencyCode.KRW));
        Instrument instrument = instrumentRepository.save(new Instrument("AAPLZ", "Apple", MarketCode.US, CurrencyCode.USD));

        assertThatThrownBy(() -> transactionCommandService.register(new RegisterTransactionCommand(
                portfolio.getId(),
                instrument.getId(),
                TransactionType.BUY,
                LocalDate.of(2026, 2, 11),
                new BigDecimal("1"),
                new BigDecimal("1000"),
                new BigDecimal("1000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                CurrencyCode.KRW,
                "invalid buy payload"
        )))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("amount must be null for transaction type BUY");
    }

    @Test
    void shouldRejectInstrumentFieldForDepositUpdate() {
        Portfolio portfolio = portfolioRepository.save(new Portfolio("Core", CurrencyCode.KRW));

        PortfolioTransaction depositTransaction = transactionCommandService.register(new RegisterTransactionCommand(
                portfolio.getId(),
                null,
                TransactionType.DEPOSIT,
                LocalDate.of(2026, 2, 11),
                null,
                null,
                new BigDecimal("5000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                CurrencyCode.KRW,
                "seed"
        ));

        assertThatThrownBy(() -> transactionCommandService.update(
                depositTransaction.getId(),
                new TransactionUpdateRequest(
                        999L,
                        LocalDate.of(2026, 2, 11),
                        null,
                        null,
                        new BigDecimal("5000"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        CurrencyCode.KRW,
                        "invalid deposit payload"
                )
        ))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("instrumentId must be null for transaction type DEPOSIT");
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
