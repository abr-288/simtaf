-- Cibles des dons (famille, école…), auparavant stockées dans le paramètre `catalog.targets`.
CREATE TABLE catalog_targets (
    id VARCHAR(80) PRIMARY KEY,
    label VARCHAR(60) NOT NULL,
    description VARCHAR(200) NOT NULL DEFAULT '',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0
);

INSERT INTO catalog_targets (id, label, sort_order) VALUES
    ('cible-1', 'Famille', 1),
    ('cible-2', 'Orphelinat', 2),
    ('cible-3', 'École', 3),
    ('cible-4', 'Personne âgée', 4),
    ('cible-5', 'Personne malade', 5),
    ('cible-6', 'Communauté / village', 6),
    ('cible-7', 'Lieu de culte', 7);
