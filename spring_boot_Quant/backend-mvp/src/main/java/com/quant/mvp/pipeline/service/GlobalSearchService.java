package com.quant.mvp.pipeline.service;

import com.quant.mvp.pipeline.domain.Menu;
import com.quant.mvp.pipeline.domain.Order;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.domain.Position;
import com.quant.mvp.pipeline.domain.Role;
import com.quant.mvp.pipeline.domain.Trade;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class GlobalSearchService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final OrderTradePositionPipelineService pipelineService;
    private final AccessControlService accessControlService;
    private final PortfolioCatalogService portfolioCatalogService;
    private final SavedViewService savedViewService;

    public GlobalSearchService(
            OrderTradePositionPipelineService pipelineService,
            AccessControlService accessControlService,
            PortfolioCatalogService portfolioCatalogService,
            SavedViewService savedViewService
    ) {
        this.pipelineService = pipelineService;
        this.accessControlService = accessControlService;
        this.portfolioCatalogService = portfolioCatalogService;
        this.savedViewService = savedViewService;
    }

    public GlobalSearchSnapshot search(String userEmail, String query, Integer limit) {
        String actor = requireText(userEmail, "userEmail").toLowerCase(Locale.ROOT);
        String normalizedQuery = normalize(query);
        int normalizedLimit = normalizeLimit(limit);
        int sectionItemLimit = Math.max(3, Math.min(8, normalizedLimit / 2));

        List<SearchSectionRow> sections = new ArrayList<>();

        List<SearchItemRow> navItems = searchNavigation(actor, normalizedQuery, sectionItemLimit);
        if (!navItems.isEmpty()) {
            sections.add(new SearchSectionRow("navigation", "메뉴 이동", navItems.size(), navItems));
        }

        if (hasRead(actor, "orders")) {
            List<SearchItemRow> orderItems = searchOrders(normalizedQuery, sectionItemLimit);
            if (!orderItems.isEmpty()) {
                sections.add(new SearchSectionRow("orders", "주문", orderItems.size(), orderItems));
            }
        }

        if (hasRead(actor, "trades")) {
            List<SearchItemRow> tradeItems = searchTrades(normalizedQuery, sectionItemLimit);
            if (!tradeItems.isEmpty()) {
                sections.add(new SearchSectionRow("trades", "체결", tradeItems.size(), tradeItems));
            }
        }

        if (hasRead(actor, "positions")) {
            List<SearchItemRow> positionItems = searchPositions(normalizedQuery, sectionItemLimit);
            if (!positionItems.isEmpty()) {
                sections.add(new SearchSectionRow("positions", "포지션", positionItems.size(), positionItems));
            }
        }

        if (hasRead(actor, "portfolios")) {
            List<SearchItemRow> portfolioItems = searchPortfolios(normalizedQuery, sectionItemLimit);
            if (!portfolioItems.isEmpty()) {
                sections.add(new SearchSectionRow("portfolios", "포트폴리오", portfolioItems.size(), portfolioItems));
            }
        }

        if (hasRead(actor, "savedViews")) {
            List<SearchItemRow> savedViewItems = searchSavedViews(actor, normalizedQuery, sectionItemLimit);
            if (!savedViewItems.isEmpty()) {
                sections.add(new SearchSectionRow("savedViews", "저장 뷰", savedViewItems.size(), savedViewItems));
            }
        }

        if (hasRead(actor, "users")) {
            List<SearchItemRow> userItems = searchUsers(normalizedQuery, sectionItemLimit);
            if (!userItems.isEmpty()) {
                sections.add(new SearchSectionRow("users", "사용자", userItems.size(), userItems));
            }
        }

        if (hasRead(actor, "roles")) {
            List<SearchItemRow> roleItems = searchRoles(normalizedQuery, sectionItemLimit);
            if (!roleItems.isEmpty()) {
                sections.add(new SearchSectionRow("roles", "권한", roleItems.size(), roleItems));
            }
        }

        return new GlobalSearchSnapshot(normalizedQuery == null ? "" : normalizedQuery, normalizedLimit, sections);
    }

    private List<SearchItemRow> searchNavigation(String actor, String query, int limit) {
        return accessControlService.listCurrentUserMenus(actor).stream()
                .map(item -> toNavItem(item.menu(), query))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(SearchItemRow::score).reversed().thenComparing(SearchItemRow::title))
                .limit(limit)
                .toList();
    }

    private SearchItemRow toNavItem(Menu menu, String query) {
        int score = scoreMatch(menu.menuLabel(), query, 30);
        score += scoreMatch(menu.menuKey(), query, 20);
        if (query != null && score <= 0) {
            return null;
        }

        return new SearchItemRow(
                "navigation",
                "menu:" + menu.menuId(),
                menu.menuLabel(),
                menu.menuKey(),
                menu.path(),
                score <= 0 ? 1 : score
        );
    }

    private List<SearchItemRow> searchOrders(String query, int limit) {
        return pipelineService.getOrders().stream()
                .map(order -> toOrderItem(order, query))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(SearchItemRow::score).reversed()
                        .thenComparing(SearchItemRow::itemKey).reversed())
                .limit(limit)
                .toList();
    }

    private SearchItemRow toOrderItem(Order order, String query) {
        String primary = order.symbol() + " " + order.status().name() + " " + order.orderId();
        int score = scoreMatch(primary, query, 50);
        if (query != null && score <= 0) {
            return null;
        }

        return new SearchItemRow(
                "orders",
                "order:" + order.orderId(),
                "#" + order.orderId() + " " + order.symbol() + " " + order.side() + " " + order.status(),
                "portfolio=" + order.portfolioId() + ", qty=" + order.quantity(),
                "/#/orders",
                score <= 0 ? 1 : score
        );
    }

    private List<SearchItemRow> searchTrades(String query, int limit) {
        return pipelineService.getTrades().stream()
                .map(trade -> toTradeItem(trade, query))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(SearchItemRow::score).reversed()
                        .thenComparing(SearchItemRow::itemKey).reversed())
                .limit(limit)
                .toList();
    }

    private SearchItemRow toTradeItem(Trade trade, String query) {
        String primary = trade.symbol() + " " + trade.tradeId() + " " + trade.orderId();
        int score = scoreMatch(primary, query, 45);
        if (query != null && score <= 0) {
            return null;
        }

        return new SearchItemRow(
                "trades",
                "trade:" + trade.tradeId(),
                "Trade #" + trade.tradeId() + " " + trade.symbol() + " " + trade.side(),
                "order=" + trade.orderId() + ", qty=" + trade.tradeQuantity() + ", price=" + trade.tradePrice(),
                "/#/trades",
                score <= 0 ? 1 : score
        );
    }

    private List<SearchItemRow> searchPositions(String query, int limit) {
        List<Long> portfolioIds = pipelineService.searchPortfolioSummaries(null).stream()
                .map(summary -> summary.portfolioId())
                .distinct()
                .sorted()
                .toList();

        List<SearchItemRow> rows = new ArrayList<>();
        for (Long portfolioId : portfolioIds) {
            for (Position position : pipelineService.getPositions(portfolioId)) {
                SearchItemRow row = toPositionItem(position, query);
                if (row != null) {
                    rows.add(row);
                }
            }
        }

        return rows.stream()
                .sorted(Comparator.comparing(SearchItemRow::score).reversed().thenComparing(SearchItemRow::title))
                .limit(limit)
                .toList();
    }

    private SearchItemRow toPositionItem(Position position, String query) {
        String primary = position.symbol() + " " + position.portfolioId();
        int score = scoreMatch(primary, query, 40);
        if (query != null && score <= 0) {
            return null;
        }
        return new SearchItemRow(
                "positions",
                "position:" + position.portfolioId() + ":" + position.symbol(),
                position.symbol() + " (Portfolio " + position.portfolioId() + ")",
                "qty=" + position.quantity() + ", mv=" + position.marketValue(),
                "/#/positions",
                score <= 0 ? 1 : score
        );
    }

    private List<SearchItemRow> searchPortfolios(String query, int limit) {
        return portfolioCatalogService.search(query, Boolean.TRUE).stream()
                .map(row -> new SearchItemRow(
                        "portfolios",
                        "portfolio:" + row.portfolioId(),
                        row.portfolioCode() + " · " + row.portfolioName(),
                        row.strategyTag() + " / " + row.benchmark(),
                        "/#/portfolioSummaries",
                        scoreMatch(row.portfolioCode() + " " + row.portfolioName(), query, 35)
                ))
                .sorted(Comparator.comparing(SearchItemRow::score).reversed().thenComparing(SearchItemRow::title))
                .limit(limit)
                .toList();
    }

    private List<SearchItemRow> searchSavedViews(String actor, String query, int limit) {
        return savedViewService.search(null, actor).stream()
                .map(view -> {
                    int score = scoreMatch(view.viewName() + " " + view.description(), query, 32);
                    if (query != null && score <= 0) {
                        return null;
                    }
                    return new SearchItemRow(
                            "savedViews",
                            "savedView:" + view.viewId(),
                            view.viewName(),
                            "resource=" + view.resourceKey() + ", " + (view.shared() ? "공용" : "개인"),
                            "/#/orders",
                            score <= 0 ? 1 : score
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(SearchItemRow::score).reversed().thenComparing(SearchItemRow::title))
                .limit(limit)
                .toList();
    }

    private List<SearchItemRow> searchUsers(String query, int limit) {
        return accessControlService.searchUsers(null, query, query, null, null).stream()
                .map(view -> new SearchItemRow(
                        "users",
                        "user:" + view.user().userId(),
                        view.user().name() + " <" + view.user().email() + ">",
                        "roles=" + String.join(",", view.roleCodes()),
                        "/#/users",
                        scoreMatch(view.user().name() + " " + view.user().email(), query, 34)
                ))
                .sorted(Comparator.comparing(SearchItemRow::score).reversed().thenComparing(SearchItemRow::title))
                .limit(limit)
                .toList();
    }

    private List<SearchItemRow> searchRoles(String query, int limit) {
        return accessControlService.searchRoles(null, query, query).stream()
                .map(this::toRoleItem)
                .map(item -> withScore(item, query))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(SearchItemRow::score).reversed().thenComparing(SearchItemRow::title))
                .limit(limit)
                .toList();
    }

    private SearchItemRow toRoleItem(Role role) {
        return new SearchItemRow(
                "roles",
                "role:" + role.roleId(),
                role.roleCode() + " · " + role.roleName(),
                role.description(),
                "/#/roles",
                1
        );
    }

    private SearchItemRow withScore(SearchItemRow item, String query) {
        int score = scoreMatch(item.title() + " " + item.subtitle(), query, 28);
        if (query != null && score <= 0) {
            return null;
        }
        return new SearchItemRow(
                item.resourceKey(),
                item.itemKey(),
                item.title(),
                item.subtitle(),
                item.path(),
                score <= 0 ? 1 : score
        );
    }

    private boolean hasRead(String actor, String menuKey) {
        return accessControlService.hasMenuPermission(actor, menuKey, PermissionAction.READ);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(MAX_LIMIT, limit);
    }

    private String normalize(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private String requireText(String text, String fieldName) {
        String normalized = normalize(text);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return normalized;
    }

    private int scoreMatch(String value, String query, int baseScore) {
        if (query == null) {
            return 1;
        }
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT);
        if (normalized.equals(query)) {
            return baseScore + 40;
        }
        if (normalized.startsWith(query)) {
            return baseScore + 24;
        }
        if (normalized.contains(query)) {
            return baseScore + 12;
        }
        return 0;
    }

    public record SearchItemRow(
            String resourceKey,
            String itemKey,
            String title,
            String subtitle,
            String path,
            Integer score
    ) {
    }

    public record SearchSectionRow(
            String resourceKey,
            String resourceLabel,
            Integer totalCount,
            List<SearchItemRow> items
    ) {
    }

    public record GlobalSearchSnapshot(
            String query,
            Integer limit,
            List<SearchSectionRow> sections
    ) {
    }
}
