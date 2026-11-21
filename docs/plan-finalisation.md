# Plan de finalisation

Objectif : `backend/` porte toute la logique métier et les données ; `apps/` (admin, saas, mobile) ne contient que l’interface. Le suivi de la migration depuis Menewa Naya est dans [plan-migration.md](plan-migration.md).

| Phase | Contenu | Durée estimée | État |
|---|---|---|---|
| 0 | Commits sur une branche, nettoyage (retrait du module `projects`) | ½ j | ✅ fait (20/11/2026) |
| 1 | Séparation back/front : contrat OpenAPI, référentiel `/api/v1/reference`, génération et contrôle des clients, règles d’affichage déplacées dans l’API | 2–3 j | ✅ fait (20/11/2026) |
| 2 | Qualité : tests par module et tests d’autorisation sur PostgreSQL, tests de bout en bout Playwright, `flutter analyze`/`flutter test`, modèles Dart générés, CI GitHub Actions | 4–5 j | ✅ fait (21/11/2026) |
| 3 | Fonctionnel : mobile au niveau du web (services, campagnes, publications, litiges), relances d’échéances planifiées, notifications asynchrones, renouvellement de session mobile, photos de l’accueil, choix paiement / SMS / e-mail | 5–8 j | en partie (21/11/2026) : tout sauf les photos de l’accueil et le choix des prestataires (décisions attendues) |
| 4 | Production : domaine et OTP réel par SMS (**reportés : phase de test en cours, décision du 21/11/2026**), suppression des protections de démo, ~~limitation par IP~~, sauvegardes automatiques (~~quotidiennes~~, copie hors serveur à brancher), surveillance, stockage objet | 3–4 j | en partie (21/11/2026) : limitation par IP, sauvegardes quotidiennes, ménage des sessions |
| 5 | Reprise des données réelles d’eleos (si elles existent) : répétition, réconciliation, bascule (`sample/` est conservé comme sauvegarde de l’ancien code) | 1–2 j | à faire |
| 6 | Publication mobile : icônes, signature, Play Store / App Store, tests internes | 3–5 j | à faire |

## Phase 1 — ce qui a été livré

- **Contrat publié par le backend** : OpenAPI 3.0 sur `/v3/api-docs`, exporté dans `docs/api/openapi.json`. Champs toujours présents, `nullable` exact, noms de schémas uniques (garde-fou dans `ContractExportTests`).
- **Référentiel unique** `GET /api/v1/reference` : statuts et tonalités, catégories, rôles, permissions, moyens de paiement, services annexes… Les libellés ne sont plus recopiés dans le web (`packages/ui`) ni dans le mobile : ils sont générés.
- **`pnpm contract`** :
  - génère `openapi.generated.ts`, `reference.generated.ts` et `reference.g.dart` ;
  - vérifie les appels des clients TypeScript (114) et Dart (35) contre les 130 opérations du contrat ;
  - `pnpm contract:check` échoue aussi si un fichier généré est périmé.
- **Types du front vérifiés à la compilation** contre les schémas générés (`contract.check.ts`, 74 types de réponse).
- **Plus de règle métier dans le front** : les textes d’aide des conditions de clôture d’un don sont calculés par l’API.
- **Structure** :
  - `backend/` : API, données, migrations, scripts de reprise ;
  - `apps/{admin,saas,mobile}` : interfaces ;
  - `packages/` : client, types, composants ;
  - `deploy/` et `docs/` ;
  - `sample/` : sauvegarde de l’ancien code d’eleos (Next.js + Drizzle), conservée à titre de référence ; rien ne l’utilise et il n’est ni compilé ni déployé.

### Choix assumés

- **Couche serveur fine conservée dans les apps Next.js** (Server Actions, cookie `HttpOnly`, proxys de fichiers). Le jeton reste hors de portée du JavaScript du navigateur. Cette couche ne contient aucune règle métier.
- **Types de réponse écrits à la main mais vérifiés, plutôt que remplacés par les types générés.** Les types générés déclarent « peut-être nul » tout champ Java non primitif, même un identifiant ; les remplacer ferait perdre la précision du front. La vérification à la compilation détecte malgré tout tout renommage, suppression ou changement de type côté backend.
- **Mobile : libellés générés et appels vérifiés.** Modèles Dart générés en phase 2 (voir ci-dessous).

## Phase 2 — ce qui a été livré

- **Backend sur un vrai PostgreSQL 16** (embarqué, sans Docker) au lieu de H2 : 39 tests.
  - `AuthorizationTests` : 16 routes d’administration × 5 profils (anonyme 401, membre 403, administrateur secondaire sans la permission 403, avec 200, administrateur principal 200) ; propriété des demandes, paiements, litiges, missions et fichiers de preuve ; coordonnées du bénéficiaire masquées avant acceptation ; `roles.manage` réservé ; révocation à la déconnexion.
  - `BusinessRulesTests` : un groupe par module (identité, dons, Mena, communauté).
- **Tests de bout en bout Playwright** (`e2e/`) : site public, protection de l’espace, inscription d’un donateur par OTP, don depuis le catalogue, acceptation par l’administration, notification reçue. Validés contre la zone de dev le 21/11/2026 (3/3).
- **Mobile** :
  - `pnpm contract` génère `apps/mobile/lib/core/api.g.dart`, un modèle typé par schéma OpenAPI (131) ;
  - les modèles des écrans s’appuient dessus : un champ renommé côté API fait échouer `flutter analyze` ;
  - `flutter analyze` ne signale aucun problème et `flutter test` passe ses 6 tests.
- **CI GitHub Actions** : jobs backend (tests + contrat à jour), web (contrat, types, lint, build), mobile (analyze, test) ; bout en bout au déclenchement manuel.

Reste pour la phase 3 : les écrans mobiles lisent encore certaines réponses (tableaux de bord, détails) par clé. Ils passeront aux modèles générés au fil de leur refonte.

## Phase 3 — ce qui a été livré (21/11/2026)

- **Relances d’échéances** (`GivingReminders`) : chaque jour à 8 h, heure de Lomé, le donateur et les gestionnaires reçoivent :
  - un rappel avant l’échéance (délai : paramètre `requests.late_alert_days`) ;
  - une relance unique en cas de retard (migration V13).
  Avant, le scan ne tournait que si un administrateur ouvrait son tableau de bord.
- **SMS en file d’envoi** (`SmsOutbox`, migration V12) :
  - écrits dans la même transaction que l’action, envoyés en arrière-plan ;
  - réessayés après 1 min, 5 min, 30 min et 2 h, puis abandonnés ;
  - suivis dans l’administration, page Paramètres, section « Envois SMS ».
  Une passerelle lente ou en panne ne bloque plus aucune action. Les codes de connexion restent envoyés immédiatement.
- **Renouvellement de session** : `POST /api/auth/refresh` délivre un nouveau jeton et invalide l’ancien. Le mobile renouvelle au démarrage si la session expire dans moins de 7 jours.
- **Mobile au niveau du web** :
  - services communautaires : recherche de prestataire, demande, négociation, étapes, avis ;
  - campagnes : contribution, soutien en Mena ;
  - publications du partenaire ;
  - litiges : consultation depuis la demande, réponse avec photo.
  La navigation est organisée par rôle. Les nouveaux écrans utilisent les modèles générés.
- **Reste, en attente de décisions** :
  - les photos de l’accueil (`hero.jpg`, `kitchen.jpg`, `network.jpg`, `man.jpg`, `woman.jpg`) ;
  - le choix des prestataires de paiement en ligne, de SMS et d’e-mail.

## Flux de travail

1. Modifier le backend (endpoint, champ, libellé).
2. `cd backend && ./gradlew test` : tests et export du contrat.
3. `pnpm contract` : génération, puis contrôle des appels clients.
4. Typecheck des fronts (`tsc --noEmit` dans chaque package) : la vérification des types signale les écarts.
