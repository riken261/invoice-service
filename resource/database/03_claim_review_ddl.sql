SET search_path TO invoice_reimbursement;

CREATE TABLE IF NOT EXISTS expense_claims (
  id varchar(64) PRIMARY KEY,
  tenant_id varchar(64) NOT NULL,
  claim_no varchar(64) NOT NULL,
  applicant_user_id varchar(64) NOT NULL,
  department_id varchar(64),
  description varchar(1000),
  total_amount numeric(18,2) NOT NULL DEFAULT 0,
  currency varchar(8) NOT NULL DEFAULT 'CNY',
  status varchar(32) NOT NULL DEFAULT 'DRAFT',
  submitted_at timestamptz,
  approved_at timestamptz,
  paid_at timestamptz,
  archived_at timestamptz,
  latest_review_opinion varchar(1000),
  created_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  created_trace varchar(128),
  updated_at timestamptz NOT NULL DEFAULT now(),
  updated_by varchar(64),
  updated_trace varchar(128),
  deleted boolean NOT NULL DEFAULT false,
  CONSTRAINT ck_expense_claims_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'APPROVING', 'FINANCE_REVIEWING', 'APPROVED', 'REJECTED', 'PAID', 'ARCHIVED')),
  CONSTRAINT ck_expense_claims_amount CHECK (total_amount >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_expense_claims_no ON expense_claims (tenant_id, claim_no) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_expense_claims_applicant ON expense_claims (tenant_id, applicant_user_id, created_at DESC) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_expense_claims_status ON expense_claims (tenant_id, status, created_at DESC) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS expense_claim_items (
  id varchar(64) PRIMARY KEY,
  tenant_id varchar(64) NOT NULL,
  claim_id varchar(64) NOT NULL,
  invoice_id varchar(64) NOT NULL,
  expense_category varchar(64) NOT NULL,
  amount numeric(18,2) NOT NULL,
  description varchar(1000),
  created_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  created_trace varchar(128),
  updated_at timestamptz NOT NULL DEFAULT now(),
  updated_by varchar(64),
  updated_trace varchar(128),
  deleted boolean NOT NULL DEFAULT false,
  CONSTRAINT ck_expense_claim_items_amount CHECK (amount >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_claim_items_invoice ON expense_claim_items (tenant_id, claim_id, invoice_id) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_claim_items_claim ON expense_claim_items (tenant_id, claim_id) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_claim_items_invoice ON expense_claim_items (tenant_id, invoice_id) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS review_records (
  id varchar(64) PRIMARY KEY,
  tenant_id varchar(64) NOT NULL,
  resource_type varchar(32) NOT NULL,
  resource_id varchar(64) NOT NULL,
  reviewer_user_id varchar(64) NOT NULL,
  action varchar(32) NOT NULL,
  opinion varchar(1000),
  before_status varchar(32),
  after_status varchar(32),
  created_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  created_trace varchar(128),
  updated_at timestamptz NOT NULL DEFAULT now(),
  updated_by varchar(64),
  updated_trace varchar(128),
  deleted boolean NOT NULL DEFAULT false,
  CONSTRAINT ck_review_records_resource_type CHECK (resource_type IN ('INVOICE', 'EXPENSE_CLAIM')),
  CONSTRAINT ck_review_records_action CHECK (action IN ('SUBMIT', 'APPROVE', 'REJECT'))
);

CREATE INDEX IF NOT EXISTS idx_review_records_resource ON review_records (tenant_id, resource_type, resource_id, created_at DESC) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_review_records_reviewer ON review_records (tenant_id, reviewer_user_id, created_at DESC) WHERE deleted = false;

DROP TRIGGER IF EXISTS trg_expense_claims_updated_at ON expense_claims;
CREATE TRIGGER trg_expense_claims_updated_at BEFORE UPDATE ON expense_claims FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_expense_claim_items_updated_at ON expense_claim_items;
CREATE TRIGGER trg_expense_claim_items_updated_at BEFORE UPDATE ON expense_claim_items FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_review_records_updated_at ON review_records;
CREATE TRIGGER trg_review_records_updated_at BEFORE UPDATE ON review_records FOR EACH ROW EXECUTE FUNCTION set_updated_at();
