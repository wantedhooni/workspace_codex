ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP(6) NULL;
ALTER TABLE users ADD COLUMN password_hash VARCHAR(255) NULL;

CREATE TABLE IF NOT EXISTS user_password_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(120) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT fk_uph_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_uph_user_created_at ON user_password_histories(user_id, created_at DESC);

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(40) NOT NULL,
    role_name VARCHAR(80) NOT NULL,
    description VARCHAR(255) NOT NULL,
    system_role BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(120) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT uk_roles_role_code UNIQUE (role_code)
);

CREATE TABLE IF NOT EXISTS menus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_menu_id BIGINT NULL,
    menu_key VARCHAR(80) NOT NULL,
    menu_label VARCHAR(120) NOT NULL,
    path VARCHAR(255) NOT NULL,
    icon VARCHAR(80) NOT NULL,
    sort_order INT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(120) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT uk_menus_menu_key UNIQUE (menu_key),
    CONSTRAINT fk_menus_parent FOREIGN KEY (parent_menu_id) REFERENCES menus(id)
);

CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(120) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT uk_user_roles_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

CREATE TABLE IF NOT EXISTS menu_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    can_read BOOLEAN NOT NULL DEFAULT FALSE,
    can_create BOOLEAN NOT NULL DEFAULT FALSE,
    can_update BOOLEAN NOT NULL DEFAULT FALSE,
    can_delete BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(120) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT uk_menu_permissions_menu_role UNIQUE (menu_id, role_id),
    CONSTRAINT fk_menu_permissions_menu FOREIGN KEY (menu_id) REFERENCES menus(id),
    CONSTRAINT fk_menu_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE INDEX IF NOT EXISTS idx_menu_permissions_role_id ON menu_permissions(role_id);

CREATE TABLE IF NOT EXISTS account_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ip_address VARCHAR(80) NOT NULL,
    user_agent VARCHAR(300) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_access_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(120) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT fk_account_sessions_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_account_sessions_user_active ON account_sessions(user_id, active);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    used_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(120) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT uk_password_reset_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user_expires ON password_reset_tokens(user_id, expires_at);
