# Démarrage développeur

## Dépendances

Installez Node.js 22+, pnpm 10+, Java 21 (Gradle 8.14 ne démarre pas avec Java 25 : définissez `JAVA_HOME` vers un JDK 21), Flutter stable et PostgreSQL 16+. Copiez `.env.example` en `.env` à la racine et renseignez les identifiants locaux.

## Interfaces web

Depuis la racine : `pnpm install`, puis `pnpm dev`. Admin : http://localhost:3001 ; SaaS : http://localhost:3000.

Le catalogue public est disponible sur `http://localhost:3000/catalogue` et l’espace membre sur `http://localhost:3000/espace` (donateurs, clients, prestataires, partenaires, bénéficiaires). La console d’administration est sur `http://localhost:3001/admin` ; chaque écran exige la permission correspondante. Les fichiers protégés (preuves, reçus) sont servis par les routes `/files/*` de chaque app, qui relaient le jeton à l’API.

## Backend

Démarrez PostgreSQL avec `docker compose up -d postgres`. Pour charger les variables dans le terminal, utilisez `set -a; source .env; set +a`, puis lancez le backend avec `cd backend && ./gradlew bootRun`. Pour le premier accès admin local, configurez `IDENTITY_BOOTSTRAP_ADMIN_PHONE=+22890000001`. En développement, l’OTP est journalisé et exposé par l’API (`OTP_DEV_MODE=true`). En production, définissez `OTP_DEV_MODE=false`, `OTP_SECRET`, `SMS_PROVIDER=twilio`, `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN` et `TWILIO_FROM` (ou `TWILIO_MESSAGING_SERVICE_SID`) : le code part alors par SMS au numéro saisi et n’est plus renvoyé par l’API. API : http://localhost:8080/api/health.

## Reprise des données de Menewa Naya

Après le premier démarrage du backend (migrations Flyway), exécutez dans l’ordre `scripts/import-legacy-identity.sql`, `scripts/import-legacy-catalog.sql` puis `scripts/import-legacy-operations.sql` sur la base contenant les tables legacy, et copiez le dossier `storage/` de l’ancienne application dans `STORAGE_DIR`. Le dernier script affiche des comptages et totaux de réconciliation.

## Mobile

Dans `apps/mobile`, exécutez `flutter pub get` puis `flutter run`. La configuration d’URL API est centralisée dans `lib/core/config/app_config.dart` (surcharge : `--dart-define=API_BASE_URL=...`). Le texte en clair (HTTP) n’est autorisé que dans le manifeste Android de debug.

## Tests

| Partie | Commande | Remarque |
|---|---|---|
| Backend | `cd backend && ./gradlew test` | PostgreSQL 16 embarqué, aucune base à démarrer ; exporte le contrat dans `docs/api/` |
| Contrat | `pnpm contract` (ou `pnpm contract:check` en CI) | Régénère types TS, libellés et modèles Dart, vérifie les 149 appels clients |
| Web | `pnpm typecheck`, `pnpm lint`, `pnpm build` | |
| Mobile | `cd apps/mobile && flutter analyze && flutter test` | Les modèles `lib/core/api.g.dart` sont générés : ne pas les modifier |
| Bout en bout | `cd e2e && pnpm install-browsers && DEMO_PASSWORD=… pnpm test` | Vise la zone de dev par défaut (`SAAS_URL`, `ADMIN_URL` pour une autre cible). Crée un donateur `+22899…` et une demande acceptée |

La CI GitHub Actions (`.github/workflows/ci.yml`) exécute les jobs backend, web et mobile à chaque push ; les tests de bout en bout se lancent à la main (« Run workflow », case *e2e*), avec la variable `E2E_SAAS_URL`/`E2E_ADMIN_URL` et le secret `E2E_DEMO_PASSWORD` définis dans le dépôt.
