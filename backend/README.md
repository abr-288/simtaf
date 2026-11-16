# Backend Menewa

API REST Java 21 avec Spring Boot 3 et Spring Modulith. Chaque domaine métier doit devenir un module racine sous `com.menewa` (par exemple `accounts`, `billing`). Les modules exposent leurs API via `package-info.java` et échangent les effets inter-domaines via des événements applicatifs.

## Lancer

Configurez PostgreSQL, puis lancez `./gradlew bootRun`. Vérification de santé : `GET /api/health` ou `/actuator/health`.

Le premier module métier est `projects` : `GET /api/projects` liste les projets et `POST /api/projects` en crée un. Flyway initialise la table à partir de `src/main/resources/db/migration`. Le SaaS consomme ces routes via `packages/api-client`.

Le module `catalog` expose `GET /api/catalog` publiquement et les opérations `/api/admin/catalog*` aux détenteurs de `catalog.manage`. Pour reprendre les articles de Naya, après Flyway, exécutez `psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f scripts/import-legacy-catalog.sql`. Le seed local (quelques tarifs de démonstration) est contrôlé par `CATALOG_SEED_DEMO_DATA=true` ; il est désactivé par défaut.

## Identité et accès

Le module `identity` expose `POST /api/auth/otp/request`, `POST /api/auth/otp/verify`, `GET /api/auth/me`, `GET /api/auth/permissions` et `POST /api/auth/logout`. Les appels authentifiés utilisent `Authorization: Bearer <accessToken>`. En développement, l’OTP est journalisé et renvoyé à l’interface ; configurez `IDENTITY_BOOTSTRAP_ADMIN_PHONE` pour créer le compte admin initial. En production, définissez `OTP_DEV_MODE=false`, `OTP_SECRET`, `SMS_GATEWAY_URL` et éventuellement `SMS_GATEWAY_TOKEN`. L’adaptateur SMS envoie le JSON `{to, message, sender}`.

Les interfaces Next.js gardent le jeton dans un cookie `HttpOnly` propre à l’application, puis appellent le backend depuis leurs Server Actions et Server Components. Les permissions reçues de l’API servent à l’affichage ; toute autorisation d’action doit également être appliquée à l’endpoint métier.

Pour une base de production existante, exécutez les migrations Flyway puis `psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f scripts/import-legacy-identity.sql` une seule fois afin de reprendre utilisateurs et permissions Drizzle. Les défis OTP et sessions actifs ne sont pas copiés : tous les utilisateurs devront se réauthentifier après bascule. Vérifiez les comptes et droits importés avant de retirer l’ancienne application.

La configuration locale est dans `src/main/resources/application.yml`; les secrets et paramètres de déploiement passent par les variables d’environnement.
