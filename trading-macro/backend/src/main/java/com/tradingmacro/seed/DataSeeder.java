package com.tradingmacro.seed;

import com.tradingmacro.menu.Menu;
import com.tradingmacro.menu.MenuPermission;
import com.tradingmacro.menu.MenuPermissionRepository;
import com.tradingmacro.menu.MenuRepository;
import com.tradingmacro.macro.MacroJob;
import com.tradingmacro.macro.MacroJobRepository;
import com.tradingmacro.performance.PerformanceSnapshot;
import com.tradingmacro.performance.PerformanceSnapshotRepository;
import com.tradingmacro.performance.PerformanceSummary;
import com.tradingmacro.performance.PerformanceSummaryRepository;
import com.tradingmacro.org.Book;
import com.tradingmacro.org.BookRepository;
import com.tradingmacro.org.Desk;
import com.tradingmacro.org.DeskRepository;
import com.tradingmacro.org.Team;
import com.tradingmacro.org.TeamRepository;
import com.tradingmacro.portfolio.Portfolio;
import com.tradingmacro.portfolio.PortfolioRepository;
import com.tradingmacro.risk.RiskPolicy;
import com.tradingmacro.risk.RiskPolicyRepository;
import com.tradingmacro.strategy.Strategy;
import com.tradingmacro.strategy.StrategyRepository;
import com.tradingmacro.trade.Trade;
import com.tradingmacro.trade.TradeRepository;
import com.tradingmacro.user.User;
import com.tradingmacro.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StrategyRepository strategyRepository;
    private final MacroJobRepository macroJobRepository;
    private final TradeRepository tradeRepository;
    private final PortfolioRepository portfolioRepository;
    private final RiskPolicyRepository riskPolicyRepository;
    private final PerformanceSnapshotRepository performanceSnapshotRepository;
    private final PerformanceSummaryRepository performanceSummaryRepository;
    private final TeamRepository teamRepository;
    private final DeskRepository deskRepository;
    private final BookRepository bookRepository;
    private final MenuRepository menuRepository;
    private final MenuPermissionRepository menuPermissionRepository;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      StrategyRepository strategyRepository,
                      MacroJobRepository macroJobRepository,
                      TradeRepository tradeRepository,
                      PortfolioRepository portfolioRepository,
                      RiskPolicyRepository riskPolicyRepository,
                      PerformanceSnapshotRepository performanceSnapshotRepository,
                      PerformanceSummaryRepository performanceSummaryRepository,
                      TeamRepository teamRepository,
                      DeskRepository deskRepository,
                      BookRepository bookRepository,
                      MenuRepository menuRepository,
                      MenuPermissionRepository menuPermissionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.strategyRepository = strategyRepository;
        this.macroJobRepository = macroJobRepository;
        this.tradeRepository = tradeRepository;
        this.portfolioRepository = portfolioRepository;
        this.riskPolicyRepository = riskPolicyRepository;
        this.performanceSnapshotRepository = performanceSnapshotRepository;
        this.performanceSummaryRepository = performanceSummaryRepository;
        this.teamRepository = teamRepository;
        this.deskRepository = deskRepository;
        this.bookRepository = bookRepository;
        this.menuRepository = menuRepository;
        this.menuPermissionRepository = menuPermissionRepository;
    }

    @Override
    public void run(String... args) {
        if (teamRepository.count() == 0) {
            Team t1 = new Team();
            t1.setName("Equity Quant Team");
            Team t2 = new Team();
            t2.setName("Macro Desk");
            teamRepository.saveAll(List.of(t1, t2));

            Desk d1 = new Desk();
            d1.setName("KOSPI StatArb");
            d1.setTeam(t1);
            Desk d2 = new Desk();
            d2.setName("FX Macro");
            d2.setTeam(t2);
            deskRepository.saveAll(List.of(d1, d2));

            Book b1 = new Book();
            b1.setName("KRW Alpha Book");
            b1.setDesk(d1);
            Book b2 = new Book();
            b2.setName("G10 FX Book");
            b2.setDesk(d2);
            bookRepository.saveAll(List.of(b1, b2));
        }

        if (userRepository.count() == 0) {
            Team team = teamRepository.findAll().get(0);
            Desk desk = deskRepository.findAll().get(0);
            Book book = bookRepository.findAll().get(0);
            User admin = new User();
            admin.setEmail("admin@tm.local");
            admin.setDisplayName("Admin");
            admin.setRole(User.Role.ADMIN);
            admin.setPasswordHash(passwordEncoder.encode("admin1234"));
            admin.setTeam(team);
            admin.setDesk(desk);
            admin.setBook(book);
            userRepository.save(admin);

            User trader = new User();
            trader.setEmail("trader@tm.local");
            trader.setDisplayName("Quant Trader");
            trader.setRole(User.Role.TRADER);
            trader.setPasswordHash(passwordEncoder.encode("trader1234"));
            trader.setTeam(team);
            trader.setDesk(desk);
            trader.setBook(book);
            userRepository.save(trader);
        }

        if (strategyRepository.count() == 0) {
            Strategy s1 = new Strategy();
            s1.setName("KOSPI Mean Reversion");
            s1.setDescription("Overnight mean reversion on top 50 liquid names.");
            s1.setAssetClass("Equities");
            s1.setTimeframe("D1");
            s1.setRiskLevel("Medium");
            s1.setStatus(Strategy.Status.LIVE);

            Strategy s2 = new Strategy();
            s2.setName("FX Momentum");
            s2.setDescription("G10 FX 3M momentum rotation.");
            s2.setAssetClass("FX");
            s2.setTimeframe("W1");
            s2.setRiskLevel("Low");
            s2.setStatus(Strategy.Status.PAUSED);

            strategyRepository.saveAll(List.of(s1, s2));
        }

        if (portfolioRepository.count() == 0) {
            Portfolio p1 = new Portfolio();
            p1.setName("Korea Alpha Book");
            p1.setBaseCurrency("KRW");
            p1.setTotalValue(new BigDecimal("1250000000"));

            Portfolio p2 = new Portfolio();
            p2.setName("Macro FX Sleeve");
            p2.setBaseCurrency("USD");
            p2.setTotalValue(new BigDecimal("4200000"));

            portfolioRepository.saveAll(List.of(p1, p2));
        }

        if (macroJobRepository.count() == 0) {
            Strategy firstStrategy = strategyRepository.findAll().get(0);
            MacroJob job = new MacroJob();
            job.setName("Morning Signal Pack");
            job.setSchedule("0 0 9 * * MON-FRI");
            job.setStatus(MacroJob.Status.ACTIVE);
            job.setLastRun(Instant.now().minusSeconds(3600));
            job.setNextRun(Instant.now().plusSeconds(86400));
            job.setStrategy(firstStrategy);
            macroJobRepository.save(job);
        }

        if (tradeRepository.count() == 0) {
            Strategy strategy = strategyRepository.findAll().get(0);
            Portfolio portfolio = portfolioRepository.findAll().get(0);
            Trade t1 = new Trade();
            t1.setSymbol("005930.KS");
            t1.setSide(Trade.Side.BUY);
            t1.setQuantity(new BigDecimal("1500"));
            t1.setPrice(new BigDecimal("74000"));
            t1.setExecutedAt(Instant.now().minusSeconds(7200));
            t1.setStrategy(strategy);
            t1.setPortfolio(portfolio);

            Trade t2 = new Trade();
            t2.setSymbol("035420.KS");
            t2.setSide(Trade.Side.SELL);
            t2.setQuantity(new BigDecimal("600"));
            t2.setPrice(new BigDecimal("195000"));
            t2.setExecutedAt(Instant.now().minusSeconds(3600));
            t2.setStrategy(strategy);
            t2.setPortfolio(portfolio);

            tradeRepository.saveAll(List.of(t1, t2));
        }

        if (riskPolicyRepository.count() == 0) {
            Portfolio portfolio = portfolioRepository.findAll().get(0);
            RiskPolicy policy = new RiskPolicy();
            policy.setName("KRW Equity Core Limits");
            policy.setMaxDailyLoss(new BigDecimal("15000000"));
            policy.setMaxPositionSize(new BigDecimal("250000000"));
            policy.setMaxLeverage(new BigDecimal("1.8"));
            policy.setAllowedAssetClasses("Equities, ETFs");
            policy.setStatus(RiskPolicy.Status.ACTIVE);
            policy.setPortfolio(portfolio);
            policy.setUpdatedAt(Instant.now());
            riskPolicyRepository.save(policy);
        }

        if (performanceSnapshotRepository.count() == 0) {
            Portfolio portfolio = portfolioRepository.findAll().get(0);
            PerformanceSnapshot snap = new PerformanceSnapshot();
            snap.setSnapshotDate(LocalDate.now().minusDays(1));
            snap.setPnl(new BigDecimal("42000000"));
            snap.setReturnPct(new BigDecimal("1.25"));
            snap.setDrawdownPct(new BigDecimal("0.6"));
            snap.setSharpeRatio(new BigDecimal("1.9"));
            snap.setVolatilityPct(new BigDecimal("12.4"));
            snap.setPortfolio(portfolio);
            performanceSnapshotRepository.save(snap);
        }

        if (performanceSummaryRepository.count() == 0) {
            Portfolio portfolio = portfolioRepository.findAll().get(0);
            Strategy strategy = strategyRepository.findAll().get(0);
            PerformanceSummary summary = new PerformanceSummary();
            summary.setPeriod(PerformanceSummary.Period.MONTHLY);
            summary.setPeriodStart(LocalDate.now().minusMonths(1).withDayOfMonth(1));
            summary.setPeriodEnd(LocalDate.now().minusMonths(1).withDayOfMonth(28));
            summary.setReturnPct(new BigDecimal("4.2"));
            summary.setBenchmarkReturnPct(new BigDecimal("2.5"));
            summary.setExcessReturnPct(new BigDecimal("1.7"));
            summary.setMaxDrawdownPct(new BigDecimal("1.1"));
            summary.setWinRatePct(new BigDecimal("58.0"));
            summary.setProfitFactor(new BigDecimal("1.35"));
            summary.setBenchmarkName("KOSPI 200");
            summary.setPortfolio(portfolio);
            summary.setStrategy(strategy);
            performanceSummaryRepository.save(summary);
        }

        if (menuRepository.count() == 0) {
            Menu m1 = createMenu("dashboard", "Dashboard", "/#/strategies", null, 1);
            Menu m2 = createMenu("strategies", "Strategies", "/#/strategies", null, 2);
            Menu m3 = createMenu("macros", "Macros", "/#/macros", null, 3);
            Menu m4 = createMenu("trades", "Trades", "/#/trades", null, 4);
            Menu m5 = createMenu("portfolios", "Portfolios", "/#/portfolios", null, 5);
            Menu m6 = createMenu("risk", "Risk Policies", "/#/risk-policies", null, 6);
            Menu m7 = createMenu("performance", "Performance", "/#/performance", null, 7);
            Menu m8 = createMenu("performanceSummaries", "Performance Summary", "/#/performance-summaries", null, 8);
            Menu m9 = createMenu("org", "Org", null, null, 9);

            menuRepository.saveAll(List.of(m1, m2, m3, m4, m5, m6, m7, m8, m9));

            Menu m10 = createMenu("teams", "Teams", "/#/teams", m9.getId(), 1);
            Menu m11 = createMenu("desks", "Desks", "/#/desks", m9.getId(), 2);
            Menu m12 = createMenu("books", "Books", "/#/books", m9.getId(), 3);
            Menu m13 = createMenu("menuMgmt", "Menu Mgmt", "/#/menus", null, 10);
            Menu m14 = createMenu("menuPerms", "Menu Permissions", "/#/menu-permissions", null, 11);
            menuRepository.saveAll(List.of(m10, m11, m12, m13, m14));
        }

        if (menuPermissionRepository.count() == 0) {
            List<Menu> menus = menuRepository.findAll();
            for (Menu menu : menus) {
                MenuPermission adminPerm = new MenuPermission();
                adminPerm.setRole(User.Role.ADMIN);
                adminPerm.setMenu(menu);
                adminPerm.setCanView(true);
                adminPerm.setCanEdit(true);
                menuPermissionRepository.save(adminPerm);

                if (!menu.getCode().startsWith("menu") && !menu.getCode().equals("org")) {
                    MenuPermission traderPerm = new MenuPermission();
                    traderPerm.setRole(User.Role.TRADER);
                    traderPerm.setMenu(menu);
                    traderPerm.setCanView(true);
                    traderPerm.setCanEdit(false);
                    menuPermissionRepository.save(traderPerm);
                }
            }
        }
    }

    private Menu createMenu(String code, String name, String path, Long parentId, int sortOrder) {
        Menu menu = new Menu();
        menu.setCode(code);
        menu.setName(name);
        menu.setPath(path);
        menu.setParentId(parentId);
        menu.setSortOrder(sortOrder);
        menu.setStatus(Menu.Status.ACTIVE);
        return menu;
    }
}
