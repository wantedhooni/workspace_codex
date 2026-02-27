package com.portal.admin.api;

import com.portal.admin.domain.AccessLog;
import com.portal.admin.domain.AdminUser;
import com.portal.admin.domain.BannerItem;
import com.portal.admin.domain.BatchJob;
import com.portal.admin.domain.BatchSchedule;
import com.portal.admin.domain.CommonCode;
import com.portal.admin.domain.ContentPage;
import com.portal.admin.domain.LoginPolicy;
import com.portal.admin.domain.Permission;
import com.portal.admin.domain.ProgramItem;
import com.portal.admin.domain.Role;
import com.portal.admin.domain.RoleRouteMapping;
import com.portal.admin.domain.ServiceAuditLog;
import com.portal.admin.menu.domain.MenuItem;
import com.portal.admin.menu.repo.MenuRepository;
import com.portal.admin.repo.AccessLogRepository;
import com.portal.admin.repo.AdminUserRepository;
import com.portal.admin.repo.BannerItemRepository;
import com.portal.admin.repo.BatchJobRepository;
import com.portal.admin.repo.BatchScheduleRepository;
import com.portal.admin.repo.CommonCodeRepository;
import com.portal.admin.repo.ContentRepository;
import com.portal.admin.repo.LoginPolicyRepository;
import com.portal.admin.repo.PermissionRepository;
import com.portal.admin.repo.ProgramItemRepository;
import com.portal.admin.repo.RoleRepository;
import com.portal.admin.repo.RoleRouteMappingRepository;
import com.portal.admin.repo.ServiceAuditLogRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class SeedData {

    @Bean
    CommandLineRunner seed(
            AdminUserRepository users,
            RoleRepository roles,
            PermissionRepository permissions,
            MenuRepository menus,
            ContentRepository contents,
            CommonCodeRepository commonCodes,
            ProgramItemRepository programs,
            RoleRouteMappingRepository roleRoutes,
            AccessLogRepository accessLogs,
            ServiceAuditLogRepository serviceAuditLogs,
            BatchJobRepository batchJobs,
            BatchScheduleRepository batchSchedules,
            BannerItemRepository banners,
            LoginPolicyRepository loginPolicies,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Map<String, Permission> permissionMap = ensurePermissions(permissions);
            Map<String, Role> roleMap = ensureRoles(roles, permissionMap);
            ensureDemoUsers(users, roleMap, passwordEncoder);
            ensureMenus(menus);
            ensureContents(contents);
            ensureCommonCodes(commonCodes);
            ensurePrograms(programs);
            ensureRoleRoutes(roleRoutes, roleMap);
            Map<String, BatchJob> batchJobMap = ensureBatchJobs(batchJobs);
            ensureBatchSchedules(batchSchedules, batchJobMap);
            ensureBanners(banners);
            ensureLoginPolicies(loginPolicies);
            ensureAccessLogs(accessLogs);
            ensureServiceAuditLogs(serviceAuditLogs);
        };
    }

    private Map<String, Permission> ensurePermissions(PermissionRepository permissions) {
        List<PermissionSeed> seeds = List.of(
                new PermissionSeed("CODE_MANAGE", "Common code management"),
                new PermissionSeed("ADMIN_MANAGE", "Admin management"),
                new PermissionSeed("ROLE_MANAGE", "Role management"),
                new PermissionSeed("MENU_MANAGE", "Menu management"),
                new PermissionSeed("PROGRAM_MANAGE", "Program management"),
                new PermissionSeed("ROUTE_MANAGE", "Role route mapping management"),
                new PermissionSeed("CONTENT_MANAGE", "Content management"),
                new PermissionSeed("BATCH_MANAGE", "Batch management"),
                new PermissionSeed("BANNER_MANAGE", "Banner/popup management"),
                new PermissionSeed("POLICY_MANAGE", "Login policy management"),
                new PermissionSeed("SETTINGS_MANAGE", "Settings management"),
                new PermissionSeed("LOG_VIEW", "Access and service log view"),
                new PermissionSeed("ADMIN_VIEW", "Admin read"),
                new PermissionSeed("ROLE_VIEW", "Role read"),
                new PermissionSeed("MENU_VIEW", "Menu read"),
                new PermissionSeed("PROGRAM_VIEW", "Program read"),
                new PermissionSeed("CONTENT_VIEW", "Content read"),
                new PermissionSeed("REPORT_VIEW", "Report read")
        );

        Map<String, Permission> permissionMap = new LinkedHashMap<>();
        for (PermissionSeed seed : seeds) {
            Permission permission = permissions.findByCode(seed.code())
                    .orElseGet(() -> new Permission(seed.code(), seed.description()));
            permission.setDescription(seed.description());
            permissionMap.put(seed.code(), permissions.save(permission));
        }
        return permissionMap;
    }

    private Map<String, Role> ensureRoles(RoleRepository roles, Map<String, Permission> permissionMap) {
        List<RoleSeed> seeds = List.of(
                new RoleSeed("SUPER_ADMIN", "Full access", new ArrayList<>(permissionMap.keySet())),
                new RoleSeed("CONTENT_MANAGER", "Content and menu read access", List.of(
                        "CONTENT_MANAGE",
                        "CONTENT_VIEW",
                        "MENU_VIEW",
                        "PROGRAM_VIEW"
                )),
                new RoleSeed("OPS_MANAGER", "Operations and settings access", List.of(
                        "MENU_MANAGE",
                        "MENU_VIEW",
                        "PROGRAM_MANAGE",
                        "PROGRAM_VIEW",
                        "ROUTE_MANAGE",
                        "BATCH_MANAGE",
                        "BANNER_MANAGE",
                        "POLICY_MANAGE",
                        "LOG_VIEW",
                        "REPORT_VIEW",
                        "SETTINGS_MANAGE"
                )),
                new RoleSeed("AUDITOR", "Read-only access", List.of(
                        "ADMIN_VIEW",
                        "ROLE_VIEW",
                        "MENU_VIEW",
                        "PROGRAM_VIEW",
                        "CONTENT_VIEW",
                        "LOG_VIEW",
                        "REPORT_VIEW"
                )),
                new RoleSeed("SECURITY_ADMIN", "Security and auth policy management", List.of(
                        "ADMIN_VIEW",
                        "ROLE_MANAGE",
                        "ROLE_VIEW",
                        "ROUTE_MANAGE",
                        "POLICY_MANAGE",
                        "LOG_VIEW"
                ))
        );

        Map<String, Role> roleMap = new LinkedHashMap<>();
        for (RoleSeed seed : seeds) {
            Role role = roles.findByName(seed.name())
                    .orElseGet(() -> new Role(seed.name(), seed.description()));
            role.setDescription(seed.description());
            role.setPermissions(resolvePermissions(permissionMap, seed.permissionCodes()));
            roleMap.put(seed.name(), roles.save(role));
        }
        return roleMap;
    }

    private void ensureDemoUsers(AdminUserRepository users, Map<String, Role> roleMap, PasswordEncoder passwordEncoder) {
        ensureDemoUser(users, passwordEncoder, "admin", "admin1234", Set.of(requiredRole(roleMap, "SUPER_ADMIN")));
        ensureDemoUser(users, passwordEncoder, "tester", "tester1234", Set.of(requiredRole(roleMap, "CONTENT_MANAGER")));
        ensureDemoUser(users, passwordEncoder, "ops", "ops1234", Set.of(requiredRole(roleMap, "OPS_MANAGER")));
        ensureDemoUser(users, passwordEncoder, "manager", "manager1234", Set.of(requiredRole(roleMap, "AUDITOR")));
        ensureDemoUser(users, passwordEncoder, "security", "security1234", Set.of(requiredRole(roleMap, "SECURITY_ADMIN")));
    }

    private void ensureDemoUser(
            AdminUserRepository users,
            PasswordEncoder passwordEncoder,
            String username,
            String password,
            Set<Role> roles
    ) {
        AdminUser user = users.findByUsername(username)
                .orElseGet(() -> new AdminUser(username, password));
        String existingHash = user.getPasswordHash();
        boolean matched = false;
        if (existingHash != null) {
            try {
                matched = passwordEncoder.matches(password, existingHash);
            } catch (IllegalArgumentException ignored) {
                matched = false;
            }
        }
        if (existingHash == null || !matched) {
            user.setPasswordHash(passwordEncoder.encode(password));
            user.incrementTokenVersion();
        }
        user.setRoles(roles);
        users.save(user);
    }

    private void ensureMenus(MenuRepository menus) {
        List<MenuSeed> seeds = List.of(
                new MenuSeed("Dashboard", "/", null, 0),
                new MenuSeed("Administration", "/administration", null, 10),
                new MenuSeed("Admins", "/admins", "/administration", 11),
                new MenuSeed("Roles", "/roles", "/administration", 12),
                new MenuSeed("Permissions", "/permissions", "/administration", 13),

                new MenuSeed("Content", "/content", null, 20),
                new MenuSeed("Contents", "/contents", "/content", 21),
                new MenuSeed("Banners", "/banners", "/content", 22),

                new MenuSeed("System", "/system", null, 30),
                new MenuSeed("Menus", "/menus", "/system", 31),
                new MenuSeed("Common Codes", "/common-codes", "/system", 32),
                new MenuSeed("Programs", "/programs", "/system", 33),
                new MenuSeed("Role Routes", "/role-routes", "/system", 34),
                new MenuSeed("Login Policies", "/login-policies", "/system", 35),

                new MenuSeed("Monitoring", "/monitoring", null, 40),
                new MenuSeed("Statistics", "/statistics", "/monitoring", 41),
                new MenuSeed("Access Logs", "/access-logs", "/monitoring", 42),
                new MenuSeed("Service Audit Logs", "/service-audit-logs", "/monitoring", 43),

                new MenuSeed("Batch", "/batch", null, 50),
                new MenuSeed("Batch Jobs", "/batch-jobs", "/batch", 51),
                new MenuSeed("Batch Schedules", "/batch-schedules", "/batch", 52)
        );

        Map<String, MenuItem> menuByPath = new LinkedHashMap<>();
        for (MenuSeed seed : seeds) {
            Long parentId = null;
            if (seed.parentPath() != null) {
                MenuItem parent = menuByPath.get(seed.parentPath());
                if (parent == null) {
                    parent = menus.findFirstByPath(seed.parentPath())
                            .orElseThrow(() -> new IllegalStateException("missing parent menu: " + seed.parentPath()));
                    menuByPath.put(parent.getPath(), parent);
                }
                parentId = parent.getId();
            }

            MenuItem item = menus.findFirstByPath(seed.path())
                    .orElse(null);
            if (item == null) {
                item = new MenuItem(seed.title(), seed.path(), parentId, seed.sortOrder());
            }
            item.setTitle(seed.title());
            item.setPath(seed.path());
            item.setParentId(parentId);
            item.setSortOrder(seed.sortOrder());
            MenuItem saved = menus.save(item);
            menuByPath.put(saved.getPath(), saved);
        }
    }

    private void ensureContents(ContentRepository contents) {
        List<ContentSeed> seeds = List.of(
                new ContentSeed(
                        "Welcome",
                        "welcome",
                        "Welcome to the admin portal demo.\nUse this page to verify CRUD behavior.",
                        "PUBLISHED"
                ),
                new ContentSeed(
                        "Operations Guide",
                        "operations-guide",
                        "1. Check dashboard metrics.\n2. Review failed jobs.\n3. Escalate incidents to on-call.",
                        "DRAFT"
                ),
                new ContentSeed(
                        "Release Notes",
                        "release-notes",
                        "Initial release with admin, role/permission, menu, and content resources.",
                        "PUBLISHED"
                )
        );

        for (ContentSeed seed : seeds) {
            if (!contents.existsBySlug(seed.slug())) {
                contents.save(new ContentPage(seed.title(), seed.slug(), seed.body(), seed.status()));
            }
        }
    }

    private void ensureCommonCodes(CommonCodeRepository commonCodes) {
        List<CommonCodeSeed> seeds = List.of(
                new CommonCodeSeed("USER_STATUS", "ACTIVE", "Active", "Active admin user", 10, true),
                new CommonCodeSeed("USER_STATUS", "LOCKED", "Locked", "Login locked user", 20, true),
                new CommonCodeSeed("CONTENT_STATUS", "DRAFT", "Draft", "Draft content", 10, true),
                new CommonCodeSeed("CONTENT_STATUS", "PUBLISHED", "Published", "Published content", 20, true),
                new CommonCodeSeed("CONTENT_STATUS", "ARCHIVED", "Archived", "Archived content", 30, true),
                new CommonCodeSeed("LOG_ACTION", "LOGIN", "Login", "Login event", 10, true),
                new CommonCodeSeed("LOG_ACTION", "LOGOUT", "Logout", "Logout event", 20, true)
        );

        for (CommonCodeSeed seed : seeds) {
            CommonCode code = commonCodes.findFirstByGroupCodeAndCode(seed.groupCode(), seed.code())
                    .orElseGet(() -> new CommonCode(
                            seed.groupCode(),
                            seed.code(),
                            seed.name(),
                            seed.description(),
                            seed.sortOrder(),
                            seed.enabled()
                    ));
            code.setName(seed.name());
            code.setDescription(seed.description());
            code.setSortOrder(seed.sortOrder());
            code.setEnabled(seed.enabled());
            commonCodes.save(code);
        }
    }

    private void ensurePrograms(ProgramItemRepository programs) {
        List<ProgramSeed> seeds = List.of(
                new ProgramSeed("Admin Users", "/admins", "GET", "Admin user list page", true),
                new ProgramSeed("Roles", "/roles", "GET", "Role list page", true),
                new ProgramSeed("Permissions", "/permissions", "GET", "Permission list page", true),
                new ProgramSeed("Menus", "/menus", "GET", "Menu list page", true),
                new ProgramSeed("Contents", "/contents", "GET", "Content list page", true),
                new ProgramSeed("Common Codes", "/common-codes", "GET", "Common code management", true),
                new ProgramSeed("Programs", "/programs", "GET", "Program management", true),
                new ProgramSeed("Role Routes", "/role-routes", "GET", "Role URL mapping", true),
                new ProgramSeed("Access Logs", "/access-logs", "GET", "Access log monitoring", true),
                new ProgramSeed("Service Audit Logs", "/service-audit-logs", "GET", "Service audit logs", true),
                new ProgramSeed("Batch Jobs", "/batch-jobs", "GET", "Batch job management", true),
                new ProgramSeed("Batch Schedules", "/batch-schedules", "GET", "Batch schedule management", true),
                new ProgramSeed("Banners", "/banners", "GET", "Banner and popup management", true),
                new ProgramSeed("Login Policies", "/login-policies", "GET", "Login policy management", true),
                new ProgramSeed("Statistics Dashboard", "/statistics", "GET", "Operations dashboard and reports", true)
        );

        for (ProgramSeed seed : seeds) {
            ProgramItem program = programs.findFirstByUrlAndHttpMethod(seed.url(), seed.httpMethod())
                    .orElseGet(() -> new ProgramItem(
                            seed.name(),
                            seed.url(),
                            seed.httpMethod(),
                            seed.description(),
                            seed.enabled()
                    ));
            program.setName(seed.name());
            program.setDescription(seed.description());
            program.setEnabled(seed.enabled());
            programs.save(program);
        }
    }

    private void ensureRoleRoutes(RoleRouteMappingRepository roleRoutes, Map<String, Role> roleMap) {
        List<RoleRouteSeed> seeds = List.of(
                new RoleRouteSeed(requiredRole(roleMap, "SUPER_ADMIN").getId(), "/**", "GET", "Super admin all read", true),
                new RoleRouteSeed(requiredRole(roleMap, "SUPER_ADMIN").getId(), "/**", "POST", "Super admin all create", true),
                new RoleRouteSeed(requiredRole(roleMap, "SUPER_ADMIN").getId(), "/**", "PATCH", "Super admin all update", true),
                new RoleRouteSeed(requiredRole(roleMap, "SUPER_ADMIN").getId(), "/**", "DELETE", "Super admin all delete", true),
                new RoleRouteSeed(requiredRole(roleMap, "OPS_MANAGER").getId(), "/batch-**", "GET", "Ops batch read", true),
                new RoleRouteSeed(requiredRole(roleMap, "OPS_MANAGER").getId(), "/batch-**", "POST", "Ops batch write", true),
                new RoleRouteSeed(requiredRole(roleMap, "OPS_MANAGER").getId(), "/statistics/**", "GET", "Ops statistics read", true),
                new RoleRouteSeed(requiredRole(roleMap, "AUDITOR").getId(), "/access-logs", "GET", "Auditor access logs", true),
                new RoleRouteSeed(requiredRole(roleMap, "AUDITOR").getId(), "/service-audit-logs", "GET", "Auditor service logs", true),
                new RoleRouteSeed(requiredRole(roleMap, "AUDITOR").getId(), "/statistics/**", "GET", "Auditor statistics read", true),
                new RoleRouteSeed(requiredRole(roleMap, "SECURITY_ADMIN").getId(), "/login-policies", "GET", "Security policy read", true),
                new RoleRouteSeed(requiredRole(roleMap, "SECURITY_ADMIN").getId(), "/login-policies", "PATCH", "Security policy update", true)
        );

        for (RoleRouteSeed seed : seeds) {
            RoleRouteMapping mapping = roleRoutes.findFirstByRoleIdAndPatternAndHttpMethod(seed.roleId(), seed.pattern(), seed.httpMethod())
                    .orElseGet(() -> new RoleRouteMapping(
                            seed.roleId(),
                            seed.pattern(),
                            seed.httpMethod(),
                            seed.description(),
                            seed.enabled()
                    ));
            mapping.setDescription(seed.description());
            mapping.setEnabled(seed.enabled());
            roleRoutes.save(mapping);
        }
    }

    private Map<String, BatchJob> ensureBatchJobs(BatchJobRepository batchJobs) {
        List<BatchJobSeed> seeds = List.of(
                new BatchJobSeed("Daily access log rollup", "ACCESS_LOG_DAILY", "Summarize access logs daily", true),
                new BatchJobSeed("Weekly content audit", "CONTENT_AUDIT_WEEKLY", "Validate stale content every Monday", true),
                new BatchJobSeed("Menu usage report", "MENU_REPORT_WEEKLY", "Generate menu usage report", true)
        );

        Map<String, BatchJob> map = new LinkedHashMap<>();
        for (BatchJobSeed seed : seeds) {
            BatchJob job = batchJobs.findByJobKey(seed.jobKey())
                    .orElseGet(() -> new BatchJob(
                            seed.name(),
                            seed.jobKey(),
                            seed.description(),
                            seed.enabled()
                    ));
            job.setName(seed.name());
            job.setDescription(seed.description());
            job.setEnabled(seed.enabled());
            map.put(seed.jobKey(), batchJobs.save(job));
        }
        return map;
    }

    private void ensureBatchSchedules(BatchScheduleRepository batchSchedules, Map<String, BatchJob> batchJobMap) {
        List<BatchScheduleSeed> seeds = List.of(
                new BatchScheduleSeed(requiredBatchJobId(batchJobMap, "ACCESS_LOG_DAILY"), "0 0 2 * * *", "Asia/Seoul", true, "SUCCESS", "2026-02-07T02:00:00+09:00"),
                new BatchScheduleSeed(requiredBatchJobId(batchJobMap, "CONTENT_AUDIT_WEEKLY"), "0 30 3 * * MON", "Asia/Seoul", true, "SUCCESS", "2026-02-02T03:30:00+09:00"),
                new BatchScheduleSeed(requiredBatchJobId(batchJobMap, "MENU_REPORT_WEEKLY"), "0 0 4 * * MON", "Asia/Seoul", false, "IDLE", null)
        );

        for (BatchScheduleSeed seed : seeds) {
            BatchSchedule schedule = batchSchedules.findFirstByBatchJobIdAndCronExpression(seed.batchJobId(), seed.cronExpression())
                    .orElseGet(() -> new BatchSchedule(
                            seed.batchJobId(),
                            seed.cronExpression(),
                            seed.timezone(),
                            seed.enabled(),
                            seed.lastStatus(),
                            seed.lastRunAt()
                    ));
            schedule.setTimezone(seed.timezone());
            schedule.setEnabled(seed.enabled());
            schedule.setLastStatus(seed.lastStatus());
            schedule.setLastRunAt(seed.lastRunAt());
            batchSchedules.save(schedule);
        }
    }

    private void ensureBanners(BannerItemRepository banners) {
        List<BannerSeed> seeds = List.of(
                new BannerSeed("Maintenance Notice", "https://picsum.photos/1200/240", "https://example.com/maintenance", "2026-02-01T00:00:00Z", "2026-12-31T23:59:59Z", true, 10),
                new BannerSeed("New Dashboard", "https://picsum.photos/1200/241", "https://example.com/dashboard", "2026-02-01T00:00:00Z", null, true, 20),
                new BannerSeed("Policy Update", "https://picsum.photos/1200/242", "https://example.com/policy", "2026-02-01T00:00:00Z", null, false, 30)
        );

        for (BannerSeed seed : seeds) {
            BannerItem banner = banners.findFirstByTitle(seed.title())
                    .orElseGet(() -> new BannerItem(
                            seed.title(),
                            seed.imageUrl(),
                            seed.linkUrl(),
                            seed.startAt(),
                            seed.endAt(),
                            seed.enabled(),
                            seed.sortOrder()
                    ));
            banner.setImageUrl(seed.imageUrl());
            banner.setLinkUrl(seed.linkUrl());
            banner.setStartAt(seed.startAt());
            banner.setEndAt(seed.endAt());
            banner.setEnabled(seed.enabled());
            banner.setSortOrder(seed.sortOrder());
            banners.save(banner);
        }
    }

    private void ensureLoginPolicies(LoginPolicyRepository loginPolicies) {
        List<LoginPolicySeed> seeds = List.of(
                new LoginPolicySeed("Default Login Policy", 5, 30, "0.0.0.0/0", true),
                new LoginPolicySeed("Admin Strict Policy", 3, 60, "10.0.0.0/8", false)
        );

        for (LoginPolicySeed seed : seeds) {
            LoginPolicy policy = loginPolicies.findFirstByName(seed.name())
                    .orElseGet(() -> new LoginPolicy(
                            seed.name(),
                            seed.maxFailCount(),
                            seed.lockMinutes(),
                            seed.allowedIpCidr(),
                            seed.enabled()
                    ));
            policy.setMaxFailCount(seed.maxFailCount());
            policy.setLockMinutes(seed.lockMinutes());
            policy.setAllowedIpCidr(seed.allowedIpCidr());
            policy.setEnabled(seed.enabled());
            loginPolicies.save(policy);
        }
    }

    private void ensureAccessLogs(AccessLogRepository accessLogs) {
        if (accessLogs.count() > 0) {
            return;
        }
        accessLogs.save(new AccessLog("admin", "127.0.0.1", "LOGIN", "/auth/login", true));
        accessLogs.save(new AccessLog("tester", "127.0.0.1", "LOGIN", "/auth/login", true));
        accessLogs.save(new AccessLog("manager", "127.0.0.1", "LOGOUT", "/auth/logout", true));
    }

    private void ensureServiceAuditLogs(ServiceAuditLogRepository serviceAuditLogs) {
        if (serviceAuditLogs.count() > 0) {
            return;
        }
        serviceAuditLogs.save(new ServiceAuditLog("MENU", 1L, "UPDATE", "admin", "Updated menu sort order"));
        serviceAuditLogs.save(new ServiceAuditLog("CONTENT", 1L, "CREATE", "tester", "Created welcome content"));
        serviceAuditLogs.save(new ServiceAuditLog("LOGIN_POLICY", 1L, "UPDATE", "security", "Updated lock minutes"));
    }

    private Set<Permission> resolvePermissions(Map<String, Permission> permissionMap, List<String> permissionCodes) {
        Set<Permission> resolved = new LinkedHashSet<>();
        for (String code : permissionCodes) {
            Permission permission = permissionMap.get(code);
            if (permission == null) {
                throw new IllegalStateException("missing permission code: " + code);
            }
            resolved.add(permission);
        }
        return resolved;
    }

    private Role requiredRole(Map<String, Role> roleMap, String roleName) {
        Role role = roleMap.get(roleName);
        if (role == null) {
            throw new IllegalStateException("missing role: " + roleName);
        }
        return role;
    }

    private Long requiredBatchJobId(Map<String, BatchJob> batchJobMap, String batchJobKey) {
        BatchJob job = batchJobMap.get(batchJobKey);
        if (job == null || job.getId() == null) {
            throw new IllegalStateException("missing batch job: " + batchJobKey);
        }
        return job.getId();
    }

    private record PermissionSeed(String code, String description) {}

    private record RoleSeed(String name, String description, List<String> permissionCodes) {}

    private record MenuSeed(String title, String path, String parentPath, Integer sortOrder) {}

    private record ContentSeed(String title, String slug, String body, String status) {}

    private record CommonCodeSeed(String groupCode, String code, String name, String description, Integer sortOrder, Boolean enabled) {}

    private record ProgramSeed(String name, String url, String httpMethod, String description, Boolean enabled) {}

    private record RoleRouteSeed(Long roleId, String pattern, String httpMethod, String description, Boolean enabled) {}

    private record BatchJobSeed(String name, String jobKey, String description, Boolean enabled) {}

    private record BatchScheduleSeed(Long batchJobId, String cronExpression, String timezone, Boolean enabled, String lastStatus, String lastRunAt) {}

    private record BannerSeed(String title, String imageUrl, String linkUrl, String startAt, String endAt, Boolean enabled, Integer sortOrder) {}

    private record LoginPolicySeed(String name, Integer maxFailCount, Integer lockMinutes, String allowedIpCidr, Boolean enabled) {}
}
