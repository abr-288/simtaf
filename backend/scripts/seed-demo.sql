-- Données de démonstration identiques à Menewa Naya (eleos-site4, src/lib/seed.ts).
-- Zone de dev uniquement, après les migrations Flyway : psql -v ON_ERROR_STOP=1 -f scripts/seed-demo.sql
-- Idempotent : relançable sans doublon. La demande d'exemple est créée via l'API (voir deploy/dev/seed-demo.mjs).
BEGIN;

INSERT INTO identity_users (phone, name, role, zone, organization) VALUES
    ('+22890000001', 'Administrateur Menewa', 'admin', 'Lomé', NULL),
    ('+22890000006', 'Admin Modération', 'subadmin', 'Lomé', NULL),
    ('+22890000002', 'Koffi Amegah', 'donor', 'Lomé', NULL),
    ('+22890000003', 'Yao Traiteur & Co', 'provider', 'Lomé', NULL),
    ('+22890000007', 'Abalo Transport', 'provider', 'Lomé', NULL),
    ('+22890000008', 'Père Kodjo (officiant)', 'provider', 'Lomé', NULL),
    ('+22890000004', 'ONG Éducation pour Tous', 'partner', 'Lomé', 'Éducation pour Tous'),
    ('+22890000005', 'Ama Mensah', 'beneficiary', 'Kpalimé', NULL)
ON CONFLICT (phone) DO NOTHING;

INSERT INTO identity_user_permissions (user_id, permission)
SELECT u.id, p FROM identity_users u, unnest(ARRAY['requests.manage', 'proofs.review', 'stats.view', 'missions.manage']) AS p
WHERE u.phone = '+22890000006'
ON CONFLICT DO NOTHING;

-- Mena de bienvenue du donateur de démonstration (comme dans Naya).
INSERT INTO rewards_accounts (user_id, balance, earned)
SELECT id, 20, 20 FROM identity_users WHERE phone = '+22890000002'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO providers_profiles (user_id, category, description, zone, verified)
SELECT u.id, v.category, v.description, 'Lomé', v.verified
FROM (VALUES
    ('+22890000003', 'traiteur', 'Préparation et distribution de repas pour événements solidaires.', TRUE),
    ('+22890000007', 'transport', 'Transport de colis et de dons vers l''intérieur du pays.', TRUE),
    ('+22890000008', 'officiant', 'Bénédictions, prières et accompagnement spirituel.', FALSE)
) AS v(phone, category, description, verified)
JOIN identity_users u ON u.phone = v.phone
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO providers_services (provider_id, label, category, unit_cost, unit)
SELECT u.id, v.label, v.category, v.cost, v.unit
FROM (VALUES
    ('+22890000003', 'Repas pour 50 personnes', 'traiteur', 60000, 'prestation'),
    ('+22890000003', 'Distribution de packs alimentaires', 'traiteur', 8000, 'livraison'),
    ('+22890000007', 'Transport Lomé → Kpalimé', 'transport', 25000, 'trajet'),
    ('+22890000008', 'Bénédiction / prière de remise', 'officiant', 5000, 'intervention')
) AS v(phone, label, category, cost, unit)
JOIN identity_users u ON u.phone = v.phone
WHERE NOT EXISTS (SELECT 1 FROM providers_services s WHERE s.provider_id = u.id AND s.label = v.label);

INSERT INTO menewa_catalog_items (slug, title, category, kind, description, unit_price, cost_estimate, unit, emoji) VALUES
    ('pack-alimentaire-famille', 'Pack alimentaire famille', 'nourriture', 'don', 'Riz, huile, haricots, farine, sel, tomate — pour une famille pendant un mois.', 15000, 12000, 'pack', '🍚'),
    ('pack-alimentaire-enfant', 'Pack repas enfants', 'nourriture', 'don', 'Repas équilibrés pour 10 enfants pendant une semaine.', 8000, 6500, 'pack', '🥣'),
    ('belier', 'Bélier', 'betail', 'don', 'Un bélier pour une fête ou une famille dans le besoin.', 85000, 72000, 'tête', '🐏'),
    ('lot-volailles', 'Lot de 5 volailles', 'volaille', 'don', 'Cinq poulets pour démarrer un petit élevage ou partager un repas.', 25000, 20000, 'lot', '🐓'),
    ('habits-enfant', 'Habits pour enfant', 'habits', 'don', 'Tenue complète (haut, bas, sous-vêtements).', 12000, 9500, 'tenue', '👕'),
    ('habits-adulte', 'Habits pour adulte', 'habits', 'don', 'Tenue complète pour adulte.', 15000, 12000, 'tenue', '👗'),
    ('kit-scolaire', 'Kit de fournitures scolaires', 'fournitures', 'don', 'Cahiers, stylos, ardoise, sac.', 7500, 6000, 'kit', '🎒'),
    ('lampe-solaire', 'Lampe solaire', 'equipements', 'don', 'Éclairage autonome pour étudier le soir.', 6000, 4500, 'unité', '💡'),
    ('matelas', 'Matelas', 'equipements', 'don', 'Matelas neuf pour une personne.', 20000, 16000, 'unité', '🛏️'),
    ('visite-personne', 'Visite à une personne', 'action', 'action', 'Un bénévole rend visite à une personne âgée, malade ou isolée.', 5000, 3000, 'visite', '🤝'),
    ('acte-a-poser', 'Acte à poser (message / cadeau remis)', 'action', 'action', 'Remise d''un message ou d''un cadeau de votre part à une cible.', 4000, 2500, 'acte', '💌')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO community_listings (owner_id, type, title, description, zone, goal_amount, raised_amount, status, ends_at)
SELECT u.id, v.type, v.title, v.description, v.zone, v.goal, v.raised, 'published', v.ends
FROM (VALUES
    ('crowdfunding', 'Cantine de l''école de Kpalimé', 'Financer les repas quotidiens de 120 élèves pendant un trimestre.', 'Kpalimé', 1500000, 420000, now() + interval '60 days'),
    ('action', 'Distribution de kits scolaires', 'Rentrée solidaire : distribution de kits dans 3 écoles rurales.', 'Plateaux', 0, 0, NULL::timestamptz)
) AS v(type, title, description, zone, goal, raised, ends)
JOIN identity_users u ON u.phone = '+22890000004'
WHERE NOT EXISTS (SELECT 1 FROM community_listings l WHERE l.title = v.title);

INSERT INTO giving_beneficiaries (full_name, phone, zone, address, needs, consent, user_id, created_by)
SELECT 'Famille Mensah', '+22890111222', 'Kpalimé', 'Quartier Agbonou', 'Aide alimentaire mensuelle', TRUE, b.id, a.id
FROM identity_users b, identity_users a
WHERE b.phone = '+22890000005' AND a.phone = '+22890000001'
  AND NOT EXISTS (SELECT 1 FROM giving_beneficiaries WHERE full_name = 'Famille Mensah');

INSERT INTO rewards_reviews (kind, ref_id, from_user_id, to_user_id, rating, comment)
SELECT 'mission', 0, d.id, p.id, 5, 'Service impeccable et très humain.'
FROM identity_users d, identity_users p
WHERE d.phone = '+22890000002' AND p.phone = '+22890000003'
ON CONFLICT DO NOTHING;

COMMIT;
