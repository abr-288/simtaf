# Zone de dev Menewa (VPS mutualisé)

Déploie PostgreSQL, l’API, la console d’administration et l’espace membre dans `/opt/menewa`. L’ensemble est servi par le reverse-proxy Caddy partagé du VPS (`/srv/proxy`) :

- HTTPS automatique ;
- noms gratuits `sslip.io`, qui résolvent vers l’IP du serveur ;
- mot de passe d’accès (authentification HTTP, utilisateur `menewa`).

PostgreSQL et l’API restent sur le réseau Docker interne `menewa_internal`. Seuls `menewa-saas` et `menewa-admin` rejoignent le réseau `proxy`.

> Zone de démonstration : les codes OTP s’affichent à l’écran. N’y mettez jamais de vraies données personnelles.

| Adresse | Conteneur |
|---|---|
| `https://menewa.<ip-avec-tirets>.sslip.io` | `menewa-saas` (espace membre) |
| `https://menewa-admin.<ip-avec-tirets>.sslip.io` | `menewa-admin` (administration, téléphone `+228 90 00 00 01`) |
| `https://api-menewa.<ip-avec-tirets>.sslip.io/api` | `menewa-backend` (application mobile ; en-tête `X-Menewa-Key` = `MOBILE_API_KEY`, seul `/api/*` est relayé) |

## Déployer ou mettre à jour

```sh
cd /opt/menewa/deploy/dev && ./deploy.sh
```

Au premier lancement, le script :

1. génère `.env` (secrets aléatoires) et `.access-password` (mot de passe d’accès, affiché une seule fois) ;
2. construit et démarre les conteneurs ;
3. ajoute le bloc `Caddyfile.snippet` à `/srv/proxy/Caddyfile`, après une sauvegarde `Caddyfile.avant-menewa-<date>`.

Le Caddyfile est validé avant tout rechargement ; s’il est invalide, la sauvegarde est restaurée. Pour mettre à jour, copiez le nouveau code dans `/opt/menewa` et relancez le script : le bloc Caddy n’est ajouté qu’une seule fois.

## Données de démonstration

`./seed-demo.sh` charge les mêmes données que Menewa Naya : comptes `+228 90 00 00 01` à `08`, les 11 dons du catalogue, 3 prestataires, 2 campagnes, la famille Mensah et une demande d’exemple. Le script est idempotent.

## Exploitation

```sh
cd /opt/menewa/deploy/dev
docker compose --env-file .env logs -f backend   # journaux de l'API
docker compose --env-file .env down              # arrêt (données conservées)
docker compose --env-file .env down -v           # arrêt et remise à zéro des données
```

## Application Android de test

- `sudo ./build-apk.sh` compile l'APK branchée sur l'API de la zone de dev et la publie sur `https://<SAAS_HOST>/telechargement/menewa-test.apk`, derrière le mot de passe d'accès. La source utilisée est `apps/mobile`, à copier au préalable dans `/opt/menewa/apps/mobile`.
- La clé de signature de test est créée au premier lancement dans `/opt/menewa-mobile-keys`, lisible par root seulement. La conserver permet d'installer chaque nouvelle version par-dessus la précédente ; la perdre oblige à désinstaller l'app.
- La clé d'application de l'API (`MOBILE_API_KEY`) est intégrée à l'APK. Elle ne protège que la zone de test et n'a rien d'un secret fort.
- Installation : ouvrir le lien sur le téléphone Android, se connecter avec le mot de passe d'accès, puis autoriser l'installation depuis le navigateur (« sources inconnues »).

## Sauvegardes

- `backup.sh` sauvegarde la base (`pg_dump`, format personnalisé, contrôlé par `pg_restore --list`) et les fichiers déposés (volume `menewa_storage`) dans `/var/backups/menewa`, lisible par root seulement. Il conserve 14 jours de sauvegardes.
- `deploy.sh` le planifie chaque nuit à 02:30 (`/etc/cron.d/menewa-backup`). Le journal est dans `/var/log/menewa-backup.log`.
- **Copie hors serveur :** définir `BACKUP_RSYNC_TARGET=utilisateur@machine:/dossier` dans `.env`, avec une clé SSH de root autorisée sur la machine cible. Sans cela, les sauvegardes disparaîtraient avec le serveur ; le journal le signale chaque nuit.
- Lancer une sauvegarde à la main : `sudo ./backup.sh`.
- Vérifier qu'une sauvegarde se restaure, dans une base temporaire et sans toucher au service : `sudo ./restore.sh /var/backups/menewa/menewa-db-….dump`.
- Remplacer la base en service, après une confirmation et une sauvegarde de l'état actuel : `sudo ./restore.sh <fichier> --production`.

## Protections et ménage automatiques (API)

- **Demandes de code :** au plus 3 par numéro en 10 minutes, et 30 par adresse IP par heure (`OTP_MAX_REQUESTS_PER_IP`).
- **Codes erronés :** au plus 5 essais par code, et 30 échecs par adresse IP par heure (`OTP_MAX_FAILURES_PER_IP`).
- Les limites par IP sont larges, car beaucoup d'abonnés mobiles partagent la même adresse. L'IP réelle est transmise par Caddy, puis relayée par les apps web.
- **Ménage horaire :** suppression des sessions expirées, des codes de plus de 24 h et des compteurs d'IP périmés.

Pour retirer Menewa du proxy, supprimez ses deux blocs dans `/srv/proxy/Caddyfile`, puis lancez `docker exec caddy caddy reload --config /etc/caddy/Caddyfile`.
