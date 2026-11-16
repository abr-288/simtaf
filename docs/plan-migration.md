# Plan de migration de Menewa Naya vers le monorepo

## 1. Objectif

Faire évoluer l’application `sample/`, actuellement fullstack Next.js et Drizzle, vers quatre applications séparées qui partagent un backend métier unique :

- **SaaS Next.js** pour le site public et les espaces des donateurs, clients, prestataires, partenaires et bénéficiaires ;
- **Admin Next.js** pour les opérations de plateforme ;
- **Mobile Flutter** pour les parcours utilisateurs prioritaires ;
- **Backend Spring Boot + Spring Modulith** qui devient le propriétaire exclusif des règles métier, des données et des accès aux fichiers.

La migration se fait par parcours métier verticaux. Chaque parcours doit fonctionner de bout en bout avant de retirer son implémentation de `sample/`. Il n’est pas nécessaire de déplacer toutes les pages ou toutes les tables au même moment.

## 2. État de départ observé

`sample/` contient une application Next.js 16 App Router avec PostgreSQL/Drizzle, Tailwind 4 et React 19. Les routes publiques sont dans `src/app/(public)`, les espaces utilisateur dans `src/app/(app)/espace` et l’administration dans `src/app/(app)/admin`.

Les limites actuelles sont surtout des limites de routage et de dossiers, pas des frontières de services :

- les espaces SaaS et admin partagent le layout authentifié, la session et le processus Next.js ;
- les Server Actions dans `src/app/actions/` contiennent des mutations et importent directement Drizzle ;
- les pages et layouts lisent également la base directement ;
- `src/db/schema.ts` centralise les tables utilisateurs, demandes, missions, preuves, paiements, litiges, services, tokens, publications, notifications, paramètres et audit ;
- `src/lib/auth.ts` établit une session web par cookie `mn_session` ;
- `src/app/api/v1/` expose déjà OTP, catalogue, profil et lecture des demandes, mais ne couvre pas encore toutes les mutations utilisées par les Server Actions ;
- `src/lib/seed.ts` initialise données de démonstration au premier appel ;
- `src/db/index.ts` bascule entre PostgreSQL et PGlite selon la configuration ;
- les fichiers téléversés sont servis par `src/app/api/files/*` avec des contrôles d’accès dans l’application Next.js ;
- la documentation mentionne une future application mobile, qui n’est pas encore dans `sample/`.

Les inventaires et règles métier existants sont décrits dans `sample/docs/ARCHITECTURE.md`, `sample/docs/API.md`, `sample/docs/USER_GUIDE.md` et dans les fonctions de `sample/src/lib/`.

## 3. Architecture cible

```text
Flutter mobile ─────┐
SaaS Next.js ───────┼── HTTPS / JSON ──> API Spring Modulith ──> PostgreSQL
Admin Next.js ─────┘                           │
                                               ├── stockage objet / fichiers
                                               ├── SMS et notifications
                                               └── événements entre modules
```

Règles de frontière :

1. Les trois frontends n’accèdent jamais à PostgreSQL et ne portent pas de règles d’autorisation métier.
2. Le backend est le seul propriétaire du schéma cible. Une donnée n’a qu’un module propriétaire.
3. Les modules backend exposent des API publiques limitées et échangent les effets inter-domaines via événements Spring Modulith lorsque c’est utile.
4. L’admin et le SaaS consomment les mêmes contrats API, avec des permissions différentes appliquées par le backend.
5. Flutter consomme ces contrats également ; aucun code Flutter ne dépend de Drizzle, JPA ou de classes internes du backend.
6. `packages/shared-types` et `packages/api-client` peuvent accélérer les frontends web, mais le contrat transport de référence est OpenAPI. Le backend ne dépend pas de ces packages TypeScript.

## 4. Modules métier backend proposés

| Module | Données et responsabilités principales | Dépendances principales |
|---|---|---|
| `identity` | utilisateurs, OTP, sessions, rôles, permissions, profil | aucune ; fondation |
| `catalog` | catalogue, catégories, prix, cibles | `identity` pour les opérations admin |
| `giving` | panier, demandes, éléments, échéances, historique, statuts | `identity`, `catalog` |
| `providers` | profils prestataires, services, disponibilité | `identity` |
| `missions` | missions, affectation, acceptation/refus, réattribution | `giving`, `providers` |
| `proofs` | métadonnées, dépôt et validation des preuves | `giving`, `missions`, `identity`, stockage |
| `finance` | paiements, échéanciers, reçus, coûts, marges | `giving`, `missions`, `identity` |
| `community` | commandes de services, messages, avis | `identity`, `providers` |
| `partners` | publications, campagnes, contributions | `identity` |
| `disputes` | litiges, messages, décisions et clôture | `identity`, références métier publiques |
| `rewards` | règles et écritures du Token Mena, badges | `identity`, événements métier |
| `notifications` | notifications in-app et adaptateurs SMS/e-mail | abonnements aux événements métier |
| `audit` | journal des actions sensibles | événements et commandes métier |
| `settings` | paramètres opérationnels modifiables | `identity` pour les permissions |
| `reporting` | statistiques et exports | vues/API publiques des modules concernés |

Ce découpage est une cible de travail, à ajuster avec les invariants métier. Commencer par peu de modules cohérents ; éviter un module par table ou le partage d’entités JPA entre modules. Les références inter-domaines devraient passer par des identifiants et des API ou événements explicites.

## 5. Ordre de migration par feature

Chaque ligne est une tranche verticale : contrat, backend, migration des données si nécessaire, frontend, sécurité et validation. Les noms de branches proposés suivent `feature/migration/<feature>`.

### F0 — inventaire et contrat de référence

**Branche :** `feature/migration/inventory-api-contract`

- Cartographier toutes les pages, Server Actions, lectures Drizzle, rôles, permissions, fichiers et variables d’environnement.
- Relier chaque action à ses tables et à ses règles dans `sample/src/lib/`.
- Compléter `sample/docs/API.md` en OpenAPI : schémas, erreurs, filtres, pagination, droits et idempotence.
- Définir les conventions d’identifiants, dates UTC, monnaies (FCFA/XOF), statuts, pagination et versionnement `/api/v1`.
- Créer des exemples de requêtes réutilisables dans `sample/` ou `docs/`.

**Sortie :** inventaire relu et contrats API acceptés avant extraction des premières pages.

### F1 — socle identité, OTP et permissions

**Branche :** `feature/migration/identity-auth`

- Porter utilisateurs, OTP, sessions, normalisation téléphone, limitation d’envoi/tentatives et permissions de `auth.ts`, `rbac.ts` et `sms.ts` dans le backend.
- Ajouter endpoints de demande/validation OTP, déconnexion, session courante et rafraîchissement/révocation.
- Pour le web, choisir une session BFF sécurisée ou un cookie de domaine adapté ; pour Flutter, préférer des jetons d’accès courts et des jetons de renouvellement révocables stockés dans le trousseau sécurisé.
- Ne pas exposer l’OTP de démonstration hors environnement local ; retirer le secret par défaut avant tout déploiement.
- Remplacer les accès à `getCurrentUser()` côté frontends par la session/API et garder les guards de navigation comme confort seulement.

**Dépendances :** F0. **Sortie :** mêmes comptes et permissions utilisables par SaaS, admin, mobile et API.

### F2 — site public et catalogue

**Branche :** `feature/migration/catalog-public`

- Déplacer le catalogue et paramètres publics vers `catalog`/`settings`.
- Exposer liste, détail, catégories et disponibilité du catalogue ; protéger création, édition, prix et activation par `catalog.manage`.
- Connecter les pages publiques, `espace/catalogue` et le parcours de lecture Flutter au contrat commun.
- Déplacer les contenus non transactionnels du site public vers le SaaS ; garder les données modifiables en base si elles le sont déjà.

**Dépendances :** F1. **Sortie :** les trois clients lisent la même source de catalogue.

### F3 — parcours donateur : panier et création de demande

**Branche :** `feature/migration/giving-create-request`

- Porter `cart_items`, `requests`, `request_items`, calculs de total, récurrence et validation de cible vers `giving`.
- Créer les commandes d’ajout/retrait panier, aperçu/prix et création idempotente de demande.
- Vérifier à nouveau identité, rôle, propriétaire et prix dans le backend ; ne jamais accepter un total calculé par le navigateur.
- Connecter le SaaS, puis Flutter, au même flux. Remplacer `checkoutCart` et `createCustomRequest` par appels API avant de supprimer leurs implémentations.

**Dépendances :** F1, F2. **Sortie :** une demande est persistée depuis le web et le mobile avec les mêmes règles.

### F4 — consultation et cycle de vie des demandes

**Branche :** `feature/migration/giving-request-lifecycle`

- Porter `request_history`, états, annulation, réponse aux demandes d’information et clôture.
- Exposer liste paginée filtrée par propriétaire et vues autorisées pour l’admin.
- Garantir transitions d’état valides côté backend et journaliser les changements.
- Migrer les pages `/espace/demandes*` vers le SaaS et les vues de traitement vers l’admin.

**Dépendances :** F3. **Sortie :** demandes consultables et transitions protégées côté serveur.

### F5 — administration des demandes et bénéficiaires

**Branche :** `feature/migration/admin-requests-beneficiaries`

- Porter décision accepter/refuser/demander des informations, affectation du bénéficiaire et permissions associées.
- Isoler les données personnelles du bénéficiaire ; contrôler chaque lecture et tracer les consultations sensibles.
- Sortir les pages `/admin/demandes`, `/admin/beneficiaires`, `/admin/cibles` dans l’app admin.

**Dépendances :** F1, F4. **Sortie :** l’admin traite les demandes par API sans accès direct à la base.

### F6 — prestataires et missions

**Branche :** `feature/migration/providers-missions`

- Porter profils/services prestataires et missions, affectation, acceptation/refus motivé, réattribution et échéances.
- Appliquer les permissions d’administration et filtrer les données visibles au prestataire.
- Déplacer les parcours admin vers l’app admin et les parcours prestataire vers le SaaS et Flutter.
- Publier des événements pour notifications et audit plutôt que faire dépendre directement tous les modules.

**Dépendances :** F1, F4, F5. **Sortie :** cycle de mission complet entre admin et prestataire.

### F7 — preuves et stockage

**Branche :** `feature/migration/proofs-storage`

- Extraire le contrôle du dépôt de `src/lib/storage.ts` et des routes `/api/files/*`.
- Conserver les fichiers hors du webroot ; utiliser une clé opaque et contrôler rôle, propriétaire et statut à chaque téléchargement.
- Migrer les fichiers existants avec manifeste, taille, type MIME et empreinte ; ne pas exposer les chemins locaux.
- Prévoir stockage objet privé avec URL signée courte ou proxy backend après vérification des droits.
- Porter contrôle admin, visibilité donateur et conditions de validation avant clôture d’un don.

**Dépendances :** F1, F4, F6. **Sortie :** transfert, validation et lecture sécurisés sur web et mobile.

### F8 — paiements et finances

**Branche :** `feature/migration/finance-payments`

- Porter paiements entrants/sortants, justificatifs, validation, paiements partiels, échéances, coûts et calcul de marge.
- Utiliser des opérations transactionnelles, idempotence des références et audit des changements financiers.
- Ne jamais supprimer les écritures financières ; utiliser annulation/compensation explicite.
- Différer l’intégration d’un agrégateur de paiement jusqu’à validation du fournisseur et des exigences réglementaires.
- Migrer les écrans SaaS et admin, puis les reçus adaptés au mobile.

**Dépendances :** F1, F3, F4, F6, F7. **Sortie :** rapprochement des montants et historique traçable.

### F9 — services communautaires, avis et partenaires

**Branche :** `feature/migration/community-partners`

- Extraire `service_orders`, messages, avis, listings, campagnes et contributions dans les modules `community` et `partners`.
- Définir modération, statut de publication, consentement et règles de contribution avant ouverture au public.
- Migrer parcours client/prestataire et partenaire dans SaaS, admin et mobile selon leur rôle.

**Dépendances :** F1, F2, F6. **Sortie :** échanges, campagnes et modération accessibles via API.

### F10 — litiges, tokens et notifications

**Branche :** `feature/migration/disputes-rewards-notifications`

- Porter les litiges et messages avec contrôle des parties et décisions auditées.
- Porter les tokens vers un ledger append-only, avec anti-doublon, plafond, motif et règles d’attribution ; conserver les transferts/conversions désactivés jusqu’à décision produit/juridique.
- Consommer les événements des modules métier pour notifications in-app/SMS/e-mail et éviter les appels croisés synchrones superflus.
- Reprendre les préférences de notification et l’historique.

**Dépendances :** F1, F4, F6, F8, F9. **Sortie :** les événements importants déclenchent des notifications sans faire échouer la transaction métier en cas de panne SMS.

### F11 — rôles, paramètres, audit, statistiques et export

**Branche :** `feature/migration/admin-operations-reporting`

- Porter gestion des rôles/permissions, settings, audit et exports de `admin.ts`, `settings.ts`, `audit.ts`, `stats.ts`.
- Réserver les permissions les plus sensibles à l’admin principal et tracer les actions de support.
- Construire des vues de reporting non mutantes et borner les exports par filtres et permission.
- Migrer les pages admin restantes après disponibilité des contrats métiers.

**Dépendances :** F1 et modules migrés. **Sortie :** administration complète sans requête Drizzle côté frontend.

### F12 — application mobile Flutter

**Branche :** `feature/migration/mobile-flutter`

- Initialiser l’app Flutter du monorepo avec navigation, configuration d’environnement, gestion de session et client API typé/généré.
- Commencer par OTP, catalogue, panier/demande, suivi et notifications ; ajouter les parcours prestataire selon la priorité produit.
- Gérer renouvellement/révocation de session, erreurs réseau, reprise hors ligne limitée, upload de preuves et deep links.
- Ne pas réimplémenter les permissions métier dans Flutter : afficher selon le rôle, puis respecter les refus renvoyés par l’API.

**Dépendances :** F1 à F4 au minimum. **Sortie :** parcours MVP mobile sur backend partagé.

## 6. Migration et propriété des données

1. Geler le modèle cible et créer les migrations versionnées dans le backend. Ne pas utiliser `drizzle-kit push` comme stratégie de production.
2. Cartographier chaque table Drizzle vers un module et un schéma cible. Préserver autant que possible les identifiants métier, références `MN-*`, dates UTC et liens d’historique.
3. Ajouter contraintes FK, uniques et index manquants après nettoyage des données orphelines ; la migration doit signaler les lignes rejetées au lieu de les ignorer silencieusement.
4. Migrer par lots reproductibles avec compteurs, empreintes et journal des erreurs. Importer les données de référence avant les demandes, missions, preuves, paiements et références dépendantes.
5. Migrer les fichiers avec contrôle MIME/taille, empreinte et lien vers l’identifiant conservé ; valider le nombre de fichiers et leur lisibilité avant bascule.
6. Pendant une phase courte, mettre le frontend en lecture seule sur les fonctions basculées ou utiliser un adaptateur de compatibilité. Éviter les doubles écritures non transactionnelles entre Drizzle et Spring.
7. Comparer comptages, totaux financiers, soldes de tokens, statuts et échantillons de relations. Faire une répétition de migration et une restauration avant production.
8. Basculer les routes feature par feature, mesurer erreurs/latence, garder un rollback vers l’ancienne app tant que les écritures peuvent être restaurées sans perte.
9. Une fois une feature stabilisée, supprimer son accès Drizzle, ses Server Actions et ses tables/logiciels legacy seulement après sauvegarde et validation métier.

## 7. Sécurité et conformité à intégrer

- Secrets hors dépôt ; aucun OTP de démo ni secret de fallback en production.
- Limitation de débit par téléphone/IP, messages d’erreur non révélateurs, expiration/révocation et rotation des sessions.
- Vérification serveur des rôles, permissions, propriété des objets et transitions d’état sur chaque endpoint.
- Validation de schéma d’entrée, taille des uploads, MIME réel, antivirus si nécessaire, URLs privées et journal d’accès aux données sensibles.
- Politique de conservation et consentement pour les données et images des bénéficiaires.
- Audit immuable des décisions d’administration, opérations financières, ajustements de tokens et exports.
- CORS limité aux origines prévues ; CSRF selon stratégie cookie ; TLS et en-têtes de sécurité au reverse proxy.
- Revue des décisions réglementaires listées dans `sample/docs/ARCHITECTURE.md` avant l’activation des paiements et des transferts/conversions Mena.

## 8. Tests et critères d’acceptation pour chaque feature

Chaque branche doit fournir :

- tests unitaires de règles métier et tests d’intégration repository/API sur PostgreSQL (H2 peut servir aux tests rapides, pas à valider les particularités PostgreSQL) ;
- tests d’autorisations : accès anonyme, rôle non autorisé, propriétaire différent, rôle admin limité et admin principal ;
- contrat OpenAPI validé et exemple `curl`/requête `.http` mis à jour ;
- build et lint du frontend concerné, typecheck web, compilation backend ;
- tests E2E du parcours utilisateur lorsqu’il est migré ;
- migration réversible ou stratégie explicite de restauration des données ;
- aucune lecture/écriture Drizzle résiduelle sur le parcours déclaré migré ;
- documentation et variables d’environnement mises à jour.

Les vérifications globales de livraison comprennent `pnpm install --frozen-lockfile`, `pnpm build`, `pnpm lint`, `pnpm typecheck`, `./gradlew build`, `flutter analyze` et les builds mobile ciblés par CI.

## 9. Stratégie Git par feature

La branche de cadrage actuelle est `feature/migration-monorepo`. Créer ensuite une branche isolée par tranche verticale, depuis la branche d’intégration convenue :

```text
feature/migration/inventory-api-contract
feature/migration/identity-auth
feature/migration/catalog-public
feature/migration/giving-create-request
feature/migration/giving-request-lifecycle
feature/migration/admin-requests-beneficiaries
feature/migration/providers-missions
feature/migration/proofs-storage
feature/migration/finance-payments
feature/migration/community-partners
feature/migration/disputes-rewards-notifications
feature/migration/admin-operations-reporting
feature/migration/mobile-flutter
```

Chaque branche porte son code, ses migrations, ses contrats et sa documentation ensemble. Fusionner les branches dans l’ordre de dépendance du plan ; ne pas fusionner une app qui dépend d’endpoints non livrés. Les changements transverses de contrat sont d’abord fusionnés dans la branche d’intégration, puis rebasés ou fusionnés dans les branches feature.

## 10. Jalons de livraison

1. **Fondations :** inventaire, contrat, sécurité et stratégie de migration de données validés.
2. **Premier parcours :** identité, catalogue et création/consultation d’une demande disponibles via le backend et le SaaS.
3. **Opérations :** admin traite une demande et missionne un prestataire ; les preuves sont contrôlées.
4. **Finances et communauté :** paiements, services, partenaires et litiges migrés avec audit.
5. **Mobile :** parcours MVP Flutter connecté aux mêmes endpoints.
6. **Bascule :** réconciliation finale, migration fichiers, répétition de restauration, bascule progressive, suivi et retrait de Drizzle legacy.

La durée et le périmètre de chaque jalon doivent être estimés après F0. Le planning de huit semaines présent dans `sample/docs/Planning-Gantt-8-semaines-Menewa-Naya.docx` est un support initial, pas une estimation validée de cette migration.
