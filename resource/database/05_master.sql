SET search_path TO invoice_reimbursement;

INSERT INTO tenants (
  id, tenant_code, tenant_name, domain, status, created_by, updated_by
) VALUES (
  'ichigo',
  'ichigo',
  'ichigo',
  'ichigo.local',
  'ACTIVE',
  'SYSTEM',
  'SYSTEM'
) ON CONFLICT DO NOTHING;

INSERT INTO permissions (
  id, tenant_id, permission_code, permission_name, description,
  created_by, updated_by
) VALUES
  ('perm_invoice_upload', NULL, 'invoice:upload', 'Invoice upload', 'Allow uploading invoice files', 'SYSTEM', 'SYSTEM'),
  ('perm_invoice_view_self', NULL, 'invoice:view:self', 'View own invoices', 'Allow viewing own invoices', 'SYSTEM', 'SYSTEM'),
  ('perm_invoice_view_department', NULL, 'invoice:view:department', 'View department invoices', 'Allow viewing department invoices', 'SYSTEM', 'SYSTEM'),
  ('perm_invoice_view_all', NULL, 'invoice:view:all', 'View all invoices', 'Allow viewing all invoices', 'SYSTEM', 'SYSTEM'),
  ('perm_invoice_review', NULL, 'invoice:review', 'Review invoices', 'Allow finance invoice review', 'SYSTEM', 'SYSTEM'),
  ('perm_invoice_download', NULL, 'invoice:download', 'Download invoice files', 'Allow downloading invoice attachments', 'SYSTEM', 'SYSTEM'),
  ('perm_claim_create', NULL, 'claim:create', 'Create claims', 'Allow creating expense claims', 'SYSTEM', 'SYSTEM'),
  ('perm_claim_submit', NULL, 'claim:submit', 'Submit claims', 'Allow submitting expense claims', 'SYSTEM', 'SYSTEM'),
  ('perm_claim_approve', NULL, 'claim:approve', 'Approve claims', 'Allow claim approval', 'SYSTEM', 'SYSTEM'),
  ('perm_claim_finance_review', NULL, 'claim:finance-review', 'Finance claim review', 'Allow finance review for claims', 'SYSTEM', 'SYSTEM'),
  ('perm_claim_pay', NULL, 'claim:pay', 'Confirm payment', 'Allow payment confirmation', 'SYSTEM', 'SYSTEM'),
  ('perm_audit_view', NULL, 'audit:view', 'View audit logs', 'Allow viewing audit logs', 'SYSTEM', 'SYSTEM'),
  ('perm_system_config', NULL, 'system:config', 'System config', 'Allow managing system config', 'SYSTEM', 'SYSTEM'),
  ('perm_user_manage', NULL, 'user:manage', 'User management', 'Allow managing users and roles', 'SYSTEM', 'SYSTEM')
ON CONFLICT DO NOTHING;

INSERT INTO roles (
  id, tenant_id, role_code, role_name, description, system_role, sensitive_role,
  created_by, updated_by
) VALUES
  ('role_employee', NULL, 'EMPLOYEE', 'Employee', 'Upload invoices and create or submit own claims', true, false, 'SYSTEM', 'SYSTEM'),
  ('role_finance_reviewer', NULL, 'FINANCE_REVIEWER', 'Finance reviewer', 'Employee permissions plus invoice and claim finance review', true, true, 'SYSTEM', 'SYSTEM'),
  ('role_system_admin', NULL, 'SYSTEM_ADMIN', 'System administrator', 'Employee permissions plus user, role, config and event management', true, true, 'SYSTEM', 'SYSTEM'),
  ('role_auditor', NULL, 'AUDITOR', 'Auditor', 'Employee permissions plus audit and business record viewing', true, true, 'SYSTEM', 'SYSTEM')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (id, tenant_id, role_id, permission_id, created_by, updated_by)
SELECT 'rp_employee_' || p.id, NULL, 'role_employee', p.id, 'SYSTEM', 'SYSTEM'
FROM permissions p
WHERE p.permission_code IN (
  'invoice:upload',
  'invoice:view:self',
  'invoice:download',
  'claim:create',
  'claim:submit'
)
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (id, tenant_id, role_id, permission_id, created_by, updated_by)
SELECT 'rp_finance_' || p.id, NULL, 'role_finance_reviewer', p.id, 'SYSTEM', 'SYSTEM'
FROM permissions p
WHERE p.permission_code IN (
  'invoice:upload',
  'invoice:view:self',
  'invoice:view:all',
  'invoice:review',
  'invoice:download',
  'claim:create',
  'claim:submit',
  'claim:finance-review'
)
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (id, tenant_id, role_id, permission_id, created_by, updated_by)
SELECT 'rp_admin_' || p.id, NULL, 'role_system_admin', p.id, 'SYSTEM', 'SYSTEM'
FROM permissions p
WHERE p.permission_code IN (
  'invoice:upload',
  'invoice:view:self',
  'invoice:download',
  'claim:create',
  'claim:submit',
  'system:config',
  'user:manage',
  'audit:view'
)
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (id, tenant_id, role_id, permission_id, created_by, updated_by)
SELECT 'rp_auditor_' || p.id, NULL, 'role_auditor', p.id, 'SYSTEM', 'SYSTEM'
FROM permissions p
WHERE p.permission_code IN (
  'audit:view',
  'invoice:upload',
  'invoice:view:self',
  'invoice:view:all',
  'invoice:download',
  'claim:create',
  'claim:submit'
)
ON CONFLICT DO NOTHING;

INSERT INTO dashboard_actions (
  id, action_code, action_name, group_code, group_name, router_path,
  permission_code, i18n_key, badge_code, group_sort_order, action_sort_order,
  enabled, created_by, updated_by
) VALUES
  ('dash_invoice_preview', 'dashboard.action.invoice.preview', 'Preview', 'dashboard.group.invoice', 'Invoices', '/invoices', 'invoice:view:self', 'dashboard.action.invoice.preview', 'NONE', 10, 10, true, 'SYSTEM', 'SYSTEM'),
  ('dash_invoice_submit', 'dashboard.action.invoice.submit', 'Upload', 'dashboard.group.invoice', 'Invoices', '/invoice/upload', 'invoice:upload', 'dashboard.action.invoice.submit', 'NONE', 10, 20, true, 'SYSTEM', 'SYSTEM'),
  ('dash_claim_apply', 'dashboard.action.claim.apply', 'Apply', 'dashboard.group.claim', 'Claims', '/claims/new', 'claim:create', 'dashboard.action.claim.apply', 'NONE', 20, 10, true, 'SYSTEM', 'SYSTEM'),
  ('dash_claim_query', 'dashboard.action.claim.query', 'Query', 'dashboard.group.claim', 'Claims', '/claims', 'claim:create', 'dashboard.action.claim.query', 'NONE', 20, 20, true, 'SYSTEM', 'SYSTEM'),
  ('dash_claim_review', 'dashboard.action.claim.review', 'Review', 'dashboard.group.claim', 'Claims', '/finance/claims', 'claim:finance-review', 'dashboard.action.claim.review', 'PENDING_CLAIMS', 20, 30, true, 'SYSTEM', 'SYSTEM'),
  ('dash_admin_outbox_dead_letter', 'dashboard.action.admin.outboxDeadLetter', 'Outbox dead letter', 'dashboard.group.admin', 'Admin', '/admin/outbox-events?status=DEAD_LETTER', 'system:config', 'dashboard.action.admin.outboxDeadLetter', 'OUTBOX_DEAD_LETTERS', 30, 10, true, 'SYSTEM', 'SYSTEM'),
  ('dash_admin_storage_failure', 'dashboard.action.admin.storageFailure', 'Storage failures', 'dashboard.group.admin', 'Admin', '/admin/storage-failures', 'system:config', 'dashboard.action.admin.storageFailure', 'STORAGE_FAILURES', 30, 20, true, 'SYSTEM', 'SYSTEM'),
  ('dash_admin_security_audit', 'dashboard.action.admin.securityAudit', 'Security audit', 'dashboard.group.admin', 'Admin', '/admin/audit-logs?type=security', 'audit:view', 'dashboard.action.admin.securityAudit', 'SECURITY_EVENTS', 30, 30, true, 'SYSTEM', 'SYSTEM'),
  ('dash_todo_claim_rejected_resubmit', 'dashboard.action.todo.claimRejectedResubmit', 'Resubmit rejected claim', 'dashboard.group.todo', 'Todo', '/claims?status=rejected', 'claim:submit', 'dashboard.action.todo.claimRejectedResubmit', 'REJECTED_CLAIMS', 40, 10, true, 'SYSTEM', 'SYSTEM'),
  ('dash_todo_claim_review', 'dashboard.action.todo.claimReview', 'Claim review', 'dashboard.group.todo', 'Todo', '/finance/claims', 'claim:finance-review', 'dashboard.action.todo.claimReview', 'PENDING_CLAIMS', 40, 20, true, 'SYSTEM', 'SYSTEM')
ON CONFLICT DO NOTHING;
