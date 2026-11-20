-- Reprise des données opérationnelles de Menewa Naya (eleos / sample, schéma Drizzle) vers les modules
-- platform, catalog, rewards, providers, giving et community.
--
-- Prérequis, dans cet ordre, sur la base PostgreSQL contenant les tables legacy :
--   1. migrations Flyway V1 → V10 appliquées (démarrage du backend) ;
--   2. scripts/import-legacy-identity.sql puis scripts/import-legacy-catalog.sql ;
--   3. ce script, une seule fois : psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f scripts/import-legacy-operations.sql
--   4. copier le dossier legacy `storage/` (proofs, receipts, disputes) dans STORAGE_DIR du backend :
--      les clés de fichiers reprennent les chemins relatifs existants.
-- Les identifiants sont conservés (références MN-*, LT-*, liens des notifications). Faire une répétition
-- sur une copie, comparer comptages et totaux, puis seulement basculer (voir docs/plan-migration.md § 6).
BEGIN;

/* ───────── Identité : profil complémentaire ───────── */
UPDATE identity_users t SET email = u.email, zone = u.zone, country = COALESCE(u.country, 'Togo'), organization = u.organization, bio = u.bio,
       notif_inapp = COALESCE((u.notif_prefs->>'inapp')::boolean, TRUE), notif_sms = COALESCE((u.notif_prefs->>'sms')::boolean, TRUE),
       notif_email = COALESCE((u.notif_prefs->>'email')::boolean, FALSE)
FROM users u WHERE u.id = t.id;

/* ───────── Plateforme ───────── */
INSERT INTO platform_settings (setting_key, setting_value, description, updated_by, updated_at)
SELECT key, value::text, description, updated_by, updated_at FROM settings WHERE key <> 'catalog.targets'
ON CONFLICT (setting_key) DO UPDATE SET setting_value = EXCLUDED.setting_value, updated_by = EXCLUDED.updated_by, updated_at = EXCLUDED.updated_at;

INSERT INTO platform_notifications (id, user_id, type, title, body, link, read_at, created_at)
SELECT id, user_id, type, LEFT(title, 200), LEFT(body, 1000), LEFT(link, 200), read_at, created_at FROM notifications;

INSERT INTO platform_audit_logs (id, actor_id, actor_role, action, entity, entity_id, details, ip, created_at)
SELECT id, actor_id, actor_role, action, entity, entity_id, LEFT(details::text, 4000), ip, created_at FROM audit_logs;

INSERT INTO platform_contact_messages (id, name, email, phone, subject, message, handled, created_at)
SELECT id, name, email, phone, subject, message, handled, created_at FROM contact_messages;

/* ───────── Catalogue : cibles (anciennement paramètre catalog.targets) ───────── */
DELETE FROM catalog_targets WHERE EXISTS (SELECT 1 FROM settings WHERE key = 'catalog.targets');
INSERT INTO catalog_targets (id, label, description, active, sort_order)
SELECT t->>'id', t->>'label', COALESCE(t->>'description', ''), COALESCE((t->>'active')::boolean, TRUE), x.o
FROM settings s, jsonb_array_elements(s.value) WITH ORDINALITY AS x(t, o)
WHERE s.key = 'catalog.targets';

/* ───────── Récompenses & réputation ───────── */
INSERT INTO rewards_rules (event_key, label, amount, daily_cap, active)
SELECT event_key, label, amount, daily_cap, active FROM token_rules
ON CONFLICT (event_key) DO UPDATE SET label = EXCLUDED.label, amount = EXCLUDED.amount, daily_cap = EXCLUDED.daily_cap, active = EXCLUDED.active;

INSERT INTO rewards_accounts (user_id, balance, earned) SELECT id, token_balance, tokens_earned FROM users
ON CONFLICT (user_id) DO UPDATE SET balance = EXCLUDED.balance, earned = EXCLUDED.earned;

INSERT INTO rewards_transactions (id, user_id, amount, type, event_key, reason, ref_type, ref_id, ref_key, status, created_by, created_at)
SELECT id, user_id, amount, type, event_key, LEFT(reason, 300), ref_type, ref_id, ref_key, status, created_by, created_at FROM token_transactions;

INSERT INTO rewards_badges (id, user_id, code, awarded_at) SELECT id, user_id, code, awarded_at FROM user_badges;

INSERT INTO rewards_reviews (id, kind, ref_id, from_user_id, to_user_id, rating, comment, created_at)
SELECT id, kind, ref_id, from_user_id, to_user_id, rating, comment, created_at FROM reviews;

-- Réalisations comptées pour les badges (dons clôturés, missions validées).
INSERT INTO rewards_achievements (ref_key, user_id, kind, created_at)
SELECT 'gift_closed:request:' || id || ':' || user_id, user_id, 'gift_closed', COALESCE(closed_at, updated_at) FROM requests WHERE status = 'closed';
INSERT INTO rewards_achievements (ref_key, user_id, kind, created_at)
SELECT 'mission_validated:mission:' || id || ':' || provider_id, provider_id, 'mission_validated', validated_at FROM missions
WHERE validated_at IS NOT NULL AND provider_id IS NOT NULL;

/* ───────── Prestataires ───────── */
INSERT INTO providers_profiles (user_id, category, description, zone, available, verified)
SELECT user_id, category, description, zone, available, verified FROM provider_profiles
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO providers_services (id, provider_id, label, category, unit_cost, unit, description, active)
SELECT id, provider_id, label, category, unit_cost, unit, description, active FROM provider_services;

/* ───────── Dons : demandes, missions, preuves, paiements, litiges ───────── */
INSERT INTO giving_cart_items (id, user_id, catalog_item_id, quantity, target, created_at)
SELECT id, user_id, catalog_item_id, LEAST(GREATEST(quantity, 1), 500), target, created_at FROM cart_items;

INSERT INTO giving_beneficiaries (id, full_name, phone, zone, address, needs, notes, consent, user_id, created_by, archived_at, created_at)
SELECT id, full_name, phone, zone, address, needs, notes, consent, user_id, created_by, archived_at, created_at FROM beneficiaries;

INSERT INTO giving_requests (id, ref, user_id, kind, title, description, category, target_name, target_zone, beneficiary_id, partner_id, status,
    rejection_reason, info_request, amount_per_occurrence, occurrences, total_amount, payment_mode, recurrence, start_date, annex_services,
    annex_cost, execution_budget, other_fees, created_at, updated_at, completed_at, closed_at, archived_at)
SELECT id, ref, user_id, kind, title, description, category, target_name, target_zone, beneficiary_id, partner_id, status,
    rejection_reason, info_request, amount_per_occurrence, occurrences, total_amount, payment_mode, recurrence, start_date, annex_services::text,
    annex_cost, execution_budget, other_fees, created_at, updated_at, completed_at, closed_at, archived_at
FROM requests;

INSERT INTO giving_request_items (id, request_id, catalog_item_id, label, quantity, unit_price, cost_estimate, target)
SELECT id, request_id, catalog_item_id, label, quantity, unit_price, cost_estimate, target FROM request_items;

INSERT INTO giving_request_history (id, request_id, status, note, actor_id, created_at)
SELECT id, request_id, status, note, actor_id, created_at FROM request_history;

INSERT INTO giving_installments (id, request_id, seq, due_date, amount, status, notified_at)
SELECT id, request_id, seq, due_date, amount, status, notified_at FROM installments;

INSERT INTO giving_missions (id, request_id, provider_id, title, instructions, status, refusal_reason, due_date, provider_fee, compensation_mode,
    exchange_description, exchange_status, exception_reason, reassigned_from, created_by, created_at, updated_at, completed_at, validated_at)
SELECT id, request_id, provider_id, title, instructions, status, refusal_reason, due_date, provider_fee, compensation_mode,
    exchange_description, exchange_status, exception_reason, reassigned_from, created_by, created_at, updated_at, completed_at, validated_at
FROM missions;

INSERT INTO giving_proofs (id, request_id, mission_id, type, file_key, file_name, mime, size, caption, uploaded_by, status, rejection_reason,
    reviewed_by, reviewed_at, visible_to_donor, created_at)
SELECT id, request_id, mission_id, type, file_path, file_name, mime, size, caption, uploaded_by, status, rejection_reason,
    reviewed_by, reviewed_at, visible_to_donor, created_at
FROM proofs;

INSERT INTO giving_payments (id, request_id, mission_id, service_order_id, installment_id, user_id, direction, kind, mode, method, amount_planned,
    amount_validated, amount_paid, status, reference, receipt_key, receipt_name, receipt_mime, exchange_description, exchange_status, due_date,
    note, created_at, paid_at)
SELECT id, request_id, mission_id, service_order_id, installment_id, user_id, direction, kind, mode, method, amount_planned,
    amount_validated, amount_paid, status, reference, receipt_path, receipt_name,
    CASE WHEN receipt_path LIKE '%.pdf' THEN 'application/pdf' WHEN receipt_path LIKE '%.png' THEN 'image/png'
         WHEN receipt_path LIKE '%.webp' THEN 'image/webp' WHEN receipt_path IS NOT NULL THEN 'image/jpeg' END,
    exchange_description, exchange_status, due_date, note, created_at, paid_at
FROM payments;

INSERT INTO giving_disputes (id, ref, request_id, mission_id, opened_by, against_user_id, subject, description, status, responsible_id, decision, created_at, closed_at)
SELECT id, ref, request_id, mission_id, opened_by, against_user_id, subject, description, status, responsible_id, decision, created_at, closed_at FROM disputes;

INSERT INTO giving_dispute_messages (id, dispute_id, author_id, body, attachment_key, attachment_name, attachment_mime, created_at)
SELECT id, dispute_id, author_id, body, attachment_path, attachment_name,
    CASE WHEN attachment_path LIKE '%.pdf' THEN 'application/pdf' WHEN attachment_path LIKE '%.png' THEN 'image/png'
         WHEN attachment_path IS NOT NULL THEN 'image/jpeg' END,
    created_at
FROM dispute_messages;

/* ───────── Communauté ───────── */
INSERT INTO community_service_orders (id, client_id, provider_id, offer_id, title, description, zone, proposed_price, agreed_price, compensation_mode,
    exchange_description, status, created_at, updated_at)
SELECT id, client_id, provider_id, offer_id, title, description, zone, proposed_price, agreed_price, compensation_mode,
    exchange_description, status, created_at, updated_at FROM service_orders;

INSERT INTO community_service_messages (id, order_id, author_id, body, price, created_at)
SELECT id, order_id, author_id, body, price, created_at FROM service_messages;

INSERT INTO community_listings (id, owner_id, type, title, description, zone, goal_amount, raised_amount, mena_support, ends_at, status, rejection_reason, created_at)
SELECT id, owner_id, type, title, description, zone, goal_amount, raised_amount, mena_support, ends_at, status, rejection_reason, created_at FROM listings;

INSERT INTO community_listing_contributions (id, listing_id, user_id, amount, method, reference, status, created_at)
SELECT id, listing_id, user_id, amount, method, reference, status, created_at FROM listing_contributions WHERE amount > 0;

/* ───────── Séquences d'identité ───────── */
DO $$
DECLARE t text;
BEGIN
    FOREACH t IN ARRAY ARRAY['platform_notifications', 'platform_audit_logs', 'platform_contact_messages', 'rewards_rules', 'rewards_transactions',
        'rewards_badges', 'rewards_reviews', 'providers_services', 'giving_cart_items', 'giving_beneficiaries', 'giving_requests',
        'giving_request_items', 'giving_request_history', 'giving_installments', 'giving_missions', 'giving_proofs', 'giving_payments',
        'giving_disputes', 'giving_dispute_messages', 'community_service_orders', 'community_service_messages', 'community_listings',
        'community_listing_contributions']
    LOOP
        EXECUTE format('SELECT setval(pg_get_serial_sequence(%L, ''id''), COALESCE((SELECT MAX(id) FROM %I), 1), (SELECT MAX(id) IS NOT NULL FROM %I))', t, t, t);
    END LOOP;
END $$;

/* ───────── Contrôles de réconciliation (à comparer avec la base legacy) ───────── */
SELECT 'requests' AS objet, (SELECT COUNT(*) FROM requests) AS legacy, (SELECT COUNT(*) FROM giving_requests) AS importe
UNION ALL SELECT 'payments', (SELECT COUNT(*) FROM payments), (SELECT COUNT(*) FROM giving_payments)
UNION ALL SELECT 'montant payé (in)', (SELECT COALESCE(SUM(amount_paid), 0) FROM payments WHERE direction = 'in'),
                 (SELECT COALESCE(SUM(amount_paid), 0) FROM giving_payments WHERE direction = 'in')
UNION ALL SELECT 'proofs', (SELECT COUNT(*) FROM proofs), (SELECT COUNT(*) FROM giving_proofs)
UNION ALL SELECT 'Mena en circulation', (SELECT COALESCE(SUM(token_balance), 0) FROM users), (SELECT COALESCE(SUM(balance), 0) FROM rewards_accounts);

COMMIT;
