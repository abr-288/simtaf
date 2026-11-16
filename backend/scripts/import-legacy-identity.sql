-- Run once against the existing Menewa Naya PostgreSQL database after V2__create_identity.sql.
-- This copies accounts and sub-admin permissions. Existing OTP challenges and sessions are
-- intentionally not copied; users must request a fresh OTP after cutover.
BEGIN;

INSERT INTO identity_users (id, phone, name, role, status, created_at, last_login_at)
SELECT id, phone, COALESCE(name, ''), role, status, created_at, last_login_at
FROM users
ON CONFLICT (phone) DO NOTHING;

INSERT INTO identity_user_permissions (user_id, permission)
SELECT target.id, permission.value
FROM users legacy
JOIN identity_users target ON target.phone = legacy.phone
CROSS JOIN LATERAL jsonb_array_elements_text(COALESCE(legacy.permissions, '[]'::jsonb)) AS permission(value)
ON CONFLICT DO NOTHING;

SELECT setval(
    pg_get_serial_sequence('identity_users', 'id'),
    COALESCE(MAX(id), 1),
    MAX(id) IS NOT NULL
)
FROM identity_users;

COMMIT;
