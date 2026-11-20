# Architecture

## Vue d’ensemble

Le dépôt contient cinq livrables indépendants : Flutter pour iOS/Android, une API Spring Modulith, un portail admin Next.js, un portail SaaS Next.js et la documentation.

```text
mobile ──┐
admin ───┼──> API Spring Modulith ──> PostgreSQL
saas ────┘          └──> stockage privé des fichiers (STORAGE_DIR), passerelle SMS
```

Les trois clients consomment le même contrat REST et n’accèdent jamais à la base. Les applications web gardent le jeton d’accès dans un cookie `HttpOnly` propre à chaque app et appellent l’API depuis leurs Server Components et Server Actions ; Flutter le conserve dans le trousseau sécurisé. Les permissions reçues servent à l’affichage ; chaque endpoint revérifie rôle, permission, propriété de l’objet et transition d’état.

## Modules backend

| Module | Données | API principale | Dépend de |
|---|---|---|---|
| `identity` | utilisateurs, profil, OTP, sessions, permissions | `/api/auth/*`, `/api/v1/admin/users*` | — (publie des événements) |
| `platform` | audit, notifications, paramètres, messages de contact | `/api/v1/notifications*`, `/api/v1/settings/public`, `/api/v1/admin/{settings,audit,contact-messages}`, `/api/v1/contact` | identity |
| `catalog` | articles, cibles des dons | `/api/v1/catalog*`, `/api/v1/admin/catalog*` | identity, platform |
| `rewards` | Token Mena (règles, ledger, comptes), badges, réalisations, avis | `/api/v1/rewards/*`, `/api/v1/admin/rewards*` | identity, platform |
| `providers` | profils prestataires, services et tarifs | `/api/v1/providers/*`, `/api/v1/admin/providers*` | identity, platform, rewards |
| `giving` | panier, demandes, échéances, bénéficiaires, missions, preuves, paiements, litiges, statistiques | `/api/v1/{cart,requests,payments,missions,proofs,disputes,files,beneficiary}*`, `/api/v1/admin/{dashboard,requests,missions,proofs,payments,finances,disputes,beneficiaries,stats}*` | identity, platform, catalog, rewards, providers |
| `community` | services communautaires, publications partenaires, contributions | `/api/v1/services/orders*`, `/api/v1/listings*`, `/api/v1/admin/{listings,contributions}*` | identity, platform, rewards, providers, giving |

Le graphe est acyclique et vérifié par `ModularityTests` (`ApplicationModules.verify()`). `identity` ne dépend d’aucun module : il publie `UserRegistered`, `UserLoggedIn`, `UserRoleChanged` et `IdentityAudited`, écoutés par `platform` (audit), `rewards` (Mena de bienvenue) et `providers` (création du profil prestataire). Chaque table appartient à un seul module (préfixe `platform_`, `catalog_`, `rewards_`, `providers_`, `giving_`, `community_`) ; les références entre modules sont des identifiants, sans clé étrangère. Les lectures inter-modules passent par les API publiques (`IdentityDirectory`, `CatalogLookup`, `ProviderDirectory`, `Rewards`, `Reputation`, `ServicePayments`, `Audit`, `Notifications`, `Settings`).

Les erreurs métier sont renvoyées en `application/problem+json` ; le champ `detail` contient le message destiné à l’utilisateur.

## Contrat front ↔ back

Le backend est la seule source de vérité. Le module de démonstration `projects` a été retiré (migration `V11`).

- **OpenAPI** : `GET /v3/api-docs` (interface `/swagger-ui.html` en local). Le contrat est exporté dans `docs/api/openapi.json` par `ContractExportTests` à chaque `./gradlew test`. Tous les champs sont présents ; `nullable` est déduit du type Java ; les noms de records sont uniques.
- **Référentiel** : `GET /api/v1/reference` (statuts et tonalités, catégories, rôles, permissions, moyens de paiement…), exporté dans `docs/api/reference.json`.
- **`pnpm contract`** (`scripts/contract.mjs`) génère :
  - `packages/shared-types/src/openapi.generated.ts` (types des schémas) ;
  - `packages/ui/src/reference.generated.ts` (libellés du web) ;
  - `apps/mobile/lib/core/reference.g.dart` (libellés du mobile).
  
  Le script vérifie ensuite que chaque appel des clients TypeScript et Dart (chemin et méthode) existe dans le contrat. `pnpm contract:check` échoue aussi si un fichier généré n’est pas à jour (pour la CI).
- **Vérification des types** : `packages/shared-types/src/contract.check.ts` compare à la compilation chaque type de réponse du front avec son schéma généré (présence et type de base de chaque champ). Un renommage ou un changement côté backend fait échouer `tsc`.
- **Les fronts n’ont aucune règle métier** : les textes d’aide (conditions de clôture d’un don, par exemple) sont calculés par l’API. Il reste dans `apps/` une couche fine : Server Actions qui relaient les formulaires vers l’API, cookie de session `HttpOnly`, proxys des fichiers protégés et de l’export CSV.

## Règles de gestion reprises de Menewa Naya

- Montants recalculés côté serveur à partir du catalogue et des paramètres (`annex.prices`) ; création de demande idempotente (`idempotencyKey`).
- Référence `MN-AAAA-NNNNN`, historique de chaque transition, refus et demande d’information motivés.
- Mission rattachée à une demande acceptée et à un prestataire, sauf exception administrative documentée ; refus motivé, réattribution, annulation tracées.
- Pas de don « réalisé » sans preuve validée (`proofs.min_validated`, `proofs.accepted_types`), missions terminées et échéances réalisées ; clôture → Mena au donateur.
- Coordonnées du bénéficiaire visibles du prestataire seulement après acceptation ; consultation admin tracée (`beneficiaries.view`).
- Fichiers hors webroot, type vérifié par signature, empreinte SHA-256, téléchargement via `/api/v1/files/{proof|receipt|dispute}/{id}` après contrôle des droits (proxys `/files/*` dans les apps web).
- Aucune suppression d’écriture financière (statut `cancelled`) ; Token Mena en ledger append-only avec anti-doublon, plafond quotidien, pas d’auto-attribution, validation par un autre administrateur principal au-delà du plafond ; transferts et conversions désactivés par défaut.

## Frontends

- `packages/api-client` : une fonction par endpoint, erreurs traduites en français (`ApiRequestError`).
- `packages/shared-types` : contrats JSON.
- `packages/ui` : composants (AppShell, tableaux, badges, suivi de demande, fil de litige), formats et libellés communs ; thème Tailwind 4 de Menewa Naya.
- Admin : routes `/admin/*` ; SaaS : routes `/espace/*`. Les chemins d’eleos sont conservés pour que les liens des notifications existantes restent valides.
- Mobile : `lib/core` (client API, session, formats, widgets) et `lib/features/*` par parcours.
