# Backend Menewa

API REST Java 21 avec Spring Boot 3 et Spring Modulith. Chaque domaine métier doit devenir un module racine sous `com.menewa` (par exemple `accounts`, `billing`). Les modules exposent leurs API via `package-info.java` et échangent les effets inter-domaines via des événements applicatifs.

## Lancer

Configurez PostgreSQL, puis lancez `./gradlew bootRun`. Vérification de santé : `GET /api/health` ou `/actuator/health`.

Le schéma est géré exclusivement par Flyway (`src/main/resources/db/migration`). Le module de démonstration `projects` a été retiré (`V11__drop_projects.sql` ; `V1` reste dans l'historique Flyway). Le contrat est publié sur `/v3/api-docs` et exporté dans `docs/api/` par `ContractExportTests`.

Le module `catalog` expose `GET /api/catalog` publiquement et les opérations `/api/admin/catalog*` aux détenteurs de `catalog.manage`. Pour reprendre les articles de Naya, après Flyway, exécutez `psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f scripts/import-legacy-catalog.sql`. Le seed local (quelques tarifs de démonstration) est contrôlé par `CATALOG_SEED_DEMO_DATA=true` ; il est désactivé par défaut.

## Identité et accès

Le module `identity` expose `POST /api/auth/otp/request`, `POST /api/auth/otp/verify`, `GET /api/auth/me`, `GET /api/auth/permissions` et `POST /api/auth/logout`. Les appels authentifiés utilisent `Authorization: Bearer <accessToken>`. En développement, l’OTP est journalisé et renvoyé à l’interface ; configurez `IDENTITY_BOOTSTRAP_ADMIN_PHONE` pour créer le compte admin initial. En production, définissez `OTP_DEV_MODE=false`, `OTP_SECRET`, `SMS_GATEWAY_URL` et éventuellement `SMS_GATEWAY_TOKEN`. L’adaptateur SMS envoie le JSON `{to, message, sender}`.

Les interfaces Next.js gardent le jeton dans un cookie `HttpOnly` propre à l’application, puis appellent le backend depuis leurs Server Actions et Server Components. Les permissions reçues de l’API servent à l’affichage ; toute autorisation d’action doit également être appliquée à l’endpoint métier.

Pour une base de production existante, exécutez les migrations Flyway puis `psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f scripts/import-legacy-identity.sql` une seule fois afin de reprendre utilisateurs et permissions Drizzle. Les défis OTP et sessions actifs ne sont pas copiés : tous les utilisateurs devront se réauthentifier après bascule. Vérifiez les comptes et droits importés avant de retirer l’ancienne application.

La configuration locale est dans `src/main/resources/application.yml`; les secrets et paramètres de déploiement passent par les variables d’environnement.

## Modules repris de Menewa Naya

`platform` (audit, notifications, paramètres, contact), `rewards` (Token Mena, badges, avis), `providers`, `giving` (demandes, missions, preuves, paiements, litiges, bénéficiaires, statistiques) et `community` (services communautaires, publications partenaires). Voir [docs/architecture.md](../docs/architecture.md) et l’ADR [0001](../docs/adr/0001-reprise-naya-modules-et-acces-donnees.md). Les fichiers sont stockés dans `STORAGE_DIR` (défaut `./storage`). Reprise des données : `scripts/import-legacy-operations.sql` après les scripts d’identité et de catalogue.

Tests : `./gradlew test` (39 tests) tourne sur un **vrai PostgreSQL 16 embarqué** (zonky, lancé par `ApiTestBase`), sans Docker ni base à installer :

- `ModularityTests` : frontières entre modules (Spring Modulith) ;
- `AuthorizationTests` : chaque route d'administration contre anonyme, membre, administrateur secondaire sans ou avec la bonne permission, administrateur principal ; propriété des demandes, paiements, litiges, missions et preuves ; rôles réservés ; révocation de session ;
- `BusinessRulesTests` : OTP (limites, tentatives), transitions de dons, montants recalculés côté serveur, idempotence, missions et preuves, Mena (plafond, doublons, ajustements), services et campagnes ;
- `GivingFlowTests`, `CatalogApiTests` : parcours complets ;
- `ContractExportTests` : export du contrat dans `docs/api/` (LF) et unicité des noms de records.

`src/test/resources/application.yml` remplace celui de production pendant les tests : tout réglage nécessaire aux tests doit y être repris.
