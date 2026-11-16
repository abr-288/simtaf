# Démarrage développeur

## Dépendances

Installez Node.js 22+, pnpm 10+, Java 21+, Flutter stable et PostgreSQL 16+. Copiez `.env.example` en `.env` à la racine et renseignez les identifiants locaux.

## Interfaces web

Depuis la racine : `pnpm install`, puis `pnpm dev`. Admin : http://localhost:3001 ; SaaS : http://localhost:3000.

## Backend

Démarrez PostgreSQL avec `docker compose up -d postgres`, puis dans `backend`, exécutez `./gradlew bootRun`. API : http://localhost:8080/api/health.

## Mobile

Dans `apps/mobile`, exécutez `flutter pub get` puis `flutter run`. La configuration d’URL API est centralisée dans `lib/core/config/app_config.dart`.
