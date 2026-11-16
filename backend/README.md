# Backend Menewa

API REST Java 21 avec Spring Boot 3 et Spring Modulith. Chaque domaine métier doit devenir un module racine sous `com.menewa` (par exemple `accounts`, `billing`). Les modules exposent leurs API via `package-info.java` et échangent les effets inter-domaines via des événements applicatifs.

## Lancer

Configurez PostgreSQL, puis lancez `./gradlew bootRun`. Vérification de santé : `GET /api/health` ou `/actuator/health`.

Le premier module métier est `projects` : `GET /api/projects` liste les projets et `POST /api/projects` en crée un. Flyway initialise la table à partir de `src/main/resources/db/migration`. Le SaaS consomme ces routes via `packages/api-client`.

La configuration locale est dans `src/main/resources/application.yml`; les secrets et paramètres de déploiement passent par les variables d’environnement.
