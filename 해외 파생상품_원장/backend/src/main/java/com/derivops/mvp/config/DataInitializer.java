package com.derivops.mvp.config;

import com.derivops.mvp.account.Account;
import com.derivops.mvp.account.AccountRepository;
import com.derivops.mvp.account.AccountStatus;
import com.derivops.mvp.audit.AuditLog;
import com.derivops.mvp.audit.AuditLogRepository;
import com.derivops.mvp.batch.BatchRun;
import com.derivops.mvp.batch.BatchRunRepository;
import com.derivops.mvp.batch.BatchStatus;
import com.derivops.mvp.cashfx.CashRequest;
import com.derivops.mvp.cashfx.CashRequestRepository;
import com.derivops.mvp.cashfx.CashRequestType;
import com.derivops.mvp.cashfx.FxRequest;
import com.derivops.mvp.cashfx.FxRequestRepository;
import com.derivops.mvp.cashfx.RequestStatus;
import com.derivops.mvp.position.Balance;
import com.derivops.mvp.position.BalanceRepository;
import com.derivops.mvp.position.Margin;
import com.derivops.mvp.position.MarginRepository;
import com.derivops.mvp.position.Position;
import com.derivops.mvp.position.PositionRepository;
import com.derivops.mvp.user.UserAccount;
import com.derivops.mvp.user.UserAccountRepository;
import com.derivops.mvp.user.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
@Configuration
public class DataInitializer {

    private final UserAccountRepository userAccountRepository;
    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final PositionRepository positionRepository;
    private final MarginRepository marginRepository;
    private final BatchRunRepository batchRunRepository;
    private final CashRequestRepository cashRequestRepository;
    private final FxRequestRepository fxRequestRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            if (userAccountRepository.count() == 0) {
                createUser("opsadmin", "admin123!", UserRole.OPS_ADMIN);
                createUser("opsviewer", "viewer123!", UserRole.OPS_VIEWER);
                createUser("auditor", "audit123!", UserRole.AUDITOR);
            }

            if (accountRepository.count() == 0) {
                Account a1 = createAccount("CME-77889901", "CME", AccountStatus.ACTIVE, "Global Macro Desk");
                Account a2 = createAccount("EUX-55667788", "Eurex", AccountStatus.PENDING_CLOSE, "Options Desk");

                LocalDate today = LocalDate.now();
                seedBalance(a1, "USD", "2450000.2500", today);
                seedBalance(a1, "KRW", "85000000.0000", today);
                seedPosition(a1, "ESM6", "3.0000", "5198.250000", today);
                seedPosition(a1, "NQM6", "1.0000", "18220.500000", today);
                seedMargin(a1, "130000.0000", "90000.0000", "45000.0000", today);

                seedBalance(a2, "EUR", "420000.0000", today);
                seedPosition(a2, "FDAXM6", "2.0000", "18420.250000", today);
                seedMargin(a2, "40000.0000", "28000.0000", "15000.0000", today);
            }

            if (batchRunRepository.count() == 0) {
                seedBatch("EOD_SETTLEMENT", BatchStatus.SUCCESS, 0, null, 2);
                seedBatch("MARGIN_RECALC", BatchStatus.FAILED, 2, "Timeout while loading broker snapshot", 1);
                seedBatch("POSITION_SYNC", BatchStatus.RUNNING, 0, null, 0);
            }

            Account primaryAccount = accountRepository.findByAccountNo("CME-77889901").orElse(null);
            Account secondaryAccount = accountRepository.findByAccountNo("EUX-55667788").orElse(null);

            if (primaryAccount != null && secondaryAccount != null) {
                if (cashRequestRepository.count() == 0) {
                    seedCashRequest(primaryAccount, CashRequestType.DEPOSIT, "25000.0000", "USD", RequestStatus.PENDING,
                            "opsadmin", "Intraday liquidity top-up", null, null);
                    seedCashRequest(primaryAccount, CashRequestType.WITHDRAW, "5000.0000", "USD", RequestStatus.APPROVED,
                            "opsadmin", "End-of-day excess sweep", "opsadmin", "Approved after exposure check");
                    seedCashRequest(secondaryAccount, CashRequestType.DEPOSIT, "8000.0000", "EUR", RequestStatus.REJECTED,
                            "opsadmin", "Margin buffer increase", "opsadmin", "Rejected due to pending account close");
                }

                if (fxRequestRepository.count() == 0) {
                    seedFxRequest(primaryAccount, "USD", "KRW", "120000.0000", RequestStatus.PENDING,
                            "opsadmin", "KRW settlement funding", null, null);
                    seedFxRequest(primaryAccount, "EUR", "USD", "15000.0000", RequestStatus.APPROVED,
                            "opsadmin", "USD collateral conversion", "opsadmin", "Approved with standard spread");
                    seedFxRequest(secondaryAccount, "USD", "EUR", "9000.0000", RequestStatus.FAILED,
                            "opsadmin", "Collateral rebalance", "opsadmin", "Broker timeout after retry");
                }
            }

            if (auditLogRepository.count() == 0) {
                seedAuditLog("opsadmin", "LOGIN_SUCCESS", "AUTH", "opsadmin", "Successful login");
                seedAuditLog("opsadmin", "CREATE_CASH_REQUEST", "CASH_REQUEST", "-", "Intraday liquidity top-up");
                seedAuditLog("opsadmin", "APPROVE_FX_REQUEST", "FX_REQUEST", "-", "Approved with standard spread");
                seedAuditLog("opsadmin", "REJECT_CASH_REQUEST", "CASH_REQUEST", "-", "Rejected due to pending account close");
            }
        };
    }

    private void createUser(String username, String password, UserRole role) {
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        userAccountRepository.save(user);
    }

    private Account createAccount(String accountNo, String broker, AccountStatus status, String ownerName) {
        Account account = new Account();
        account.setAccountNo(accountNo);
        account.setBroker(broker);
        account.setStatus(status);
        account.setOwnerName(ownerName);
        account.setOpenedAt(LocalDate.now().minusYears(1));
        return accountRepository.save(account);
    }

    private void seedBalance(Account account, String currency, String amount, LocalDate tradingDate) {
        Balance b = new Balance();
        b.setAccount(account);
        b.setCurrency(currency);
        b.setAmount(new BigDecimal(amount));
        b.setTradingDate(tradingDate);
        balanceRepository.save(b);
    }

    private void seedPosition(Account account, String symbol, String quantity, String avgPrice, LocalDate tradingDate) {
        Position p = new Position();
        p.setAccount(account);
        p.setSymbol(symbol);
        p.setQuantity(new BigDecimal(quantity));
        p.setAvgPrice(new BigDecimal(avgPrice));
        p.setTradingDate(tradingDate);
        positionRepository.save(p);
    }

    private void seedMargin(Account account, String initial, String maintenance, String available, LocalDate tradingDate) {
        Margin margin = new Margin();
        margin.setAccount(account);
        margin.setInitialMargin(new BigDecimal(initial));
        margin.setMaintenanceMargin(new BigDecimal(maintenance));
        margin.setAvailableMargin(new BigDecimal(available));
        margin.setTradingDate(tradingDate);
        marginRepository.save(margin);
    }

    private void seedBatch(String name, BatchStatus status, int retryCount, String errorMessage, int hoursAgo) {
        BatchRun run = new BatchRun();
        run.setBatchName(name);
        run.setStatus(status);
        OffsetDateTime started = OffsetDateTime.now().minusHours(hoursAgo + 1);
        run.setStartedAt(started);
        run.setFinishedAt(status == BatchStatus.RUNNING ? null : started.plusMinutes(20));
        run.setRetryCount(retryCount);
        run.setErrorMessage(errorMessage);
        batchRunRepository.save(run);
    }

    private void seedCashRequest(
            Account account,
            CashRequestType type,
            String amount,
            String currency,
            RequestStatus status,
            String requestedBy,
            String reason,
            String reviewedBy,
            String reviewReason
    ) {
        CashRequest request = new CashRequest();
        request.setAccount(account);
        request.setType(type);
        request.setAmount(new BigDecimal(amount));
        request.setCurrency(currency);
        request.setStatus(status);
        request.setRequestedBy(requestedBy);
        request.setReason(reason);
        request.setReviewedBy(reviewedBy);
        request.setReviewReason(reviewReason);
        if (status != RequestStatus.PENDING) {
            request.setReviewedAt(OffsetDateTime.now().minusMinutes(30));
        }
        cashRequestRepository.save(request);
    }

    private void seedFxRequest(
            Account account,
            String fromCurrency,
            String toCurrency,
            String amount,
            RequestStatus status,
            String requestedBy,
            String reason,
            String reviewedBy,
            String reviewReason
    ) {
        FxRequest request = new FxRequest();
        request.setAccount(account);
        request.setFromCurrency(fromCurrency);
        request.setToCurrency(toCurrency);
        request.setAmount(new BigDecimal(amount));
        request.setStatus(status);
        request.setRequestedBy(requestedBy);
        request.setReason(reason);
        request.setReviewedBy(reviewedBy);
        request.setReviewReason(reviewReason);
        if (status != RequestStatus.PENDING) {
            request.setReviewedAt(OffsetDateTime.now().minusMinutes(20));
        }
        fxRequestRepository.save(request);
    }

    private void seedAuditLog(String actor, String action, String targetType, String targetId, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActor(actor);
        auditLog.setAction(action);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
    }
}
