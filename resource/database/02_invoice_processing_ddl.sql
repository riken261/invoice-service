SET search_path TO invoice_reimbursement;

CREATE TABLE IF NOT EXISTS invoice_files (
  id varchar(64) PRIMARY KEY,
  tenant_id varchar(64) NOT NULL,
  owner_user_id varchar(64) NOT NULL,
  storage_type varchar(32) NOT NULL,
  bucket varchar(255),
  object_key varchar(512) NOT NULL,
  original_filename varchar(255) NOT NULL,
  content_type varchar(128) NOT NULL,
  size_bytes bigint NOT NULL,
  sha256 varchar(64) NOT NULL,
  file_status varchar(32) NOT NULL DEFAULT 'ACTIVE',
  created_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  created_trace varchar(128),
  updated_at timestamptz NOT NULL DEFAULT now(),
  updated_by varchar(64),
  updated_trace varchar(128),
  deleted boolean NOT NULL DEFAULT false,
  CONSTRAINT ck_invoice_files_storage_type CHECK (storage_type IN ('LOCAL', 'S3', 'OSS', 'MINIO')),
  CONSTRAINT ck_invoice_files_status CHECK (file_status IN ('TEMPORARY', 'ACTIVE', 'ORPHANED', 'ARCHIVED')),
  CONSTRAINT ck_invoice_files_size CHECK (size_bytes > 0)
);

CREATE INDEX IF NOT EXISTS idx_invoice_files_owner ON invoice_files (tenant_id, owner_user_id, created_at DESC) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_invoice_files_sha256 ON invoice_files (tenant_id, sha256) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS invoices (
  id varchar(64) PRIMARY KEY,
  tenant_id varchar(64) NOT NULL,
  invoice_file_id varchar(64) NOT NULL,
  owner_user_id varchar(64) NOT NULL,
  invoice_type varchar(64) NOT NULL,
  invoice_code varchar(64),
  invoice_number varchar(64),
  issue_date date,
  seller_name varchar(255),
  buyer_name varchar(255),
  amount_without_tax numeric(18,2),
  tax_amount numeric(18,2),
  total_amount numeric(18,2),
  currency varchar(8) NOT NULL DEFAULT 'CNY',
  ocr_status varchar(32) NOT NULL DEFAULT 'OCR_SUCCESS',
  invoice_status varchar(32) NOT NULL DEFAULT 'USER_CONFIRMED',
  duplicate_status varchar(32) NOT NULL DEFAULT 'NO_DUPLICATE',
  manual_input boolean NOT NULL DEFAULT false,
  ocr_fields jsonb,
  confirmed_fields jsonb,
  manual_fields jsonb,
  latest_review_opinion varchar(1000),
  created_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  created_trace varchar(128),
  updated_at timestamptz NOT NULL DEFAULT now(),
  updated_by varchar(64),
  updated_trace varchar(128),
  deleted boolean NOT NULL DEFAULT false,
  CONSTRAINT ck_invoices_ocr_status CHECK (ocr_status IN ('OCR_SUCCESS', 'OCR_FAILED')),
  CONSTRAINT ck_invoices_invoice_status CHECK (invoice_status IN ('USER_CONFIRMED', 'MANUAL_REVIEW_REQUIRED', 'FINANCE_APPROVED', 'FINANCE_REJECTED', 'CLAIM_SUBMITTED', 'REIMBURSED', 'ARCHIVED')),
  CONSTRAINT ck_invoices_duplicate_status CHECK (duplicate_status IN ('NO_DUPLICATE', 'POSSIBLE_DUPLICATE', 'CONFIRMED_DUPLICATE')),
  CONSTRAINT ck_invoices_amount CHECK (total_amount IS NULL OR total_amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_invoices_owner_created ON invoices (tenant_id, owner_user_id, created_at DESC) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_invoices_status_created ON invoices (tenant_id, invoice_status, created_at DESC) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_invoices_duplicate_created ON invoices (tenant_id, duplicate_status, created_at DESC) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_invoices_invoice_identity ON invoices (tenant_id, invoice_code, invoice_number, issue_date, total_amount) WHERE deleted = false;
CREATE UNIQUE INDEX IF NOT EXISTS uk_invoices_file ON invoices (invoice_file_id) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS ocr_recognition_sessions (
  id varchar(64) PRIMARY KEY,
  batch_id varchar(64),
  tenant_id varchar(64) NOT NULL,
  invoice_file_id varchar(64) NOT NULL,
  owner_user_id varchar(64) NOT NULL,
  idempotency_key varchar(128) NOT NULL,
  source varchar(32) NOT NULL,
  provider varchar(64) NOT NULL,
  status varchar(32) NOT NULL DEFAULT 'PROCESSING',
  raw_request_id varchar(128),
  raw_result jsonb,
  normalized_result jsonb,
  error_code varchar(128),
  error_message varchar(1000),
  started_at timestamptz,
  finished_at timestamptz,
  expires_at timestamptz NOT NULL,
  submitted_invoice_id varchar(64),
  created_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  created_trace varchar(128),
  updated_at timestamptz NOT NULL DEFAULT now(),
  updated_by varchar(64),
  updated_trace varchar(128),
  deleted boolean NOT NULL DEFAULT false,
  CONSTRAINT ck_ocr_recognition_sessions_source CHECK (source IN ('CAMERA', 'ALBUM', 'LOCAL_FILE')),
  CONSTRAINT ck_ocr_recognition_sessions_status CHECK (status IN ('PROCESSING', 'OCR_SUCCESS', 'LOW_CONFIDENCE', 'OCR_FAILED', 'SUBMITTED', 'EXPIRED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_ocr_recognition_sessions_idempotency ON ocr_recognition_sessions (tenant_id, invoice_file_id, idempotency_key) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_ocr_recognition_sessions_batch ON ocr_recognition_sessions (tenant_id, batch_id, created_at DESC) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_ocr_recognition_sessions_file ON ocr_recognition_sessions (tenant_id, invoice_file_id) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_ocr_recognition_sessions_owner ON ocr_recognition_sessions (tenant_id, owner_user_id, created_at DESC) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_ocr_recognition_sessions_expired ON ocr_recognition_sessions (status, expires_at) WHERE deleted = false;

DROP TRIGGER IF EXISTS trg_invoice_files_updated_at ON invoice_files;
CREATE TRIGGER trg_invoice_files_updated_at BEFORE UPDATE ON invoice_files FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_invoices_updated_at ON invoices;
CREATE TRIGGER trg_invoices_updated_at BEFORE UPDATE ON invoices FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_ocr_recognition_sessions_updated_at ON ocr_recognition_sessions;
CREATE TRIGGER trg_ocr_recognition_sessions_updated_at BEFORE UPDATE ON ocr_recognition_sessions FOR EACH ROW EXECUTE FUNCTION set_updated_at();
