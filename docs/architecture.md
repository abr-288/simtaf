# Architecture

## Vue d’ensemble

Le dépôt contient cinq livrables indépendants : Flutter pour iOS/Android, une API Spring Modulith, un portail admin Next.js, un portail SaaS Next.js et la documentation.

```text
mobile ──┐
admin ───┼──> API Spring Modulith ──> PostgreSQL
saas ────┘
```

Les applications web consomment le contrat REST. Les modules métier backend sont organisés par domaine et gardent leurs entités et services internes. `shared-types` et `api-client` sont partagés entre les frontends web uniquement.

## Premier module : projets

Le module `com.menewa.projects` possède le modèle `Project`, sa persistance et les routes `GET /api/projects` et `POST /api/projects`. Flyway crée le schéma via `V1__create_projects.sql`. Le SaaS lit la liste côté serveur et son formulaire crée un projet via une Server Action, en utilisant le client partagé. La configuration du backend côté serveur Next.js se fait avec `API_BASE_URL` (par défaut `http://localhost:8080/api`).
