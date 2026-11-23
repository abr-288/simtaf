-- Photos de la page d'accueil (site public et application mobile), téléversées par l'administration.
CREATE TABLE giving_home_photos (
    slot VARCHAR(20) PRIMARY KEY,
    file_key VARCHAR(200) NOT NULL,
    mime VARCHAR(60) NOT NULL,
    alt VARCHAR(200),
    updated_by BIGINT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
