package com.derivops.mvp.config;

import com.derivops.mvp.account.Account;
import com.derivops.mvp.account.infrastructure.AccountRepository;
import com.derivops.mvp.account.AccountStatus;
import com.derivops.mvp.approval.ApprovalDomain;
import com.derivops.mvp.approval.ApprovalPolicy;
import com.derivops.mvp.approval.infrastructure.ApprovalPolicyRepository;
import com.derivops.mvp.audit.AuditLog;
import com.derivops.mvp.audit.infrastructure.AuditLogRepository;
import com.derivops.mvp.batch.BatchRun;
import com.derivops.mvp.batch.infrastructure.BatchRunRepository;
import com.derivops.mvp.batch.BatchStatus;
import com.derivops.mvp.cashfx.CashRequest;
import com.derivops.mvp.cashfx.infrastructure.CashRequestRepository;
import com.derivops.mvp.cashfx.CashRequestType;
import com.derivops.mvp.cashfx.FxRequest;
import com.derivops.mvp.cashfx.infrastructure.FxRequestRepository;
import com.derivops.mvp.cashfx.RequestPriority;
import com.derivops.mvp.cashfx.RequestStatus;
import com.derivops.mvp.domainterm.DomainTerm;
import com.derivops.mvp.domainterm.infrastructure.DomainTermRepository;
import com.derivops.mvp.exchangerate.ExchangeRate;
import com.derivops.mvp.exchangerate.infrastructure.ExchangeRateRepository;
import com.derivops.mvp.journalentry.JournalEntry;
import com.derivops.mvp.ledger.LedgerEntry;
import com.derivops.mvp.menu.MenuEntry;
import com.derivops.mvp.menu.infrastructure.MenuEntryRepository;
import com.derivops.mvp.opscase.OpsCase;
import com.derivops.mvp.opscase.OpsCaseCategory;
import com.derivops.mvp.opscase.infrastructure.OpsCaseRepository;
import com.derivops.mvp.opscase.OpsCaseSeverity;
import com.derivops.mvp.opscase.OpsCaseStatus;
import com.derivops.mvp.position.Balance;
import com.derivops.mvp.position.infrastructure.BalanceRepository;
import com.derivops.mvp.position.Margin;
import com.derivops.mvp.position.infrastructure.MarginRepository;
import com.derivops.mvp.position.Position;
import com.derivops.mvp.position.infrastructure.PositionRepository;
import com.derivops.mvp.risk.RiskLimitPolicy;
import com.derivops.mvp.risk.infrastructure.RiskLimitPolicyRepository;
import com.derivops.mvp.stockpurchase.application.StockPurchaseService;
import com.derivops.mvp.stockpurchase.StockPurchase;
import com.derivops.mvp.stockpurchase.dto.CreateStockPurchaseRequest;
import com.derivops.mvp.stockpurchase.infrastructure.StockPurchaseRepository;
import com.derivops.mvp.stockposition.StockPosition;
import com.derivops.mvp.user.UserAccount;
import com.derivops.mvp.user.infrastructure.UserAccountRepository;
import com.derivops.mvp.user.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final MenuEntryRepository menuEntryRepository;
    private final DomainTermRepository domainTermRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final OpsCaseRepository opsCaseRepository;
    private final ApprovalPolicyRepository approvalPolicyRepository;
    private final RiskLimitPolicyRepository riskLimitPolicyRepository;
    private final StockPurchaseRepository stockPurchaseRepository;
    private final StockPurchaseService stockPurchaseService;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            ensureEnversSchema();
            patchLegacySchema();

            ensureDemoUser("opsadmin", "admin123!", UserRole.OPS_ADMIN);
            ensureDemoUser("opsadmin2", "admin234!", UserRole.OPS_ADMIN);
            ensureDemoUser("opsviewer", "viewer123!", UserRole.OPS_VIEWER);
            ensureDemoUser("auditor", "audit123!", UserRole.AUDITOR);

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

            ensureApprovalPolicy("GLOBAL", ApprovalDomain.CASH_DEPOSIT, "250000", "1000000", "300000", true, true, "Global default deposit policy");
            ensureApprovalPolicy("GLOBAL", ApprovalDomain.CASH_WITHDRAW, "250000", "1000000", "200000", true, true, "Global default withdrawal policy");
            ensureApprovalPolicy("GLOBAL", ApprovalDomain.FX, "250000", "1000000", "300000", true, true, "Global default FX policy");
            ensureApprovalPolicy("CME", ApprovalDomain.CASH_WITHDRAW, "200000", "900000", "180000", true, true, "CME withdraw stricter threshold");
            ensureApprovalPolicy("CME", ApprovalDomain.FX, "220000", "850000", "250000", true, true, "CME FX KRW sensitivity");
            ensureApprovalPolicy("EUREX", ApprovalDomain.FX, "200000", "800000", "250000", true, true, "EUREX FX conversion policy");

            ensureRiskLimitPolicy("GLOBAL", ApprovalDomain.CASH_DEPOSIT, null, "1500000", "3000000", "5000000", true, "Global deposit risk limit");
            ensureRiskLimitPolicy("GLOBAL", ApprovalDomain.CASH_WITHDRAW, null, "1200000", "2500000", "4000000", true, "Global withdraw risk limit");
            ensureRiskLimitPolicy("GLOBAL", ApprovalDomain.FX, null, "1500000", "3000000", "5000000", true, "Global FX risk limit");
            ensureRiskLimitPolicy("CME", ApprovalDomain.CASH_WITHDRAW, "USD", "1000000", "2000000", "3200000", true, "CME USD withdraw control");
            ensureRiskLimitPolicy("CME", ApprovalDomain.FX, "USD", "1300000", "2500000", "4200000", true, "CME USD FX limit");
            ensureRiskLimitPolicy("CME", ApprovalDomain.FX, "KRW", "900000", "1800000", "3000000", true, "CME KRW leg stricter control");

            ensureExchangeRate("USD", "KRW", LocalDate.now().minusDays(1), "1325.40000000", "OPS_DEMO");
            ensureExchangeRate("USD", "KRW", LocalDate.now(), "1328.45000000", "OPS_DEMO");
            ensureExchangeRate("EUR", "USD", LocalDate.now().minusDays(1), "1.08150000", "OPS_DEMO");
            ensureExchangeRate("EUR", "USD", LocalDate.now(), "1.08450000", "OPS_DEMO");
            ensureExchangeRate("USD", "JPY", LocalDate.now(), "149.25000000", "OPS_DEMO");
            ensureExchangeRate("USD", "SGD", LocalDate.now(), "1.35240000", "OPS_DEMO");

            ensureMenu("dashboard", "Dashboard", "운영 개요 도메인. 계좌, 요청, 배치, 통제 현황을 한눈에 보는 시작 화면입니다.", "/", null, "dashboard", "dashboard", 10, true, "OPS_ADMIN,OPS_VIEWER,AUDITOR");
            ensureMenu("reference-root", "Reference", "공통 기준 정보 묶음입니다. 용어집과 환율처럼 여러 업무가 함께 참조하는 데이터를 모아둡니다.", "", null, null, "menu_book", 20, true, "OPS_ADMIN,OPS_VIEWER,AUDITOR");
            ensureMenu("domain-terms", "Domain Terms", "도메인 용어집 화면. 업무 용어를 한글 설명과 예시로 조회하는 공통 사전입니다.", "/domain-terms", "reference-root", "domain-terms", "menu_book", 10, true, "OPS_ADMIN,OPS_VIEWER,AUDITOR");
            ensureMenu("exchange-rates", "Exchange Rates", "환율 도메인. 통화쌍별 기준 환율과 환전 예상 금액을 조회하고 관리합니다.", "/exchange-rates", "reference-root", "exchange-rates", "query_stats", 20, true, "OPS_ADMIN,OPS_VIEWER,AUDITOR");
            ensureMenu("operations-root", "Operations", "실행성 업무 메뉴 묶음입니다. 계좌, 증권, 자금, 배치 운영 기능을 2 depth 구조로 제공합니다.", "", null, null, "dashboard", 30, true, "OPS_ADMIN,OPS_VIEWER,AUDITOR");
            ensureMenu("accounts", "Accounts", "계좌 도메인. 해외 브로커 계좌와 잔고, 포지션, 증거금 현황을 조회합니다.", "/accounts", "operations-root", "accounts", "account_balance", 10, true, "OPS_ADMIN,OPS_VIEWER,AUDITOR");
            ensureMenu("portfolio", "Portfolio", "포트폴리오 도메인. 계좌별 현금 잔고와 주식 보유, 최근 매수 내역을 한 화면에서 확인합니다.", "/portfolio", "operations-root", "portfolio", "pie_chart", 20, true, "OPS_ADMIN,OPS_VIEWER,AUDITOR");
            ensureMenu("stock-purchases", "Stock Purchases", "증권 거래 도메인. 주식 매수 등록과 체결 이력을 조회합니다.", "/stock-purchases", "operations-root", "stock-purchases", "candlestick_chart", 30, true, "OPS_ADMIN,OPS_VIEWER,AUDITOR");
            ensureMenu("stock-positions", "Stock Positions", "증권 포지션 도메인. 종목별 현재 보유 수량, 평균단가, 총원가를 조회합니다.", "/stock-positions", "operations-root", "stock-positions", "inventory_2", 40, true, "OPS_ADMIN,OPS_VIEWER,AUDITOR");
            ensureMenu("stock-recommendations", "Stock Recommendations", "AI 추천 도메인. 포트폴리오와 운용 조건을 기준으로 종목 추천 초안을 생성합니다.", "/stock-recommendations", "operations-root", "stock-recommendations", "auto_awesome", 50, true, "OPS_ADMIN,OPS_VIEWER");
            ensureMenu("cash-requests", "Cash Requests", "자금 도메인. 입금과 출금 요청을 등록하고 승인 상태와 통제 결과를 확인합니다.", "/cash-requests", "operations-root", "cash-requests", "payments", 60, true, "OPS_ADMIN,OPS_VIEWER,AUDITOR");
            ensureMenu("fx-requests", "FX Requests", "환전 도메인. 통화 전환 요청과 브로커 처리 상태를 관리합니다.", "/fx-requests", "operations-root", "fx-requests", "currency_exchange", 70, true, "OPS_ADMIN,OPS_VIEWER,AUDITOR");
            ensureMenu("batches", "Batches", "배치 도메인. 포지션 동기화, 증거금 재계산, EOD 정산 작업의 실행 상태를 모니터링합니다.", "/batches", "operations-root", "batches", "schedule", 80, true, "OPS_ADMIN,OPS_VIEWER");
            ensureMenu("control-root", "Control", "감사, 정책, 운영 예외 같은 내부통제 업무 메뉴 묶음입니다.", "", null, null, "manage_search", 40, true, "OPS_ADMIN,OPS_VIEWER,AUDITOR");
            ensureMenu("audit-logs", "Audit Logs", "감사 도메인. 로그인, 요청 처리, 정책 변경 같은 주요 행위를 추적합니다.", "/audit-logs", "control-root", "audit-logs", "manage_search", 10, true, "OPS_ADMIN,AUDITOR");
            ensureMenu("ops-cases", "Ops Cases", "운영 예외 도메인. 장애, 실패, 통제 위반 케이스를 등록하고 추적합니다.", "/ops-cases", "control-root", "ops-cases", "incident", 20, true, "OPS_ADMIN,OPS_VIEWER,AUDITOR");
            ensureMenu("approval-policies", "Approval Policies", "통제 정책 도메인. 요청 승인 임계치와 수동심사 기준을 설정합니다.", "/approval-policies", "control-root", "approval-policies", "policy", 30, true, "OPS_ADMIN,AUDITOR");
            ensureMenu("risk-limits", "Risk Limits", "리스크 도메인. 단건 요청 한도와 일중 누적 노출 한도를 관리합니다.", "/risk-limits", "control-root", "risk-limits", "risk", 40, true, "OPS_ADMIN,AUDITOR");
            ensureMenu("menu-admin", "Menus", "메뉴 관리 도메인. 역할별 메뉴 노출과 화면 진입 구성을 관리합니다.", "/menus", "control-root", "menus", "menu", 50, true, "OPS_ADMIN,AUDITOR");
            cleanupLegacyMenus("account-ops-root", "funding-root", "settlement-root", "audit-root", "policy-root");
            ensureDomainTerm("ACCOUNT", "계좌/잔고", 10, "ACCOUNT", "Account", "계좌", "해외 브로커에 개설된 거래 계좌 마스터입니다. 잔고, 포지션, 증거금, 요청의 기준 단위가 됩니다.", "예: CME-77889901 계좌에서 현금요청과 주식매수가 발생합니다.", 10);
            ensureDomainTerm("ACCOUNT", "계좌/잔고", 10, "BALANCE", "Balance", "잔고", "계좌와 통화 기준 현재 가용 자금 또는 예치 금액 스냅샷입니다.", "예: USD 2,450,000.2500 보유", 20);
            ensureDomainTerm("ACCOUNT", "계좌/잔고", 10, "POSITION", "Position", "포지션", "선물/옵션 등 파생상품의 종목별 보유 수량과 평균 단가 스냅샷입니다.", "예: ESM6 3계약 보유", 30);
            ensureDomainTerm("ACCOUNT", "계좌/잔고", 10, "MARGIN", "Margin", "증거금", "거래 유지를 위해 필요한 초기/유지/가용 증거금 정보입니다.", "예: 유지증거금 부족 시 추가 자금 투입이 필요합니다.", 40);
            ensureDomainTerm("ACCOUNT", "계좌/잔고", 10, "ACCOUNT_STATUS", "Account Status", "계좌 상태", "계좌의 운영 가능 상태입니다. ACTIVE, SUSPENDED, PENDING_CLOSE 같은 상태로 운영 통제와 요청 가능 여부를 구분합니다.", "예: PENDING_CLOSE 계좌는 신규 출금 승인 시 추가 확인이 필요합니다.", 50);
            ensureDomainTerm("ACCOUNT", "계좌/잔고", 10, "AVAILABLE_CASH", "Available Cash", "가용 현금", "실제 요청이나 증거금 충당에 바로 사용할 수 있는 현금 잔액입니다. 총잔고와 다를 수 있습니다.", "예: 총 USD 잔고는 충분해도 미결제 거래 때문에 가용 현금은 더 적을 수 있습니다.", 60);
            ensureDomainTerm("FUNDING", "입출금/환전", 20, "CASH_REQUEST", "Cash Request", "입출금 요청", "계좌로 입금하거나 계좌에서 출금하기 위한 운영 요청입니다. 승인정책과 리스크한도를 함께 적용합니다.", "예: 장중 유동성 확보를 위해 USD 입금 요청 생성", 10);
            ensureDomainTerm("FUNDING", "입출금/환전", 20, "FX_REQUEST", "FX Request", "환전 요청", "한 통화를 다른 통화로 전환하기 위한 요청입니다. 브로커 송신 결과에 따라 승인 또는 실패가 기록됩니다.", "예: USD를 KRW로 전환해 원화 정산 재원 확보", 20);
            ensureDomainTerm("FUNDING", "입출금/환전", 20, "FOUR_EYES", "4-eyes Control", "4-eyes 통제", "요청자와 승인자를 분리해 동일 사용자의 자기승인을 막는 내부통제 규칙입니다.", "예: opsadmin이 만든 요청은 opsadmin2가 승인해야 합니다.", 30);
            ensureDomainTerm("FUNDING", "입출금/환전", 20, "VALUE_DATE", "Value Date", "가치일", "자금이 실제로 효력을 가지는 날짜입니다. 거래일과 다를 수 있으며 정산과 회계 반영 시점 판단에 사용합니다.", "예: 금요일에 요청한 환전이 다음 영업일 가치일로 반영될 수 있습니다.", 40);
            ensureDomainTerm("FUNDING", "입출금/환전", 20, "EXCHANGE_RATE", "Exchange Rate", "환율", "한 통화를 다른 통화로 환산할 때 적용하는 기준 비율입니다. 환전 예상 금액과 요청 검토의 기준이 됩니다.", "예: USD/KRW 1328.45 기준으로 10,000 USD는 약 13,284,500 KRW", 50);
            ensureDomainTerm("FUNDING", "입출금/환전", 20, "FX_QUOTE", "FX Quote", "환전 예상금액", "기준 환율을 적용해 현재 요청 금액이 상대 통화로 얼마인지 계산한 값입니다. 실제 체결과는 차이가 날 수 있습니다.", "예: 15,000 EUR를 USD로 환산한 예상 수취금액", 60);
            ensureDomainTerm("TRADING", "주문/체결/정산", 25, "ORDER", "Order", "주문", "거래소나 브로커에 송신하는 매수/매도 지시입니다. 수량, 가격, 계좌, 상품 정보가 포함됩니다.", "예: CME 계좌로 E-mini S&P 선물 3계약 신규 매수 주문", 10);
            ensureDomainTerm("TRADING", "주문/체결/정산", 25, "EXECUTION", "Execution", "체결", "주문이 시장에서 실제로 성사된 결과입니다. 체결 단가와 체결 수량이 포지션과 정산의 기준이 됩니다.", "예: 3계약 주문 중 1계약만 먼저 부분 체결될 수 있습니다.", 20);
            ensureDomainTerm("TRADING", "주문/체결/정산", 25, "SETTLEMENT", "Settlement", "정산", "거래 결과를 현금, 포지션, 회계에 반영하는 후속 처리입니다. 일일정산과 최종결제 관점이 모두 포함됩니다.", "예: EOD 정산 시 손익과 증거금이 다시 계산됩니다.", 30);
            ensureDomainTerm("TRADING", "주문/체결/정산", 25, "PRODUCT", "Product", "상품", "거래소에 상장된 선물, 옵션, 주식 같은 거래 대상입니다. 상품 특성에 따라 정산, 증거금, 거래시간이 달라집니다.", "예: CME의 Micro Nasdaq 선물은 별도 상품 규격을 가집니다.", 40);
            ensureDomainTerm("TRADING", "주문/체결/정산", 25, "CONTRACT", "Contract", "종목/계약", "만기와 세부 규격이 확정된 실제 거래 단위입니다. 같은 상품 안에서도 월물별로 다른 계약이 존재합니다.", "예: ESM6, NQM6는 서로 다른 선물 계약입니다.", 50);
            ensureDomainTerm("TRADING", "주문/체결/정산", 25, "TRADING_DAY", "Trading Day", "거래일", "거래소 달력 기준으로 거래를 인식하는 일자입니다. 국내 영업일과 다를 수 있어 배치 기준일 산정에 중요합니다.", "예: 미국 시장 휴일이면 한국 기준 평일이어도 거래일이 아닐 수 있습니다.", 60);
            ensureDomainTerm("TRADING", "주문/체결/정산", 25, "MARK_TO_MARKET", "Mark to Market", "일일정산평가", "파생상품을 당일 정산가격으로 재평가해 손익과 증거금을 반영하는 절차입니다.", "예: 선물 포지션은 장 종료 후 MTM 손익이 현금에 반영됩니다.", 70);
            ensureDomainTerm("STOCK", "주식/회계", 30, "STOCK_PURCHASE", "Stock Purchase", "주식 매수", "주식 매수 거래 원본 이벤트입니다. 거래일, 결제일, 수량, 단가, 수수료를 저장합니다.", "예: AAPL 25주 매수", 10);
            ensureDomainTerm("STOCK", "주식/회계", 30, "STOCK_POSITION", "Stock Position", "주식 포지션", "계좌와 종목 기준 현재 보유 수량, 평균단가, 총원가를 집계한 데이터입니다.", "예: AAPL 누적 매수 후 평균단가 재계산", 20);
            ensureDomainTerm("STOCK", "주식/회계", 30, "LEDGER_ENTRY", "Ledger Entry", "원장 엔트리", "거래별 수량 증감과 누적 수량/금액을 기록하는 운영 원장입니다.", "예: 매수 1건당 원장 1건 생성", 30);
            ensureDomainTerm("STOCK", "주식/회계", 30, "JOURNAL_ENTRY", "Journal Entry", "분개", "회계 차변/대변 라인입니다. 한 거래에 여러 라인이 묶여 생성될 수 있습니다.", "예: 재고자산 차변, 수수료비용 차변, 현금 대변", 40);
            ensureDomainTerm("STOCK", "주식/회계", 30, "PORTFOLIO", "Portfolio", "포트폴리오", "계좌 기준 현금 잔고, 주식 보유, 최근 거래를 함께 보는 증권 자산 집계 화면 또는 관점입니다.", "예: 계좌별 USD 현금과 AAPL/NVDA 보유를 함께 조회", 50);
            ensureDomainTerm("STOCK", "주식/회계", 30, "BOOK_COST", "Book Cost", "장부원가", "주식 포지션이 현재 원장 기준으로 보유하고 있는 총 취득원가입니다.", "예: AAPL 총원가 6,000 USD", 60);
            ensureDomainTerm("STOCK", "주식/회계", 30, "AVERAGE_PRICE", "Average Price", "평균단가", "누적 매수 또는 체결 결과를 기준으로 계산한 보유 단가입니다. 포지션 평가와 매도 손익 산정의 기초 값입니다.", "예: AAPL 2회 매수 후 평균단가 182.92 USD", 70);
            ensureDomainTerm("STOCK", "주식/회계", 30, "AI_STOCK_RECOMMENDATION", "AI Stock Recommendation", "AI 종목 추천", "포트폴리오 보유 현황과 운용 조건을 입력해 AI가 종목 후보, 액션, 주의사항을 초안 형태로 제시하는 기능입니다.", "예: 계좌별 현금 여력과 기존 AAPL/NVDA 보유를 보고 분산 후보를 제안", 80);
            ensureDomainTerm("REFERENCE", "거래소/브로커", 35, "EXCHANGE", "Exchange", "거래소", "상품이 상장되고 거래 규정이 정의되는 시장 운영 주체입니다. 거래시간과 정산 규칙의 기준이 됩니다.", "예: CME, Eurex, SGX는 서로 다른 거래소입니다.", 10);
            ensureDomainTerm("REFERENCE", "거래소/브로커", 35, "BROKER", "Broker", "브로커", "주문 집행, 계좌 유지, 자금 이체를 연결하는 외부 중개기관입니다.", "예: 브로커 응답 지연이 발생하면 주문/환전 상태가 보류될 수 있습니다.", 20);
            ensureDomainTerm("REFERENCE", "거래소/브로커", 35, "BROKER_INTERFACE", "Broker Interface", "브로커 연계", "브로커와 주문, 잔고, 환전, 파일을 주고받는 시스템 인터페이스입니다. 장애 시 수동 대응이 필요할 수 있습니다.", "예: Position Sync 배치는 브로커 포지션 파일을 읽어 내부 데이터와 비교합니다.", 30);
            ensureDomainTerm("REFERENCE", "거래소/브로커", 35, "EXCHANGE_RULE", "Exchange Rule", "거래소 규정", "상품 상장, 만기, 거래시간, 정산 방식, 증거금 조건을 정의하는 외부 규정입니다.", "예: 신규 상품 상장 시 시스템 상품 마스터와 배치 규칙을 함께 반영합니다.", 40);
            ensureDomainTerm("CONTROL", "통제/운영", 40, "APPROVAL_POLICY", "Approval Policy", "승인정책", "브로커와 요청 도메인별 승인 임계치, 수동심사 기준을 관리하는 정책입니다.", "예: 대규모 출금은 자동으로 HIGH 우선순위 부여", 10);
            ensureDomainTerm("CONTROL", "통제/운영", 40, "RISK_LIMIT", "Risk Limit", "리스크 한도", "단건 또는 일중 누적 요청 한도를 정의하는 통제 정책입니다.", "예: CME USD 출금은 단건 1,000,000 한도", 20);
            ensureDomainTerm("CONTROL", "통제/운영", 40, "OPS_CASE", "Ops Case", "운영 케이스", "장애, 통제 위반, 연계 실패 후속조치를 추적하는 운영 예외 관리 단위입니다.", "예: 브로커 응답 실패 후 REQUEST_FAILURE 케이스 생성", 30);
            ensureDomainTerm("CONTROL", "통제/운영", 40, "BATCH_RUN", "Batch Run", "배치 실행", "정산, 포지션 동기화, 증거금 재계산 등 배치 작업의 실행 결과입니다.", "예: EOD_SETTLEMENT 성공, MARGIN_RECALC 실패", 40);
            ensureDomainTerm("CONTROL", "통제/운영", 40, "AUDIT_LOG", "Audit Log", "감사 로그", "사용자 로그인, 승인, 정책 변경 같은 주요 행위를 남기는 추적 기록입니다. 내부감사와 감독기관 대응의 기초 자료입니다.", "예: 누가 어떤 출금 요청을 언제 승인했는지 추적합니다.", 50);
            ensureDomainTerm("CONTROL", "통제/운영", 40, "REGULATORY_REPORT", "Regulatory Report", "대외 보고", "감독기관이나 내부 통제 부서에 제출하는 정기 또는 수시 보고 자료입니다.", "예: 대규모 자금이동 내역을 감사 대응용으로 정리합니다.", 60);

            if (opsCaseRepository.count() == 0) {
                seedOpsCase(
                        "OPS-DEMO-001",
                        OpsCaseCategory.REQUEST_FAILURE,
                        OpsCaseSeverity.HIGH,
                        OpsCaseStatus.OPEN,
                        "Mock broker failure follow-up",
                        "Failed FX request requires manual retry and broker desk confirmation.",
                        "opsadmin",
                        "opsadmin",
                        "FX_REQUEST",
                        "DEMO-FX-REQUEST",
                        1L
                );
                seedOpsCase(
                        "OPS-DEMO-002",
                        OpsCaseCategory.BATCH_FAILURE,
                        OpsCaseSeverity.MEDIUM,
                        OpsCaseStatus.IN_PROGRESS,
                        "Margin recalculation delay",
                        "Batch timeout investigation in progress with infra team.",
                        "opsadmin",
                        "opsadmin2",
                        "BATCH_RUN",
                        "2",
                        null
                );
            }

            if (primaryAccount != null && stockPurchaseRepository.count() == 0) {
                seedStockPurchase(
                        primaryAccount,
                        "AAPL",
                        "NASDAQ",
                        "USD",
                        LocalDate.now().minusDays(2),
                        LocalDate.now(),
                        "25.0000",
                        "182.450000",
                        "15.5000",
                        "opsadmin"
                );
                seedStockPurchase(
                        primaryAccount,
                        "AAPL",
                        "NASDAQ",
                        "USD",
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(1),
                        "10.0000",
                        "184.100000",
                        "7.2500",
                        "opsadmin2"
                );
                seedStockPurchase(
                        primaryAccount,
                        "NVDA",
                        "NASDAQ",
                        "USD",
                        LocalDate.now(),
                        LocalDate.now().plusDays(2),
                        "12.0000",
                        "812.350000",
                        "18.7500",
                        "opsadmin"
                );
            }
        };
    }

    private void ensureEnversSchema() {
        executeDdlSafely("""
                create table revinfo (
                    rev integer generated by default as identity primary key,
                    revtstmp bigint not null
                )
                """);

        List.of(
                "accounts",
                "approval_policies",
                "batch_runs",
                "balances",
                "cash_requests",
                "domain_terms",
                "exchange_rates",
                "fx_requests",
                "journal_entries",
                "ledger_entries",
                "margins",
                "menus",
                "ops_cases",
                "positions",
                "risk_limit_policies",
                "stock_positions",
                "stock_purchases",
                "users"
        ).forEach(this::ensureAuditShadowTable);
    }

    private void ensureAuditShadowTable(String baseTable) {
        String auditTable = baseTable + "_aud";
        executeDdlSafely("create table " + auditTable + " as select * from " + baseTable + " where 1 = 0");
        executeDdlSafely("alter table " + auditTable + " add column rev integer not null");
        executeDdlSafely("alter table " + auditTable + " add column revtype smallint");
    }

    private void executeDdlSafely(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException ex) {
            if (!isDuplicateDdl(ex)) {
                throw ex;
            }
        }
    }

    private boolean isDuplicateDdl(DataAccessException ex) {
        return containsDuplicateMarker(ex.getMessage())
                || containsDuplicateMarker(ex.getMostSpecificCause() == null ? null : ex.getMostSpecificCause().getMessage());
    }

    private boolean containsDuplicateMarker(String message) {
        if (message == null) {
            return false;
        }

        String normalized = message.toLowerCase(Locale.ROOT);
        return normalized.contains("already exists")
                || normalized.contains("already used by an existing object")
                || normalized.contains("duplicate column name");
    }

    private void seedStockPurchase(
            Account account,
            String symbol,
            String market,
            String currency,
            LocalDate tradeDate,
            LocalDate settlementDate,
            String quantity,
            String price,
            String feeAmount,
            String actor
    ) {
        stockPurchaseService.create(
                new CreateStockPurchaseRequest(
                        account.getId(),
                        symbol,
                        market,
                        currency,
                        tradeDate,
                        settlementDate,
                        new BigDecimal(quantity),
                        new BigDecimal(price),
                        new BigDecimal(feeAmount)
                ),
                actor
        );
    }

    private void createUser(String username, String password, UserRole role) {
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        userAccountRepository.save(user);
    }

    private void ensureDemoUser(String username, String password, UserRole role) {
        if (userAccountRepository.findByUsername(username).isPresent()) {
            return;
        }
        createUser(username, password, role);
    }

    private void patchLegacySchema() {
        jdbcTemplate.execute("alter table if exists cash_requests add column if not exists manual_review_required boolean");
        jdbcTemplate.execute("update cash_requests set manual_review_required = false where manual_review_required is null");
        jdbcTemplate.execute("alter table if exists cash_requests alter column manual_review_required set default false");
        jdbcTemplate.execute("alter table if exists cash_requests alter column manual_review_required set not null");

        jdbcTemplate.execute("alter table if exists fx_requests add column if not exists manual_review_required boolean");
        jdbcTemplate.execute("update fx_requests set manual_review_required = false where manual_review_required is null");
        jdbcTemplate.execute("alter table if exists fx_requests alter column manual_review_required set default false");
        jdbcTemplate.execute("alter table if exists fx_requests alter column manual_review_required set not null");
        jdbcTemplate.execute("alter table if exists menus add column if not exists description varchar(255)");
        jdbcTemplate.execute("alter table if exists menus add column if not exists parent_menu_key varchar(80)");
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
        request.setPriority(new BigDecimal(amount).compareTo(new BigDecimal("250000")) >= 0 ? RequestPriority.HIGH : RequestPriority.NORMAL);
        request.setValueDate(LocalDate.now());
        request.setSlaDueAt(OffsetDateTime.now().plusHours(4));
        request.setManualReviewRequired(false);
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
        applySeedFxQuote(request);
        request.setStatus(status);
        request.setPriority(new BigDecimal(amount).compareTo(new BigDecimal("250000")) >= 0 ? RequestPriority.HIGH : RequestPriority.NORMAL);
        request.setValueDate(LocalDate.now());
        request.setSlaDueAt(OffsetDateTime.now().plusHours(4));
        request.setManualReviewRequired(false);
        request.setRequestedBy(requestedBy);
        request.setReason(reason);
        request.setReviewedBy(reviewedBy);
        request.setReviewReason(reviewReason);
        if (status != RequestStatus.PENDING) {
            request.setReviewedAt(OffsetDateTime.now().minusMinutes(20));
        }
        fxRequestRepository.save(request);
    }

    private void applySeedFxQuote(FxRequest request) {
        LocalDate rateDate = LocalDate.now();
        exchangeRateRepository.findTopByFromCurrencyAndToCurrencyAndRateDateLessThanEqualOrderByRateDateDescIdDesc(
                        request.getFromCurrency(),
                        request.getToCurrency(),
                        rateDate
                )
                .ifPresent(rate -> {
                    request.setExchangeRate(rate.getRate());
                    request.setExpectedToAmount(request.getAmount().multiply(rate.getRate()));
                    request.setExchangeRateDate(rate.getRateDate());
                    request.setExchangeRateSource(rate.getSource() + " (DIRECT)");
                });
        if (request.getExchangeRate() != null) {
            return;
        }
        exchangeRateRepository.findTopByFromCurrencyAndToCurrencyAndRateDateLessThanEqualOrderByRateDateDescIdDesc(
                        request.getToCurrency(),
                        request.getFromCurrency(),
                        rateDate
                )
                .ifPresent(rate -> {
                    request.setExchangeRate(BigDecimal.ONE.divide(rate.getRate(), 8, java.math.RoundingMode.HALF_UP));
                    request.setExpectedToAmount(request.getAmount().multiply(request.getExchangeRate()));
                    request.setExchangeRateDate(rate.getRateDate());
                    request.setExchangeRateSource(rate.getSource() + " (INVERSE)");
                });
    }

    private void ensureExchangeRate(
            String fromCurrency,
            String toCurrency,
            LocalDate rateDate,
            String rate,
            String source
    ) {
        ExchangeRate entity = exchangeRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(fromCurrency, toCurrency, rateDate)
                .orElseGet(ExchangeRate::new);
        entity.setFromCurrency(fromCurrency);
        entity.setToCurrency(toCurrency);
        entity.setRateDate(rateDate);
        entity.setRate(new BigDecimal(rate));
        entity.setSource(source);
        exchangeRateRepository.save(entity);
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

    private void ensureMenu(
            String menuKey,
            String title,
            String description,
            String path,
            String parentMenuKey,
            String resourceName,
            String icon,
            int sortOrder,
            boolean enabled,
            String rolesCsv
    ) {
        MenuEntry menu = menuEntryRepository.findByMenuKey(menuKey).orElseGet(MenuEntry::new);
        menu.setMenuKey(menuKey);
        menu.setTitle(title);
        menu.setDescription(description);
        menu.setPath(path);
        menu.setParentMenuKey(parentMenuKey);
        menu.setResourceName(resourceName);
        menu.setIcon(icon);
        menu.setSortOrder(sortOrder);
        menu.setEnabled(enabled);
        menu.setRolesCsv(rolesCsv);
        menuEntryRepository.save(menu);
    }

    private void cleanupLegacyMenus(String... menuKeys) {
        for (String menuKey : menuKeys) {
            jdbcTemplate.update("delete from menus where menu_key = ?", menuKey);
        }
    }

    private void ensureDomainTerm(
            String domainKey,
            String domainName,
            int domainSortOrder,
            String termKey,
            String termName,
            String koreanName,
            String description,
            String exampleText,
            int sortOrder
    ) {
        DomainTerm term = domainTermRepository.findByTermKey(termKey).orElseGet(DomainTerm::new);
        term.setDomainKey(domainKey);
        term.setDomainName(domainName);
        term.setDomainSortOrder(domainSortOrder);
        term.setTermKey(termKey);
        term.setTermName(termName);
        term.setKoreanName(koreanName);
        term.setDescription(description);
        term.setExampleText(exampleText);
        term.setSortOrder(sortOrder);
        term.setEnabled(true);
        domainTermRepository.save(term);
    }

    private void ensureApprovalPolicy(
            String brokerCode,
            ApprovalDomain domain,
            String highThreshold,
            String urgentThreshold,
            String manualReviewThreshold,
            boolean sameDayAutoReview,
            boolean enabled,
            String description
    ) {
        String normalizedBroker = brokerCode.trim().toUpperCase();
        LocalDate today = LocalDate.now();
        ApprovalPolicy policy = approvalPolicyRepository.resolve(normalizedBroker, domain, today)
                .orElseGet(ApprovalPolicy::new);
        policy.setBrokerCode(normalizedBroker);
        policy.setDomain(domain);
        policy.setHighThreshold(new BigDecimal(highThreshold));
        policy.setUrgentThreshold(new BigDecimal(urgentThreshold));
        policy.setManualReviewThreshold(new BigDecimal(manualReviewThreshold));
        policy.setSameDayAutoReview(sameDayAutoReview);
        policy.setEnabled(enabled);
        policy.setEffectiveFrom(today.minusYears(1));
        policy.setDescription(description);
        approvalPolicyRepository.save(policy);
    }

    private void ensureRiskLimitPolicy(
            String brokerCode,
            ApprovalDomain domain,
            String currencyCode,
            String maxPerRequest,
            String dailySoftLimit,
            String dailyHardLimit,
            boolean enabled,
            String description
    ) {
        String normalizedBroker = brokerCode.trim().toUpperCase();
        String normalizedCurrency = currencyCode == null || currencyCode.isBlank() ? null : currencyCode.trim().toUpperCase();
        LocalDate today = LocalDate.now();

        RiskLimitPolicy policy = riskLimitPolicyRepository.resolve(normalizedBroker, domain, normalizedCurrency, today)
                .orElseGet(RiskLimitPolicy::new);
        policy.setBrokerCode(normalizedBroker);
        policy.setDomain(domain);
        policy.setCurrencyCode(normalizedCurrency);
        policy.setMaxPerRequest(new BigDecimal(maxPerRequest));
        policy.setDailySoftLimit(new BigDecimal(dailySoftLimit));
        policy.setDailyHardLimit(new BigDecimal(dailyHardLimit));
        policy.setEnabled(enabled);
        policy.setEffectiveFrom(today.minusYears(1));
        policy.setDescription(description);
        riskLimitPolicyRepository.save(policy);
    }

    private void seedOpsCase(
            String caseNo,
            OpsCaseCategory category,
            OpsCaseSeverity severity,
            OpsCaseStatus status,
            String title,
            String description,
            String createdBy,
            String assignee,
            String linkedType,
            String linkedId,
            Long accountId
    ) {
        OpsCase item = new OpsCase();
        item.setCaseNo(caseNo);
        item.setCategory(category);
        item.setSeverity(severity);
        item.setStatus(status);
        item.setTitle(title);
        item.setDescription(description);
        item.setCreatedBy(createdBy);
        item.setUpdatedBy(createdBy);
        item.setAssignee(assignee);
        item.setLinkedType(linkedType);
        item.setLinkedId(linkedId);
        item.setAccountId(accountId);
        item.setDueAt(OffsetDateTime.now().plusHours(6));
        opsCaseRepository.save(item);
    }
}
