# Menewa — monorepo

Monorepo de Menewa : application mobile Flutter, backend Java/Spring Modulith, interfaces web Next.js et documentation.

## Prérequis

- Node.js 22+ et pnpm 10+
- Flutter stable et Dart (pour `apps/mobile`)
- Java 21+ (pour `backend`)
- PostgreSQL 16+ (pour le backend)

## Structure

```text
apps/
  admin/       Interface d’administration — Next.js
  saas/        Application SaaS — Next.js
  mobile/      Application iOS/Android — Flutter
backend/       API Java — Spring Boot + Spring Modulith
packages/
  typescript-config/  Configurations TypeScript partagées
  eslint-config/      Configuration ESLint partagée
  api-client/         Client HTTP TypeScript partagé
  shared-types/       Types communs aux interfaces web
docs/          Documentation produit, architecture et démarrage
```

## Démarrage

```bash
cp .env.example .env
docker compose up -d postgres
pnpm install
pnpm dev
```

Les commandes pnpm pilotent les deux applications Next.js. Démarrez le backend dans un autre terminal avec `cd backend && ./gradlew bootRun`, et le mobile avec `cd apps/mobile && flutter run`. Pour compiler l’ensemble : `pnpm build`, `cd backend && ./gradlew build`, puis `cd apps/mobile && flutter analyze && flutter build apk --debug`.

Consultez [docs/getting-started.md](docs/getting-started.md) pour les détails, notamment l’installation Flutter et le démarrage de PostgreSQL.
