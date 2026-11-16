-- Run once after Flyway V3__create_catalog.sql against the existing Naya database.
-- Prices and costs remain the values present at cutover; review them before accepting new donations.
BEGIN;

INSERT INTO menewa_catalog_items (id, slug, title, category, kind, description, unit_price, cost_estimate, unit, emoji, active, created_at)
SELECT id, slug, title, category, kind, description, unit_price, cost_estimate, unit, emoji, active, created_at
FROM catalog_items
ON CONFLICT (slug) DO NOTHING;

SELECT setval(
    pg_get_serial_sequence('menewa_catalog_items', 'id'),
    COALESCE(MAX(id), 1),
    MAX(id) IS NOT NULL
)
FROM menewa_catalog_items;

COMMIT;
