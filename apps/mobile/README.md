# Menewa Mobile

Application Flutter (iOS/Android) branchée sur l’API Menewa. Lancez `flutter pub get` puis `flutter run`. Adaptez l’URL de l’API avec `--dart-define=API_BASE_URL=...` (émulateur Android : `http://10.0.2.2:8080/api`, simulateur iOS : `http://localhost:8080/api`, appareil physique : adresse LAN du poste).

## Zone de dev publique

```sh
flutter run --dart-define=API_BASE_URL=https://api-menewa.169-58-93-234.sslip.io/api --dart-define=API_KEY=<clé>
```

La clé d'application (`MOBILE_API_KEY` dans `/opt/menewa/deploy/dev/.env` sur le serveur) est envoyée dans l'en-tête `X-Menewa-Key`. Sans elle, le proxy répond 401. Ne la publiez pas : elle protège une API en mode démo, où les codes OTP sont renvoyés à l'écran.

## Parcours MVP

- Connexion / inscription par téléphone et code SMS (jeton conservé dans le trousseau sécurisé).
- Donateur / client : tableau de bord et solde Mena, catalogue avec cible, panier et validation (échéancier, services annexes), suivi des demandes (étapes, preuves validées, échéancier, historique), déclaration de paiement, réponse aux demandes d’informations, annulation, avis, signalement d’un litige.
- Prestataire : missions proposées, acceptation ou refus motivé, démarrage, preuve photo (appareil ou galerie), fin de mission.
- Partenaire et bénéficiaire : tableau de bord ; les publications partenaires se gèrent sur le web.
- Tous : notifications (liens vers la demande ou la mission), profil, préférences de notification, Token Mena, déconnexion.

Les droits ne sont pas réimplémentés dans l’application : elle affiche les refus renvoyés par l’API. Les comptes administrateurs sont redirigés vers la console web.

## Vérification

`flutter analyze` et `flutter test`. Le code n’a pas encore été compilé dans l’environnement de migration (Flutter absent) : lancez ces deux commandes avant la première livraison.
