# Démarrage développeur

## Dépendances

Installez Node.js 22+, pnpm 10+, Java 21 (Gradle 8.14 ne démarre pas avec Java 25 : définissez `JAVA_HOME` vers un JDK 21), Flutter stable et PostgreSQL 16+. Copiez `.env.example` en `.env` à la racine et renseignez les identifiants locaux.

## Interfaces web

Depuis la racine : `pnpm install`, puis `pnpm dev`. Admin : http://localhost:3001 ; SaaS : http://localhost:3000.

Le catalogue public est disponible sur `http://localhost:3000/catalogue` et l’espace membre sur `http://localhost:3000/espace` (donateurs, clients, prestataires, partenaires, bénéficiaires). La console d’administration est sur `http://localhost:3001/admin` ; chaque écran exige la permission correspondante. Les fichiers protégés (preuves, reçus) sont servis par les routes `/files/*` de chaque app, qui relaient le jeton à l’API.

## Backend

Démarrez PostgreSQL avec `docker compose up -d postgres`. Pour charger les variables dans le terminal, utilisez `set -a; source .env; set +a`, puis lancez le backend avec `cd backend && ./gradlew bootRun`. Pour le premier accès admin local, configurez `IDENTITY_BOOTSTRAP_ADMIN_PHONE=+22890000001`. En développement, l’OTP est journalisé et exposé par l’API (`OTP_DEV_MODE=true`). En production, définissez `OTP_DEV_MODE=false`, `OTP_SECRET`, `SMS_GATEWAY_URL` et éventuellement `SMS_GATEWAY_TOKEN`. API : http://localhost:8080/api/health.

## Reprise des données de Menewa Naya

Après le premier démarrage du backend (migrations Flyway), exécutez dans l’ordre `scripts/import-legacy-identity.sql`, `scripts/import-legacy-catalog.sql` puis `scripts/import-legacy-operations.sql` sur la base contenant les tables legacy, et copiez le dossier `storage/` de l’ancienne application dans `STORAGE_DIR`. Le dernier script affiche des comptages et totaux de réconciliation.

## Mobile

Dans `apps/mobile`, exécutez `flutter pub get` puis `flutter run`. La configuration d’URL API est centralisée dans `lib/core/config/app_config.dart` (surcharge : `--dart-define=API_BASE_URL=...`). Le texte en clair (HTTP) n’est autorisé que dans le manifeste Android de debug.
