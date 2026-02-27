INSERT INTO portfolios (id, code, name, currency, created_at, created_by, updated_by)
SELECT 1, 'US_MOM_A', 'US Momentum Core', 'USD', CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM portfolios WHERE id = 1);

INSERT INTO portfolios (id, code, name, currency, created_at, created_by, updated_by)
SELECT 2, 'US_VAL_B', 'US Value Quality', 'USD', CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM portfolios WHERE id = 2);

INSERT INTO portfolios (id, code, name, currency, created_at, created_by, updated_by)
SELECT 3, 'US_IDX_H', 'US Index Hedge', 'USD', CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM portfolios WHERE id = 3);

INSERT INTO portfolios (id, code, name, currency, created_at, created_by, updated_by)
SELECT 4, 'US_SEMI_X', 'US Semiconductor Alpha', 'USD', CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM portfolios WHERE id = 4);

INSERT INTO users (id, email, name, status, password_hash, last_login_at, created_at, updated_at, created_by, updated_by)
SELECT 1, 'admin@quant.io', 'System Admin', 'ACTIVE', '$2a$10$k9V2uKKgujOA7EU/5vfgZO2sYfOQqBDZi36BPBPaMg6JqIPQd.oey',
       CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 1);

INSERT INTO users (id, email, name, status, password_hash, last_login_at, created_at, updated_at, created_by, updated_by)
SELECT 2, 'trader@quant.io', 'Execution Trader', 'ACTIVE', '$2a$10$amYSBjNy8jA8IdIo8mXgp.QZzg76nlrtWpMKWJ6VLBtGRysJxddg.',
       CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 2);

INSERT INTO users (id, email, name, status, password_hash, last_login_at, created_at, updated_at, created_by, updated_by)
SELECT 3, 'risk@quant.io', 'Risk Officer', 'ACTIVE', '$2a$10$vwofFQLkZgEeZ.7PMZmEzu20OvLxPUa530C4e/AcQ1lJrEm1YpUpG',
       CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 3);

INSERT INTO users (id, email, name, status, password_hash, last_login_at, created_at, updated_at, created_by, updated_by)
SELECT 4, 'viewer@quant.io', 'Read Only', 'ACTIVE', '$2a$10$t.86NGjfF..PFPmoX1D4UeMZUcd3lf5h.zVpsdMtSDS13HdsGDaDa',
       CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 4);

INSERT INTO user_password_histories (id, user_id, password_hash, created_at, updated_at, created_by, updated_by)
SELECT 1, 1, '$2a$10$k9V2uKKgujOA7EU/5vfgZO2sYfOQqBDZi36BPBPaMg6JqIPQd.oey', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM user_password_histories WHERE id = 1);

INSERT INTO user_password_histories (id, user_id, password_hash, created_at, updated_at, created_by, updated_by)
SELECT 2, 2, '$2a$10$amYSBjNy8jA8IdIo8mXgp.QZzg76nlrtWpMKWJ6VLBtGRysJxddg.', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM user_password_histories WHERE id = 2);

INSERT INTO user_password_histories (id, user_id, password_hash, created_at, updated_at, created_by, updated_by)
SELECT 3, 3, '$2a$10$vwofFQLkZgEeZ.7PMZmEzu20OvLxPUa530C4e/AcQ1lJrEm1YpUpG', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM user_password_histories WHERE id = 3);

INSERT INTO user_password_histories (id, user_id, password_hash, created_at, updated_at, created_by, updated_by)
SELECT 4, 4, '$2a$10$t.86NGjfF..PFPmoX1D4UeMZUcd3lf5h.zVpsdMtSDS13HdsGDaDa', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM user_password_histories WHERE id = 4);

INSERT INTO roles (id, role_code, role_name, description, system_role, created_at, updated_at, created_by, updated_by)
SELECT 1, 'ADMIN', 'Administrator', 'Full admin permission', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE id = 1);

INSERT INTO roles (id, role_code, role_name, description, system_role, created_at, updated_at, created_by, updated_by)
SELECT 2, 'QUANT', 'Quant Trader', 'Quant and execution operator', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE id = 2);

INSERT INTO roles (id, role_code, role_name, description, system_role, created_at, updated_at, created_by, updated_by)
SELECT 3, 'VIEWER', 'Viewer', 'Read only user', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE id = 3);

INSERT INTO roles (id, role_code, role_name, description, system_role, created_at, updated_at, created_by, updated_by)
SELECT 4, 'RISK', 'Risk Officer', 'Risk control user', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE id = 4);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 1, NULL, 'dashboard', 'Dashboard', '/#/', 'dashboard', 10, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 1);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 2, NULL, 'orders', 'Orders', '/#/orders', 'receipt', 20, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 2);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 15, NULL, 'orderAudits', 'Order Audits', '/#/orderAudits', 'history', 25, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 15);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 3, NULL, 'trades', 'Trades', '/#/trades', 'paid', 30, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 3);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 4, NULL, 'positions', 'Positions', '/#/positions', 'analytics', 40, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 4);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 19, NULL, 'portfolios', 'Portfolios', '/#/portfolios', 'folder_shared', 45, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 19);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 20, NULL, 'savedViews', 'Saved Views', '/#/savedViews', 'view_list', 47, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 20);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 5, NULL, 'portfolioSummaries', 'Portfolio Summaries', '/#/portfolioSummaries', 'insights', 50, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 5);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 18, NULL, 'orderHealth', 'Order Health', '/#/orderHealth', 'monitor_heart', 52, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 18);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 16, NULL, 'riskAlerts', 'Risk Alerts', '/#/riskAlerts', 'warning', 55, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 16);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 17, NULL, 'executionQualities', 'Execution Qualities', '/#/executionQualities', 'speed', 58, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 17);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 6, NULL, 'riskLimits', 'Risk Limits', '/#/riskLimits', 'shield', 60, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 6);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 7, NULL, 'journalVouchers', 'Journal Vouchers', '/#/journalVouchers', 'description', 70, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 7);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 8, NULL, 'ledgerEntries', 'Ledger Entries', '/#/ledgerEntries', 'book', 80, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 8);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 9, NULL, 'users', 'Users', '/#/users', 'people', 90, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 9);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 10, NULL, 'roles', 'Roles', '/#/roles', 'security', 100, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 10);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 11, NULL, 'menus', 'Menus', '/#/menus', 'menu', 110, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 11);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 12, NULL, 'menuPermissions', 'Menu Permissions', '/#/menuPermissions', 'rule', 120, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 12);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 13, NULL, 'accountProfile', 'Account Profile', '/#/accountProfile', 'person', 130, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 13);

INSERT INTO menus (id, parent_menu_id, menu_key, menu_label, path, icon, sort_order, enabled, created_at, updated_at, created_by, updated_by)
SELECT 14, NULL, 'accountSessions', 'Account Sessions', '/#/accountSessions', 'devices', 140, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE id = 14);

INSERT INTO user_roles (id, user_id, role_id, created_at, updated_at, created_by, updated_by)
SELECT id, user_id, role_id, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
FROM (
    SELECT 1 AS id, 1 AS user_id, 1 AS role_id
    UNION ALL SELECT 2, 2, 2
    UNION ALL SELECT 3, 3, 4
    UNION ALL SELECT 4, 3, 3
    UNION ALL SELECT 5, 4, 3
) seed
WHERE NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.id = seed.id);

INSERT INTO menu_permissions (id, menu_id, role_id, can_read, can_create, can_update, can_delete, created_at, updated_at, created_by, updated_by)
SELECT id, menu_id, role_id, can_read, can_create, can_update, can_delete, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
FROM (
    SELECT 1 AS id, 1 AS menu_id, 1 AS role_id, TRUE AS can_read, TRUE AS can_create, TRUE AS can_update, TRUE AS can_delete
    UNION ALL SELECT 2, 2, 1, TRUE, TRUE, TRUE, TRUE
    UNION ALL SELECT 3, 3, 1, TRUE, TRUE, TRUE, TRUE
    UNION ALL SELECT 4, 4, 1, TRUE, TRUE, TRUE, TRUE
    UNION ALL SELECT 5, 5, 1, TRUE, TRUE, TRUE, TRUE
    UNION ALL SELECT 6, 6, 1, TRUE, TRUE, TRUE, TRUE
    UNION ALL SELECT 7, 7, 1, TRUE, TRUE, TRUE, TRUE
    UNION ALL SELECT 8, 8, 1, TRUE, TRUE, TRUE, TRUE
    UNION ALL SELECT 9, 9, 1, TRUE, TRUE, TRUE, TRUE
    UNION ALL SELECT 10, 10, 1, TRUE, TRUE, TRUE, TRUE
    UNION ALL SELECT 11, 11, 1, TRUE, TRUE, TRUE, TRUE
    UNION ALL SELECT 12, 12, 1, TRUE, TRUE, TRUE, TRUE
    UNION ALL SELECT 13, 13, 1, TRUE, TRUE, TRUE, TRUE
    UNION ALL SELECT 14, 14, 1, TRUE, TRUE, TRUE, TRUE
    UNION ALL SELECT 15, 1, 2, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 16, 2, 2, TRUE, TRUE, FALSE, FALSE
    UNION ALL SELECT 17, 3, 2, TRUE, TRUE, FALSE, FALSE
    UNION ALL SELECT 18, 4, 2, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 19, 5, 2, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 20, 7, 2, TRUE, TRUE, FALSE, FALSE
    UNION ALL SELECT 21, 8, 2, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 22, 13, 2, TRUE, TRUE, TRUE, FALSE
    UNION ALL SELECT 23, 14, 2, TRUE, TRUE, TRUE, FALSE
    UNION ALL SELECT 24, 1, 3, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 25, 5, 3, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 26, 4, 3, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 27, 13, 3, TRUE, TRUE, FALSE, FALSE
    UNION ALL SELECT 28, 14, 3, TRUE, TRUE, FALSE, FALSE
    UNION ALL SELECT 29, 6, 4, TRUE, TRUE, TRUE, FALSE
    UNION ALL SELECT 30, 5, 4, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 31, 13, 4, TRUE, TRUE, TRUE, FALSE
    UNION ALL SELECT 32, 14, 4, TRUE, TRUE, TRUE, FALSE
    UNION ALL SELECT 33, 15, 1, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 34, 15, 2, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 35, 15, 4, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 36, 16, 1, TRUE, FALSE, TRUE, FALSE
    UNION ALL SELECT 37, 16, 2, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 38, 16, 3, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 39, 16, 4, TRUE, FALSE, TRUE, FALSE
    UNION ALL SELECT 40, 17, 1, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 41, 17, 2, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 42, 17, 3, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 43, 17, 4, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 44, 18, 1, TRUE, FALSE, TRUE, FALSE
    UNION ALL SELECT 45, 18, 2, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 46, 18, 3, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 47, 18, 4, TRUE, FALSE, TRUE, FALSE
    UNION ALL SELECT 48, 19, 1, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 49, 19, 2, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 50, 19, 3, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 51, 19, 4, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 52, 20, 1, TRUE, TRUE, TRUE, TRUE
    UNION ALL SELECT 53, 20, 2, TRUE, TRUE, FALSE, TRUE
    UNION ALL SELECT 54, 20, 3, TRUE, FALSE, FALSE, FALSE
    UNION ALL SELECT 55, 20, 4, TRUE, TRUE, FALSE, TRUE
) seed
WHERE NOT EXISTS (SELECT 1 FROM menu_permissions mp WHERE mp.id = seed.id);

INSERT INTO account_sessions (id, user_id, ip_address, user_agent, active, last_access_at, created_at, updated_at, created_by, updated_by)
SELECT 1, 1, '127.0.0.1', 'Chrome Local', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM account_sessions WHERE id = 1);

INSERT INTO account_sessions (id, user_id, ip_address, user_agent, active, last_access_at, created_at, updated_at, created_by, updated_by)
SELECT 2, 1, '10.0.0.41', 'MacBook Safari', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM account_sessions WHERE id = 2);

INSERT INTO account_sessions (id, user_id, ip_address, user_agent, active, last_access_at, created_at, updated_at, created_by, updated_by)
SELECT 3, 1, '10.0.0.99', 'Old Session', FALSE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM account_sessions WHERE id = 3);
