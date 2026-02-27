package com.portal.admin.service;

import static com.portal.admin.dto.StatisticsDtos.*;

import com.portal.admin.domain.ServiceAuditLog;
import com.portal.admin.menu.repo.MenuRepository;
import com.portal.admin.repo.AccessLogRepository;
import com.portal.admin.repo.AdminUserRepository;
import com.portal.admin.repo.ContentRepository;
import com.portal.admin.repo.PermissionRepository;
import com.portal.admin.repo.ProgramItemRepository;
import com.portal.admin.repo.RoleRepository;
import com.portal.admin.repo.ServiceAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class StatisticsService {

    private static final Logger log = LoggerFactory.getLogger(StatisticsService.class);

    private final AdminUserRepository admins;
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final MenuRepository menus;
    private final ProgramItemRepository programs;
    private final ContentRepository contents;
    private final AccessLogRepository accessLogs;
    private final ServiceAuditLogRepository serviceAuditLogs;

    public StatisticsService(
            AdminUserRepository admins,
            RoleRepository roles,
            PermissionRepository permissions,
            MenuRepository menus,
            ProgramItemRepository programs,
            ContentRepository contents,
            AccessLogRepository accessLogs,
            ServiceAuditLogRepository serviceAuditLogs
    ) {
        this.admins = admins;
        this.roles = roles;
        this.permissions = permissions;
        this.menus = menus;
        this.programs = programs;
        this.contents = contents;
        this.accessLogs = accessLogs;
        this.serviceAuditLogs = serviceAuditLogs;
    }

    public StatisticsOverviewResponse overview() {
        writeReadAudit("OVERVIEW");
        return buildOverview();
    }

    public List<PathUsageResponse> menuUsage(int limit) {
        writeReadAudit("MENU_USAGE");
        return buildMenuUsage(limit);
    }

    public List<ActionUsageResponse> actionUsage(int limit) {
        writeReadAudit("ACTION_USAGE");
        return buildActionUsage(limit);
    }

    public StatisticsDashboardResponse dashboard(int limit) {
        writeReadAudit("DASHBOARD");
        return new StatisticsDashboardResponse(
                buildOverview(),
                buildMenuUsage(limit),
                buildActionUsage(limit)
        );
    }

    private StatisticsOverviewResponse buildOverview() {
        Instant todayStart = ZonedDateTime.now(ZoneId.systemDefault())
                .toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        return new StatisticsOverviewResponse(
                admins.count(),
                roles.count(),
                permissions.count(),
                menus.count(),
                programs.count(),
                contents.count(),
                accessLogs.count(),
                serviceAuditLogs.count(),
                accessLogs.countSince(todayStart),
                accessLogs.countSuccessfulActionSince(todayStart, "LOGIN")
        );
    }

    private List<PathUsageResponse> buildMenuUsage(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return accessLogs.findTopPathCounts(safeLimit).stream()
                .map(view -> new PathUsageResponse(view.path(), view.total()))
                .toList();
    }

    private List<ActionUsageResponse> buildActionUsage(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return accessLogs.findTopActionCounts(safeLimit).stream()
                .map(view -> new ActionUsageResponse(view.action(), view.total()))
                .toList();
    }

    private void writeReadAudit(String action) {
        String username = "anonymous";
        String detail = "GET /statistics";

        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            Object authUsername = attrs.getRequest().getAttribute("auth.username");
            if (authUsername instanceof String raw && !raw.isBlank()) {
                username = raw;
            }
            detail = attrs.getRequest().getMethod() + " " + attrs.getRequest().getRequestURI();
        }

        try {
            serviceAuditLogs.save(new ServiceAuditLog("STATISTICS", 0L, action, username, detail));
        } catch (Exception exception) {
            log.warn("Failed to persist statistics audit log: action={}", action, exception);
        }
    }
}
