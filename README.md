# Menewa — monorepo

Monorepo de Menewa : application mobile Flutter, backend Java/Spring Modulith, interfaces web Next.js et documentation.

## Prérequis

- Node.js 22+ et pnpm 10+
- Flutter stable et Dart (pour `apps/mobile`)
- Java 21 (pour `backend` : Gradle 8.14 ne démarre pas sous Java 25)
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
  ui/                 Composants, libellés et formats partagés (admin + SaaS)
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

Après toute modification d’un endpoint ou d’un libellé côté backend : `cd backend && ./gradlew test` (exporte le contrat dans `docs/api/`), puis `pnpm contract` (régénère les types, libellés et modèles Dart des clients et vérifie leurs appels). Tests : voir [docs/getting-started.md](docs/getting-started.md#tests).

Consultez [docs/getting-started.md](docs/getting-started.md) pour les détails, notamment l’installation Flutter et le démarrage de PostgreSQL.

Déploiement sur le serveur de test (installation, mise à jour, retour arrière, APK, sauvegardes) : [deploy/dev/README.md](deploy/dev/README.md).

## Fonctionnalités reprises de Menewa Naya (`sample/`, eleos-site4)

- **Backend** : modules `identity`, `platform`, `catalog`, `rewards`, `providers`, `giving`, `community` — demandes de dons, missions, preuves, paiements, litiges, Token Mena, services communautaires, campagnes partenaires, statistiques et audit. Voir [docs/architecture.md](docs/architecture.md).
- **Admin** (`/admin/*`) : les 19 écrans d'administration (demandes, missions, preuves, paiements, finances, litiges, utilisateurs, rôles, prestataires, bénéficiaires, publications, catalogue, cibles, Token Mena, statistiques et export CSV, audit, paramètres).
- **SaaS** (`/espace/*`) : espaces donateur/client, prestataire, partenaire et bénéficiaire.
- **Mobile** : parcours MVP Flutter (OTP, catalogue, panier, demandes, paiements, missions prestataire avec preuve photo, notifications, profil).
