-- Profil utilisateur repris de Menewa Naya (table legacy `users`).
ALTER TABLE identity_users ADD COLUMN email VARCHAR(120);
ALTER TABLE identity_users ADD COLUMN zone VARCHAR(100);
ALTER TABLE identity_users ADD COLUMN country VARCHAR(60) DEFAULT 'Togo';
ALTER TABLE identity_users ADD COLUMN organization VARCHAR(120);
ALTER TABLE identity_users ADD COLUMN bio VARCHAR(600);
ALTER TABLE identity_users ADD COLUMN notif_inapp BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE identity_users ADD COLUMN notif_sms BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE identity_users ADD COLUMN notif_email BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_identity_users_role ON identity_users(role);
