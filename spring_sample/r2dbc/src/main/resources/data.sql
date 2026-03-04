INSERT INTO customer_account (customer_code, name, email, tier, created_at, updated_at)
VALUES
    ('CUST-100', 'Kim Minji', 'minji.kim@example.com', 'STANDARD', NOW(), NOW()),
    ('CUST-200', 'Lee Jihoon', 'jihoon.lee@example.com', 'VIP', NOW(), NOW())
ON CONFLICT (customer_code) DO NOTHING;
