package com.revy.mvpbanking.config;

import com.revy.mvpbanking.announcement.domain.Announcement;
import com.revy.mvpbanking.announcement.domain.AnnouncementAudience;
import com.revy.mvpbanking.announcement.domain.AnnouncementRepository;
import com.revy.mvpbanking.announcement.domain.AnnouncementSeverity;
import com.revy.mvpbanking.account.domain.Account;
import com.revy.mvpbanking.account.domain.AccountRepository;
import com.revy.mvpbanking.account.domain.AccountStatus;
import com.revy.mvpbanking.account.domain.AccountType;
import com.revy.mvpbanking.approval.domain.ApprovalRequest;
import com.revy.mvpbanking.approval.domain.ApprovalRequestRepository;
import com.revy.mvpbanking.approval.domain.ApprovalStatus;
import com.revy.mvpbanking.approval.domain.ApprovalTargetType;
import com.revy.mvpbanking.admin.domain.AdminRole;
import com.revy.mvpbanking.admin.domain.AdminUser;
import com.revy.mvpbanking.admin.domain.AdminUserRepository;
import com.revy.mvpbanking.customer.domain.Customer;
import com.revy.mvpbanking.customer.domain.CustomerRepository;
import com.revy.mvpbanking.customer.domain.CustomerStatus;
import com.revy.mvpbanking.exchange.domain.ExchangeRequest;
import com.revy.mvpbanking.exchange.domain.ExchangeRequestRepository;
import com.revy.mvpbanking.exchange.domain.ExchangeRequestStatus;
import com.revy.mvpbanking.fx.domain.FxRate;
import com.revy.mvpbanking.fx.domain.FxRateRepository;
import com.revy.mvpbanking.funding.application.FundingRequestService;
import com.revy.mvpbanking.funding.domain.FundingRequest;
import com.revy.mvpbanking.funding.domain.FundingRequestRepository;
import com.revy.mvpbanking.funding.domain.FundingRequestType;
import com.revy.mvpbanking.linkedaccount.domain.LinkedBankAccount;
import com.revy.mvpbanking.linkedaccount.domain.LinkedBankAccountRepository;
import com.revy.mvpbanking.notification.application.NotificationService;
import com.revy.mvpbanking.notification.domain.NotificationCategory;
import com.revy.mvpbanking.notification.domain.NotificationRecipientType;
import com.revy.mvpbanking.notification.domain.NotificationRepository;
import com.revy.mvpbanking.notification.domain.NotificationSeverity;
import com.revy.mvpbanking.stock.domain.StockOrder;
import com.revy.mvpbanking.stock.domain.StockOrderRepository;
import com.revy.mvpbanking.stock.domain.StockOrderSide;
import com.revy.mvpbanking.stock.domain.StockOrderStatus;
import com.revy.mvpbanking.stock.domain.StockPosition;
import com.revy.mvpbanking.stock.domain.StockPositionRepository;
import com.revy.mvpbanking.stock.domain.StockQuote;
import com.revy.mvpbanking.stock.domain.StockQuoteRepository;
import com.revy.mvpbanking.stock.application.StockOrderService;
import com.revy.mvpbanking.transaction.domain.TransactionEntry;
import com.revy.mvpbanking.transaction.domain.TransactionEntryRepository;
import com.revy.mvpbanking.transaction.domain.TransactionStatus;
import com.revy.mvpbanking.transaction.domain.TransactionType;
import com.revy.mvpbanking.user.domain.EndUser;
import com.revy.mvpbanking.user.domain.EndUserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InitialDataConfig {

    private static final String ADMIN_EMAIL = "admin@mvpbanking.local";
    private static final String ADMIN_PASSWORD = "Admin1234!";
    private static final String USER_EMAIL = "user@mvpbanking.local";
    private static final String USER_PASSWORD = "User1234!";
    private static final String USER_FULL_NAME = "MVP User";
    private static final String DEFAULT_CURRENCY = "KRW";
    private static final String DEFAULT_FOREIGN_CURRENCY = "USD";
    private static final int DEMO_CUSTOMER_COUNT = 100;

    @Bean
    CommandLineRunner seedInitialUsers(
            AdminUserRepository adminUserRepository,
            EndUserRepository endUserRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            ApprovalRequestRepository approvalRequestRepository,
            TransactionEntryRepository transactionEntryRepository,
            FxRateRepository fxRateRepository,
            FundingRequestRepository fundingRequestRepository,
            LinkedBankAccountRepository linkedBankAccountRepository,
            ExchangeRequestRepository exchangeRequestRepository,
            StockOrderRepository stockOrderRepository,
            StockPositionRepository stockPositionRepository,
            StockQuoteRepository stockQuoteRepository,
            NotificationRepository notificationRepository,
            AnnouncementRepository announcementRepository,
            NotificationService notificationService,
            FundingRequestService fundingRequestService,
            StockOrderService stockOrderService,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (adminUserRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
                adminUserRepository.save(
                        new AdminUser(
                                ADMIN_EMAIL,
                                passwordEncoder.encode(ADMIN_PASSWORD),
                                AdminRole.SUPER_ADMIN,
                                "Platform Admin",
                                true
                        )
                );
            }

            if (endUserRepository.findByEmail(USER_EMAIL).isEmpty()) {
                endUserRepository.save(
                        new EndUser(
                                USER_EMAIL,
                                passwordEncoder.encode(USER_PASSWORD),
                                USER_FULL_NAME,
                                true
                        )
                );
            }

            EndUser seededUser = endUserRepository.findByEmail(USER_EMAIL).orElseThrow();

            Customer primaryCustomer = customerRepository.findByEndUserId(seededUser.getId())
                    .orElseGet(() -> customerRepository.save(
                            new Customer(
                                    seededUser.getId(),
                                    customerNumber(1),
                                    USER_FULL_NAME,
                                    seededUser.getEmail(),
                                    CustomerStatus.ACTIVE
                            )
                    ));

            seedCustomerPortfolio(
                    primaryCustomer,
                    1,
                    accountRepository,
                    transactionEntryRepository,
                    approvalRequestRepository
            );

            for (int customerIndex = 2; customerIndex <= DEMO_CUSTOMER_COUNT; customerIndex++) {
                int index = customerIndex;
                Customer demoCustomer = customerRepository.findByCustomerNumber(customerNumber(index))
                        .orElseGet(() -> customerRepository.save(
                                new Customer(
                                        null,
                                        customerNumber(index),
                                        "Demo Customer " + String.format("%03d", index),
                                        "customer" + String.format("%03d", index) + "@mvpbanking.local",
                                        resolveCustomerStatus(index)
                                )
                        ));

                seedCustomerPortfolio(
                        demoCustomer,
                        index,
                        accountRepository,
                        transactionEntryRepository,
                        approvalRequestRepository
                );
            }

            seedFxRates(fxRateRepository);
            seedFundingDemoData(
                    primaryCustomer,
                    accountRepository,
                    fundingRequestRepository,
                    linkedBankAccountRepository,
                    fundingRequestService,
                    approvalRequestRepository
            );
            seedExchangeAndStockDemoData(
                    primaryCustomer,
                    accountRepository,
                    fxRateRepository,
                    exchangeRequestRepository,
                    stockOrderRepository,
                    stockPositionRepository,
                    stockQuoteRepository,
                    stockOrderService,
                    approvalRequestRepository
            );
            seedAnnouncements(announcementRepository);
            seedNotifications(
                    adminUserRepository.findByEmail(ADMIN_EMAIL).orElseThrow(),
                    seededUser,
                    notificationRepository,
                    notificationService
            );
        };
    }

    private static void seedCustomerPortfolio(
            Customer customer,
            int customerIndex,
            AccountRepository accountRepository,
            TransactionEntryRepository transactionEntryRepository,
            ApprovalRequestRepository approvalRequestRepository
    ) {
        Account bankingAccount = ensureAccount(
                accountRepository,
                customer.getId(),
                bankingAccountNumber(customerIndex),
                AccountType.BANKING,
                resolveBankingAccountStatus(customerIndex),
                balanceFor(customerIndex, 12_500_000L, 325_000L),
                DEFAULT_CURRENCY
        );

        seedTransactions(
                transactionEntryRepository,
                bankingAccount,
                customerIndex,
                List.of(TransactionType.DEPOSIT, TransactionType.WITHDRAWAL, TransactionType.DEPOSIT)
        );

        if (bankingAccount.getStatus() == AccountStatus.PENDING_APPROVAL) {
            ensureApproval(
                    approvalRequestRepository,
                    ApprovalTargetType.WITHDRAWAL,
                    bankingAccount.getId(),
                    "Withdrawal review for " + customer.getCustomerNumber(),
                    "Manual review required for " + bankingAccount.getAccountNumber(),
                    "ops.queue@mvpbanking.local"
            );
        }

        if (customerIndex % 2 == 0 || customerIndex == 1) {
            Account securitiesAccount = ensureAccount(
                    accountRepository,
                    customer.getId(),
                    securitiesAccountNumber(customerIndex),
                    AccountType.SECURITIES,
                    resolveSecuritiesAccountStatus(customerIndex),
                    balanceFor(customerIndex, 48_000_000L, 1_150_000L),
                    DEFAULT_CURRENCY
            );

            seedTransactions(
                    transactionEntryRepository,
                    securitiesAccount,
                    customerIndex,
                    List.of(TransactionType.BUY, TransactionType.SELL)
            );

            if (securitiesAccount.getStatus() == AccountStatus.PENDING_APPROVAL) {
                ensureApproval(
                        approvalRequestRepository,
                        ApprovalTargetType.ACCOUNT,
                        securitiesAccount.getId(),
                        "Account activation review for " + customer.getCustomerNumber(),
                        "Pending securities account activation for " + securitiesAccount.getAccountNumber(),
                        "ops.queue@mvpbanking.local"
                );
            }
        }

        if (shouldSeedForeignCurrencyBankingAccount(customerIndex)) {
            ensureAccount(
                    accountRepository,
                    customer.getId(),
                    foreignCurrencyBankingAccountNumber(customerIndex),
                    AccountType.BANKING,
                    AccountStatus.ACTIVE,
                    balanceFor(customerIndex, 18_000L, 175L),
                    DEFAULT_FOREIGN_CURRENCY
            );
        }
    }

    private static void seedTransactions(
            TransactionEntryRepository transactionEntryRepository,
            Account account,
            int customerIndex,
            List<TransactionType> transactionTypes
    ) {
        String accountPrefix = account.getAccountType() == AccountType.BANKING ? "BNK" : "SEC";
        for (int sequence = 0; sequence < transactionTypes.size(); sequence++) {
            int order = sequence + 1;
            TransactionType transactionType = transactionTypes.get(sequence);
            String transactionNumber = "TXN-" + accountPrefix + "-" + String.format("%03d", customerIndex) + "-" + String.format("%02d", order);
            transactionEntryRepository.findByTransactionNumber(transactionNumber)
                    .orElseGet(() -> transactionEntryRepository.save(
                            new TransactionEntry(
                                    account.getId(),
                                    transactionNumber,
                                    transactionType,
                                    resolveTransactionStatus(customerIndex, order),
                                    amountFor(customerIndex, order, account.getAccountType()),
                                    account.getCurrency(),
                                    Instant.now().minusSeconds((long) customerIndex * 3600L).minusSeconds((long) order * 900L)
                            )
                    ));
        }
    }

    private static Account ensureAccount(
            AccountRepository accountRepository,
            java.util.UUID customerId,
            String accountNumber,
            AccountType accountType,
            AccountStatus status,
            BigDecimal balance,
            String currency
    ) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseGet(() -> accountRepository.save(
                        new Account(customerId, accountNumber, accountType, status, balance, currency)
                ));
    }

    private static void ensureApproval(
            ApprovalRequestRepository approvalRequestRepository,
            ApprovalTargetType targetType,
            java.util.UUID targetId,
            String title,
            String description,
            String requestedByEmail
    ) {
        approvalRequestRepository.findByTitle(title)
                .orElseGet(() -> approvalRequestRepository.save(
                        new ApprovalRequest(
                                targetType,
                                targetId,
                                title,
                                description,
                                ApprovalStatus.PENDING,
                                requestedByEmail
                        )
                ));
    }

    private static void seedFxRates(FxRateRepository fxRateRepository) {
        ensureFxRate(fxRateRepository, "USD", "KRW", "1338.250000");
        ensureFxRate(fxRateRepository, "JPY", "KRW", "8.964000");
        ensureFxRate(fxRateRepository, "EUR", "KRW", "1451.820000");
        ensureFxRate(fxRateRepository, "KRW", "USD", "0.000747");
    }

    private static void ensureFxRate(FxRateRepository fxRateRepository, String baseCurrency, String quoteCurrency, String rate) {
        if (fxRateRepository.findTopByBaseCurrencyAndQuoteCurrencyOrderByEffectiveAtDesc(baseCurrency, quoteCurrency).isEmpty()) {
            fxRateRepository.save(
                    new FxRate(
                            baseCurrency,
                            quoteCurrency,
                            new BigDecimal(rate),
                            Instant.now().minusSeconds(600),
                            "MVP_MARKET_DATA"
                    )
            );
        }
    }

    private static void seedExchangeAndStockDemoData(
            Customer customer,
            AccountRepository accountRepository,
            FxRateRepository fxRateRepository,
            ExchangeRequestRepository exchangeRequestRepository,
            StockOrderRepository stockOrderRepository,
            StockPositionRepository stockPositionRepository,
            StockQuoteRepository stockQuoteRepository,
            StockOrderService stockOrderService,
            ApprovalRequestRepository approvalRequestRepository
    ) {
        List<Account> accounts = accountRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
        Account krwBankingAccount = accounts.stream()
                .filter(account -> account.getAccountType() == AccountType.BANKING)
                .filter(account -> DEFAULT_CURRENCY.equalsIgnoreCase(account.getCurrency()))
                .findFirst()
                .orElseThrow();
        Account usdBankingAccount = accounts.stream()
                .filter(account -> account.getAccountType() == AccountType.BANKING)
                .filter(account -> DEFAULT_FOREIGN_CURRENCY.equalsIgnoreCase(account.getCurrency()))
                .findFirst()
                .orElseThrow();
        Account securitiesAccount = accounts.stream().filter(account -> account.getAccountType() == AccountType.SECURITIES).findFirst().orElseThrow();

        FxRate usdKrw = fxRateRepository.findTopByBaseCurrencyAndQuoteCurrencyOrderByEffectiveAtDesc("USD", "KRW").orElseThrow();

        ExchangeRequest exchangeRequest = exchangeRequestRepository.findByRequestNumber("FX-DEMO-0001")
                .orElseGet(() -> exchangeRequestRepository.save(
                        new ExchangeRequest(
                                customer.getId(),
                                usdBankingAccount.getId(),
                                krwBankingAccount.getId(),
                                "FX-DEMO-0001",
                                "USD",
                                "KRW",
                                new BigDecimal("2500.0000"),
                                usdKrw.getRate(),
                                new BigDecimal("2500.0000").multiply(usdKrw.getRate()).setScale(4, RoundingMode.HALF_UP),
                                ExchangeRequestStatus.PENDING_APPROVAL
                        )
                ));
        exchangeRequest.synchronizeAccounts(usdBankingAccount.getId(), krwBankingAccount.getId());
        exchangeRequestRepository.save(exchangeRequest);

        ensureApproval(
                approvalRequestRepository,
                ApprovalTargetType.FX_EXCHANGE,
                exchangeRequest.getId(),
                "FX exchange " + exchangeRequest.getRequestNumber(),
                "USD to KRW exchange request",
                customer.getEmail()
        );

        StockOrder stockOrder = stockOrderRepository.findByOrderNumber("ORD-DEMO-0001")
                .orElseGet(() -> stockOrderRepository.save(
                        new StockOrder(
                                customer.getId(),
                                securitiesAccount.getId(),
                                "ORD-DEMO-0001",
                                "AAPL",
                                "NASDAQ",
                                StockOrderSide.BUY,
                                new BigDecimal("10.0000"),
                                new BigDecimal("182.5000"),
                                new BigDecimal("1825.0000"),
                                "USD",
                                StockOrderStatus.PENDING_APPROVAL
                        )
                ));

        ensureApproval(
                approvalRequestRepository,
                ApprovalTargetType.STOCK_ORDER,
                stockOrder.getId(),
                "Stock order " + stockOrder.getOrderNumber(),
                "BUY AAPL on NASDAQ",
                customer.getEmail()
        );

        stockPositionRepository.findByAccountIdAndSymbolIgnoreCase(securitiesAccount.getId(), "MSFT")
                .orElseGet(() -> stockPositionRepository.save(
                        new StockPosition(
                                customer.getId(),
                                securitiesAccount.getId(),
                                "MSFT",
                                "NASDAQ",
                                new BigDecimal("12.0000"),
                                new BigDecimal("318.2500"),
                                "USD"
                        )
                ));

        seedStockQuotes(stockQuoteRepository);
        seedHistoricalApprovedOrder(customer, securitiesAccount, stockOrderRepository, stockOrderService);
        seedHistoricalPartialFillOrder(customer, securitiesAccount, stockOrderRepository, stockOrderService);
    }

    private static void seedHistoricalApprovedOrder(
            Customer customer,
            Account securitiesAccount,
            StockOrderRepository stockOrderRepository,
            StockOrderService stockOrderService
    ) {
        StockOrder historicalOrder = stockOrderRepository.findByOrderNumber("ORD-DEMO-EXEC-0001")
                .orElseGet(() -> stockOrderRepository.save(
                        new StockOrder(
                                customer.getId(),
                                securitiesAccount.getId(),
                                "ORD-DEMO-EXEC-0001",
                                "TSLA",
                                "NASDAQ",
                                StockOrderSide.BUY,
                                new BigDecimal("6.0000"),
                                new BigDecimal("240.5000"),
                                new BigDecimal("1443.0000"),
                                "USD",
                                StockOrderStatus.PENDING_APPROVAL
                        )
                ));

        if (historicalOrder.getStatus() == StockOrderStatus.PENDING_APPROVAL) {
            stockOrderService.markApproved(historicalOrder.getId());
        }
    }

    private static void seedHistoricalPartialFillOrder(
            Customer customer,
            Account securitiesAccount,
            StockOrderRepository stockOrderRepository,
            StockOrderService stockOrderService
    ) {
        StockOrder partialOrder = stockOrderRepository.findByOrderNumber("ORD-DEMO-PARTIAL-0001")
                .orElseGet(() -> stockOrderRepository.save(
                        new StockOrder(
                                customer.getId(),
                                securitiesAccount.getId(),
                                "ORD-DEMO-PARTIAL-0001",
                                "AAPL",
                                "NASDAQ",
                                StockOrderSide.BUY,
                                new BigDecimal("10.0000"),
                                new BigDecimal("188.2000"),
                                new BigDecimal("1882.0000"),
                                "USD",
                                StockOrderStatus.PENDING_APPROVAL
                        )
                ));

        if (partialOrder.getStatus() == StockOrderStatus.PENDING_APPROVAL) {
            stockOrderService.markApproved(partialOrder.getId());
        }
    }

    private static void seedStockQuotes(StockQuoteRepository stockQuoteRepository) {
        ensureStockQuote(stockQuoteRepository, "AAPL", "NASDAQ", "189.4000", "USD", "0.012500");
        ensureStockQuote(stockQuoteRepository, "MSFT", "NASDAQ", "332.8000", "USD", "0.008200");
        ensureStockQuote(stockQuoteRepository, "TSLA", "NASDAQ", "241.7000", "USD", "-0.006500");
    }

    private static void seedNotifications(
            AdminUser adminUser,
            EndUser endUser,
            NotificationRepository notificationRepository,
            NotificationService notificationService
    ) {
        ensureNotification(
                notificationRepository,
                "ADMIN-SEED-QUEUE",
                NotificationRecipientType.ADMIN,
                adminUser.getId(),
                NotificationCategory.APPROVAL,
                NotificationSeverity.ACTION_REQUIRED,
                "승인 대기열 점검 필요",
                "승인 SLA 초과 항목과 부분 체결 주문을 확인하세요.",
                "/approvals",
                "APPROVAL_QUEUE",
                null
        );
        ensureNotification(
                notificationRepository,
                "ADMIN-SEED-MARKET",
                NotificationRecipientType.ADMIN,
                adminUser.getId(),
                NotificationCategory.SYSTEM,
                NotificationSeverity.WARNING,
                "시세 freshness 점검",
                "FX 또는 주식 시세 freshness가 기준 시간에 근접했습니다.",
                "/fx-rates",
                "MARKET_DATA",
                null
        );

        notificationService.notifyUser(
                "USER-SEED-PORTFOLIO:" + endUser.getId(),
                endUser.getId(),
                NotificationCategory.PORTFOLIO,
                NotificationSeverity.INFO,
                "포트폴리오 현황 안내",
                "통화 노출과 상위 보유 종목을 알림센터에서 함께 확인할 수 있습니다.",
                "/notifications",
                "DASHBOARD",
                null
        );
        notificationService.notifyUser(
                "USER-SEED-EXCHANGE:" + endUser.getId(),
                endUser.getId(),
                NotificationCategory.EXCHANGE,
                NotificationSeverity.ACTION_REQUIRED,
                "환전 요청 승인 대기",
                "FX-DEMO-0001 요청이 운영 승인 대기 중입니다.",
                "/exchange-requests",
                "EXCHANGE_REQUEST",
                null
        );
        notificationService.notifyUser(
                "USER-SEED-FUNDING:" + endUser.getId(),
                endUser.getId(),
                NotificationCategory.FUNDING,
                NotificationSeverity.INFO,
                "입출금 요청 사용 가능",
                "은행/증권 계좌별 입출금 요청을 새 티켓 화면에서 직접 등록할 수 있습니다.",
                "/funding-requests",
                "FUNDING_REQUEST",
                null
        );
        notificationService.notifyUser(
                "USER-SEED-LINKED-BANK:" + endUser.getId(),
                endUser.getId(),
                NotificationCategory.FUNDING,
                NotificationSeverity.INFO,
                "출금 연결 계좌 등록 가능",
                "외부 은행 연결 계좌를 등록하면 출금 요청 목적지를 직접 선택할 수 있습니다.",
                "/linked-bank-accounts",
                "LINKED_BANK_ACCOUNT",
                null
        );
    }

    private static void seedAnnouncements(AnnouncementRepository announcementRepository) {
        ensureAnnouncement(
                announcementRepository,
                "ANNOUNCEMENT-GLOBAL-MAINTENANCE",
                "해외주식 주문 점검 예정 안내",
                "이번 주말 해외주식 지정가 주문 점검이 예정되어 있습니다.",
                "2026년 3월 8일 02:00부터 05:00까지 해외주식 주문, 정정, 취소 기능이 순차 점검됩니다. 점검 시간에는 주문 체결 조회가 지연될 수 있습니다.",
                AnnouncementSeverity.WARNING,
                AnnouncementAudience.ALL,
                true,
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(172800),
                ADMIN_EMAIL,
                true
        );
        ensureAnnouncement(
                announcementRepository,
                "ANNOUNCEMENT-FX-CUTOFF",
                "환전 당일 정산 컷오프 시간 안내",
                "USD/KRW 환전은 영업일 16:00 이후 익영업일 정산될 수 있습니다.",
                "환전 신청 시각이 영업일 16:00 이후인 경우 정산 시간이 익영업일 오전으로 이월될 수 있습니다. 긴급 환전은 운영센터를 통해 별도 문의해 주세요.",
                AnnouncementSeverity.INFO,
                AnnouncementAudience.USER,
                false,
                Instant.now().minusSeconds(7200),
                Instant.now().plusSeconds(604800),
                ADMIN_EMAIL,
                true
        );
        ensureAnnouncement(
                announcementRepository,
                "ANNOUNCEMENT-ADMIN-OPS",
                "운영자 공지: 승인 큐 우선 검토",
                "부분체결 주문과 FX 승인 건을 우선 검토해 주세요.",
                "운영자용 공지입니다. 장 시작 전 부분체결 잔여 주문과 전일 16:00 이후 환전 요청을 우선 확인해 주세요.",
                AnnouncementSeverity.CRITICAL,
                AnnouncementAudience.ADMIN,
                true,
                Instant.now().minusSeconds(1800),
                Instant.now().plusSeconds(86400),
                ADMIN_EMAIL,
                true
        );
        ensureAnnouncement(
                announcementRepository,
                "ANNOUNCEMENT-DRAFT-POLICY",
                "보안 정책 변경 사전 공지",
                "2단계 인증 옵션 제공을 위한 사전 공지 초안입니다.",
                "다음 배포에서 2단계 인증과 로그인 알림 정책이 추가될 예정입니다. 아직 게시되지 않은 초안입니다.",
                AnnouncementSeverity.INFO,
                AnnouncementAudience.ALL,
                false,
                Instant.now().plusSeconds(86400),
                Instant.now().plusSeconds(259200),
                ADMIN_EMAIL,
                false
        );
    }

    private static void ensureNotification(
            NotificationRepository notificationRepository,
            String notificationKey,
            NotificationRecipientType recipientType,
            java.util.UUID recipientId,
            NotificationCategory category,
            NotificationSeverity severity,
            String title,
            String message,
            String actionPath,
            String referenceType,
            java.util.UUID referenceId
    ) {
        notificationRepository.findByNotificationKey(notificationKey)
                .orElseGet(() -> notificationRepository.save(
                        new com.revy.mvpbanking.notification.domain.Notification(
                                notificationKey,
                                recipientType,
                                recipientId,
                                category,
                                severity,
                                title,
                                message,
                                actionPath,
                                referenceType,
                                referenceId
                        )
                ));
    }

    private static void ensureAnnouncement(
            AnnouncementRepository announcementRepository,
            String announcementKey,
            String title,
            String summary,
            String body,
            AnnouncementSeverity severity,
            AnnouncementAudience audience,
            boolean pinned,
            Instant startsAt,
            Instant endsAt,
            String createdByEmail,
            boolean publish
    ) {
        Announcement announcement = announcementRepository.findByAnnouncementKey(announcementKey)
                .orElseGet(() -> announcementRepository.save(
                        new Announcement(
                                announcementKey,
                                title,
                                summary,
                                body,
                                severity,
                                audience,
                                pinned,
                                startsAt,
                                endsAt,
                                createdByEmail
                        )
                ));
        if (publish && announcement.getStatus() != com.revy.mvpbanking.announcement.domain.AnnouncementStatus.PUBLISHED) {
            announcement.publish(Instant.now().minusSeconds(600));
            announcementRepository.save(announcement);
        }
    }

    private static void seedFundingDemoData(
            Customer customer,
            AccountRepository accountRepository,
            FundingRequestRepository fundingRequestRepository,
            LinkedBankAccountRepository linkedBankAccountRepository,
            FundingRequestService fundingRequestService,
            ApprovalRequestRepository approvalRequestRepository
    ) {
        Instant seedNow = Instant.now();
        List<Account> accounts = accountRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
        Account krwBankingAccount = accounts.stream()
                .filter(account -> account.getAccountType() == AccountType.BANKING)
                .filter(account -> DEFAULT_CURRENCY.equalsIgnoreCase(account.getCurrency()))
                .findFirst()
                .orElseThrow();
        Account securitiesAccount = accounts.stream()
                .filter(account -> account.getAccountType() == AccountType.SECURITIES)
                .findFirst()
                .orElseThrow();

        LinkedBankAccount primaryWithdrawalAccount = ensureLinkedBankAccount(
                linkedBankAccountRepository,
                customer,
                "Shinhan Bank",
                "급여 출금 계좌",
                USER_FULL_NAME,
                "110-999-123456",
                true
        );
        ensureLinkedBankAccount(
                linkedBankAccountRepository,
                customer,
                "KB Kookmin Bank",
                "예비 생활비 계좌",
                USER_FULL_NAME,
                "004-555-987654",
                false
        );
        ensurePendingLinkedBankAccount(
                linkedBankAccountRepository,
                customer,
                "Toss Bank",
                "신규 출금 계좌",
                USER_FULL_NAME,
                "100-321-654987",
                false,
                "MVP-2468"
        );

        FundingRequest pendingWithdrawal = fundingRequestRepository.findByRequestNumber("FND-DEMO-0001")
                .orElseGet(() -> fundingRequestRepository.save(
                        new FundingRequest(
                                customer.getId(),
                                customer.getEmail(),
                                krwBankingAccount.getId(),
                                krwBankingAccount.getAccountNumber(),
                                krwBankingAccount.getAccountType().name(),
                                "FND-DEMO-0001",
                                FundingRequestType.WITHDRAWAL,
                                new BigDecimal("1250000.0000"),
                                krwBankingAccount.getCurrency(),
                                krwBankingAccount.getBalance(),
                                new BigDecimal("1000.0000"),
                                false,
                                new BigDecimal("0.0000"),
                                new BigDecimal("1251000.0000"),
                                primaryWithdrawalAccount.getId(),
                                primaryWithdrawalAccount.getBankName(),
                                primaryWithdrawalAccount.getAccountAlias(),
                                com.revy.mvpbanking.common.support.MaskingUtils.maskAccountNumber(primaryWithdrawalAccount.getAccountNumber()),
                                primaryWithdrawalAccount.getAccountHolderName(),
                                new BigDecimal("3000000.0000"),
                                new BigDecimal("1250000.0000"),
                                false,
                                true,
                                seedNow.plusSeconds(6 * 60 * 60),
                                true,
                                "고액 출금 심사",
                                "월간 운영비 출금 요청"
                        )
                ));

        ensureApproval(
                approvalRequestRepository,
                ApprovalTargetType.FUNDING_REQUEST,
                pendingWithdrawal.getId(),
                pendingWithdrawal.getRequestType().name() + " funding " + pendingWithdrawal.getRequestNumber(),
                pendingWithdrawal.getRequestType().name() + " " + pendingWithdrawal.getAmount().toPlainString() + " " + pendingWithdrawal.getCurrency(),
                customer.getEmail()
        );

        FundingRequest approvedDeposit = fundingRequestRepository.findByRequestNumber("FND-DEMO-0002")
                .orElseGet(() -> fundingRequestRepository.save(
                        new FundingRequest(
                                customer.getId(),
                                customer.getEmail(),
                                securitiesAccount.getId(),
                                securitiesAccount.getAccountNumber(),
                                securitiesAccount.getAccountType().name(),
                                "FND-DEMO-0002",
                                FundingRequestType.DEPOSIT,
                                new BigDecimal("3500000.0000"),
                                securitiesAccount.getCurrency(),
                                securitiesAccount.getBalance(),
                                new BigDecimal("0.0000"),
                                false,
                                new BigDecimal("0.0000"),
                                new BigDecimal("3500000.0000"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                new BigDecimal("20000000.0000"),
                                new BigDecimal("3500000.0000"),
                                false,
                                true,
                                seedNow.plusSeconds(4 * 60 * 60),
                                false,
                                null,
                                "해외주식 추가 매수용 예탁금 입금"
                        )
                ));

        if (approvedDeposit.getStatus() == com.revy.mvpbanking.funding.domain.FundingRequestStatus.PENDING_APPROVAL) {
            fundingRequestService.markApproved(approvedDeposit.getId());
        }
    }

    private static LinkedBankAccount ensureLinkedBankAccount(
            LinkedBankAccountRepository linkedBankAccountRepository,
            Customer customer,
            String bankName,
            String accountAlias,
            String accountHolderName,
            String accountNumber,
            boolean primaryWithdrawal
    ) {
        return linkedBankAccountRepository.findByCustomerIdAndAccountNumber(customer.getId(), accountNumber)
                .orElseGet(() -> linkedBankAccountRepository.save(
                        new LinkedBankAccount(
                                customer.getId(),
                                customer.getEmail(),
                                bankName,
                                accountAlias,
                                accountHolderName,
                                accountNumber,
                                primaryWithdrawal
                        )
                ));
    }

    private static LinkedBankAccount ensurePendingLinkedBankAccount(
            LinkedBankAccountRepository linkedBankAccountRepository,
            Customer customer,
            String bankName,
            String accountAlias,
            String accountHolderName,
            String accountNumber,
            boolean primaryWithdrawal,
            String verificationReference
    ) {
        return linkedBankAccountRepository.findByCustomerIdAndAccountNumber(customer.getId(), accountNumber)
                .orElseGet(() -> linkedBankAccountRepository.save(
                        LinkedBankAccount.pendingVerification(
                                customer.getId(),
                                customer.getEmail(),
                                bankName,
                                accountAlias,
                                accountHolderName,
                                accountNumber,
                                primaryWithdrawal,
                                verificationReference
                        )
                ));
    }

    private static void ensureStockQuote(
            StockQuoteRepository stockQuoteRepository,
            String symbol,
            String market,
            String price,
            String currency,
            String changeRate
    ) {
        if (stockQuoteRepository.findTopBySymbolIgnoreCaseAndMarketIgnoreCaseOrderByEffectiveAtDesc(symbol, market).isEmpty()) {
            stockQuoteRepository.save(
                    new StockQuote(
                            symbol,
                            market,
                            new BigDecimal(price),
                            currency,
                            new BigDecimal(changeRate),
                            Instant.now().minusSeconds(300),
                            "MVP_MARKET_DATA"
                    )
            );
        }
    }

    private static String customerNumber(int customerIndex) {
        return "CUST-" + (100000 + customerIndex);
    }

    private static String bankingAccountNumber(int customerIndex) {
        return "110-" + String.format("%03d", customerIndex) + "-" + String.format("%06d", customerIndex * 13);
    }

    private static String foreignCurrencyBankingAccountNumber(int customerIndex) {
        return "120-" + String.format("%03d", customerIndex) + "-" + String.format("%06d", customerIndex * 17);
    }

    private static String securitiesAccountNumber(int customerIndex) {
        return "800-" + String.format("%03d", customerIndex) + "-" + String.format("%06d", customerIndex * 29);
    }

    private static boolean shouldSeedForeignCurrencyBankingAccount(int customerIndex) {
        return customerIndex == 1 || customerIndex % 5 == 0;
    }

    private static CustomerStatus resolveCustomerStatus(int customerIndex) {
        if (customerIndex % 17 == 0) {
            return CustomerStatus.SUSPENDED;
        }
        if (customerIndex % 8 == 0) {
            return CustomerStatus.REVIEW_REQUIRED;
        }
        return CustomerStatus.ACTIVE;
    }

    private static AccountStatus resolveBankingAccountStatus(int customerIndex) {
        if (customerIndex % 10 == 0) {
            return AccountStatus.PENDING_APPROVAL;
        }
        if (customerIndex % 21 == 0) {
            return AccountStatus.LOCKED;
        }
        return AccountStatus.ACTIVE;
    }

    private static AccountStatus resolveSecuritiesAccountStatus(int customerIndex) {
        if (customerIndex % 12 == 0) {
            return AccountStatus.PENDING_APPROVAL;
        }
        if (customerIndex % 18 == 0) {
            return AccountStatus.LOCKED;
        }
        return AccountStatus.ACTIVE;
    }

    private static TransactionStatus resolveTransactionStatus(int customerIndex, int order) {
        if (order == 2 && customerIndex % 13 == 0) {
            return TransactionStatus.REJECTED;
        }
        if (order == 3 && customerIndex % 7 == 0) {
            return TransactionStatus.PENDING;
        }
        return TransactionStatus.COMPLETED;
    }

    private static BigDecimal balanceFor(int customerIndex, long baseAmount, long increment) {
        return BigDecimal.valueOf(baseAmount + increment * customerIndex).setScale(4);
    }

    private static BigDecimal amountFor(int customerIndex, int order, AccountType accountType) {
        long baseAmount = accountType == AccountType.BANKING ? 350_000L : 1_250_000L;
        return BigDecimal.valueOf(baseAmount + (long) customerIndex * 25_000L + (long) order * 10_000L).setScale(4);
    }
}
