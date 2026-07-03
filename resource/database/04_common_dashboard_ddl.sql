SET search_path TO invoice_reimbursement;

CREATE TABLE IF NOT EXISTS dashboard_actions (
  id varchar(64) PRIMARY KEY,
  action_code varchar(128) NOT NULL,
  action_name varchar(128) NOT NULL,
  group_code varchar(128) NOT NULL,
  group_name varchar(128) NOT NULL,
  router_path varchar(255) NOT NULL,
  permission_code varchar(128),
  i18n_key varchar(128) NOT NULL,
  badge_code varchar(64),
  group_sort_order integer NOT NULL DEFAULT 0,
  action_sort_order integer NOT NULL DEFAULT 0,
  enabled boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  created_trace varchar(128),
  updated_at timestamptz NOT NULL DEFAULT now(),
  updated_by varchar(64),
  updated_trace varchar(128),
  deleted boolean NOT NULL DEFAULT false
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_dashboard_actions_code ON dashboard_actions (action_code) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_dashboard_actions_enabled ON dashboard_actions (enabled, group_code, action_code) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_dashboard_actions_sort ON dashboard_actions (group_sort_order, action_sort_order) WHERE deleted = false AND enabled = true;

DROP TRIGGER IF EXISTS trg_dashboard_actions_updated_at ON dashboard_actions;
CREATE TRIGGER trg_dashboard_actions_updated_at BEFORE UPDATE ON dashboard_actions FOR EACH ROW EXECUTE FUNCTION set_updated_at();
