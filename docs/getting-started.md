# Démarrage développeur

## Dépendances

Installez Node.js 22+, pnpm 10+, Java 21+, Flutter stable et PostgreSQL 16+. Copiez `.env.example` en `.env` à la racine et renseignez les identifiants locaux.

## Interfaces web

Depuis la racine : `pnpm install`, puis `pnpm dev`. Admin : http://localhost:3001 ; SaaS : http://localhost:3000.


## Backend

Démarrez PostgreSQL avec `docker compose up -d postgres`. Pour charger les variables dans le terminal, utilisez `set -a; source .env; set +a`, puis lancez le backend avec `cd backend && ./gradlew bootRun`. Pour le premier accès admin local, configurez `IDENTITY_BOOTSTRAP_ADMIN_PHONE=+22890000001`. En développement, l’OTP est journalisé et exposé par l’API (`OTP_DEV_MODE=true`). En production, définissez `OTP_DEV_MODE=false`, `OTP_SECRET`, `SMS_GATEWAY_URL` et éventuellement `SMS_GATEWAY_TOKEN`. API : http://localhost:8080/api/health.

## Mobile

Dans `apps/mobile`, exécutez `flutter pub get` puis `flutter run`. La configuration d’URL API est centralisée dans `lib/core/config/app_config.dart`.
