SET search_path TO invoice_reimbursement;

CREATE TABLE IF NOT EXISTS tenants (
  id varchar(64) PRIMARY KEY,
  tenant_code varchar(64) NOT NULL,
  tenant_name varchar(128) NOT NULL,
  domain varchar(255),
  status varchar(32) NOT NULL DEFAULT 'ACTIVE',
  created_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  created_trace varchar(128),
  updated_at timestamptz NOT NULL DEFAULT now(),
  updated_by varchar(64),
  updated_trace varchar(128),
  deleted boolean NOT NULL DEFAULT false,
  CONSTRAINT ck_tenants_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_tenants_tenant_code ON tenants (tenant_code) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_tenants_status ON tenants (status) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS users (
  id varchar(64) PRIMARY KEY,
  tenant_id varchar(64) NOT NULL,
  keycloak_subject varchar(128) NOT NULL,
  username varchar(128) NOT NULL,
  display_name varchar(128) NOT NULL,
  email varchar(255) NOT NULL,
  department_id varchar(64),
  source_type varchar(32) NOT NULL,
  enabled boolean NOT NULL DEFAULT true,
  last_login_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  created_trace varchar(128),
  updated_at timestamptz NOT NULL DEFAULT now(),
  updated_by varchar(64),
  updated_trace varchar(128),
  deleted boolean NOT NULL DEFAULT false,
  CONSTRAINT ck_users_source_type CHECK (source_type IN ('LOCAL', 'SSO', 'JIT'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_tenant_subject ON users (tenant_id, keycloak_subject) WHERE deleted = false;
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_tenant_email ON users (tenant_id, email) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_users_tenant_department ON users (tenant_id, department_id) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_users_tenant_enabled ON users (tenant_id, enabled) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS roles (
  id varchar(64) PRIMARY KEY,
  tenant_id varchar(64),
  role_code varchar(64) NOT NULL,
  role_name varchar(128) NOT NULL,
  description varchar(512),
  system_role boolean NOT NULL DEFAULT false,
  sensitive_role boolean NOT NULL DEFAULT false,
  created_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  created_trace varchar(128),
  updated_at timestamptz NOT NULL DEFAULT now(),
  updated_by varchar(64),
  updated_trace varchar(128),
  deleted boolean NOT NULL DEFAULT false
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_roles_tenant_code ON roles (COALESCE(tenant_id, 'GLOBAL'), role_code) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS permissions (
  id varchar(64) PRIMARY KEY,
  tenant_id varchar(64),
  permission_code varchar(128) NOT NULL,
  permission_name varchar(128) NOT NULL,
  description varchar(512),
  created_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  created_trace varchar(128),
  updated_at timestamptz NOT NULL DEFAULT now(),
  updated_by varchar(64),
  updated_trace varchar(128),
  deleted boolean NOT NULL DEFAULT false
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_permissions_tenant_code ON permissions (COALESCE(tenant_id, 'GLOBAL'), permission_code) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS role_permissions (
  id varchar(64) PRIMARY KEY,
  tenant_id varchar(64),
  role_id varchar(64) NOT NULL,
  permission_id varchar(64) NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  created_trace varchar(128),
  updated_at timestamptz NOT NULL DEFAULT now(),
  updated_by varchar(64),
  updated_trace varchar(128),
  deleted boolean NOT NULL DEFAULT false
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_role_permissions ON role_permissions (role_id, permission_id) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS user_roles (
  id varchar(64) PRIMARY KEY,
  tenant_id varchar(64) NOT NULL,
  user_id varchar(64) NOT NULL,
  role_id varchar(64) NOT NULL,
  granted_by varchar(64),
  granted_at timestamptz NOT NULL DEFAULT now(),
  grant_reason varchar(512),
  created_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  created_trace varchar(128),
  updated_at timestamptz NOT NULL DEFAULT now(),
  updated_by varchar(64),
  updated_trace varchar(128),
  deleted boolean NOT NULL DEFAULT false
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_roles ON user_roles (tenant_id, user_id, role_id) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_user_roles_user ON user_roles (tenant_id, user_id) WHERE deleted = false;

DROP TRIGGER IF EXISTS trg_tenants_updated_at ON tenants;
CREATE TRIGGER trg_tenants_updated_at BEFORE UPDATE ON tenants FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_roles_updated_at ON roles;
CREATE TRIGGER trg_roles_updated_at BEFORE UPDATE ON roles FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_permissions_updated_at ON permissions;
CREATE TRIGGER trg_permissions_updated_at BEFORE UPDATE ON permissions FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_role_permissions_updated_at ON role_permissions;
CREATE TRIGGER trg_role_permissions_updated_at BEFORE UPDATE ON role_permissions FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_user_roles_updated_at ON user_roles;
CREATE TRIGGER trg_user_roles_updated_at BEFORE UPDATE ON user_roles FOR EACH ROW EXECUTE FUNCTION set_updated_at();
