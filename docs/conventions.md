# Conventions

- Nommez les modules métier selon le domaine, pas selon la couche technique.
- Gardez les frontières Modulith vérifiables et testez-les avec `ApplicationModules.verify()`.
- Centralisez les appels HTTP web dans `packages/api-client` et les types dans `packages/shared-types`.
- Placez les variables sensibles dans l’environnement ; ne les commitez jamais.
- Documentez les changements d’architecture durables dans `docs/adr/`.
