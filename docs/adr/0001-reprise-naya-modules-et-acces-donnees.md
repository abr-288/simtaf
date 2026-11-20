# 0001 — Reprise de Menewa Naya : découpage en modules et accès aux données

## Contexte

L’application `sample/` (eleos-site4, Next.js + Drizzle) a été portée vers le monorepo : admin et espace membre Next.js, parcours MVP Flutter, règles métier dans l’API Spring Modulith. Le plan de migration proposait un module par domaine (giving, missions, proofs, finance, disputes…). Plusieurs invariants lient pourtant ces domaines dans une même transaction : un don n’est « réalisé » que si ses preuves sont validées, ses missions terminées et ses échéances faites ; valider une mission valide aussi la rémunération du prestataire.

## Décision

1. **Sept modules** : `identity`, `platform`, `catalog`, `rewards`, `providers`, `giving`, `community`. Demandes, missions, preuves, paiements et litiges restent ensemble dans `giving` tant que leurs invariants sont couplés ; ils pourront être séparés plus tard via des événements.
2. **`identity` sans dépendance** : il publie des événements (inscription, connexion, changement de rôle, actions d’administration) écoutés de façon synchrone par `platform`, `rewards` et `providers`.
3. **JdbcClient** pour les nouveaux modules (SQL explicite, proche des requêtes Drizzle d’origine, sans entités JPA partagées) ; `identity`, `catalog` et `projects` gardent JPA. Flyway reste l’unique propriétaire du schéma (`ddl-auto: validate`, y compris en test).
4. **Tables préfixées par module**, aucune clé étrangère entre modules, identifiants legacy conservés (script `backend/scripts/import-legacy-operations.sql`).
5. **Chemins web conservés** (`/admin/*`, `/espace/*`) pour la compatibilité des liens déjà enregistrés dans les notifications.

## Conséquences

- Les frontends n’ont plus aucun accès à la base ; les règles d’autorisation sont toutes appliquées par l’API.
- `ModularityTests` garantit l’absence de cycles ; `GivingFlowTests` couvre le parcours complet (panier → décision → mission → preuve → validation → clôture → Mena).
- Les écouteurs synchrones font échouer la transaction métier si l’audit échoue : acceptable pour l’audit, à revoir (événements asynchrones `@ApplicationModuleListener`) quand les notifications passeront par une file.
- Le scan des échéances proches se déclenche à l’ouverture du tableau de bord admin, comme dans Naya ; une planification (`@Scheduled`) reste à ajouter.
