INSERT INTO job_definition (
    id, job_name, cron_expr, bean_name, method_name, arg_types_json, args_json, enabled, created_at, updated_at
) VALUES (
    1,
    'daily-report',
    '0 0/1 * * * ?',
    'reportService',
    'generateDailyReport',
    '["com.example.dynamicjob.dto.ReportRequest"]',
    '[{"bizDate":"2026-03-06","type":"DAILY","retryCount":3}]',
    TRUE,
    CURRENT_TIMESTAMP(),
    CURRENT_TIMESTAMP()
);

INSERT INTO job_definition (
    id, job_name, cron_expr, bean_name, method_name, arg_types_json, args_json, enabled, created_at, updated_at
) VALUES (
    2,
    'user-sync',
    '15 0/2 * * * ?',
    'userService',
    'syncUsers',
    '["java.lang.String","java.lang.Boolean"]',
    '["LEGACY",true]',
    TRUE,
    CURRENT_TIMESTAMP(),
    CURRENT_TIMESTAMP()
);
