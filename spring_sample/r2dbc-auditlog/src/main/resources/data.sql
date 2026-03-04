INSERT INTO approval_request (request_number, requester, target_system, status, reason, created_at, updated_at)
VALUES ('APR-2026-000', 'security-team', 'auth-service', 'REQUESTED', '정책 엔진 점검 윈도우 확보', NOW(), NOW())
ON CONFLICT (request_number) DO NOTHING;

INSERT INTO audit_log (aggregate_type, aggregate_id, action, actor, detail, logged_at)
VALUES ('APPROVAL_REQUEST', 'APR-2026-000', 'REQUEST_CREATED', 'security-team', '정책 엔진 점검 윈도우 확보', NOW())
ON CONFLICT DO NOTHING;
