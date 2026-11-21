# Déploiement de la zone de dev Menewa

Ce kit déploie PostgreSQL, l'API (Spring), la console d'administration et l'espace membre (Next.js) sur un VPS mutualisé. Il compile aussi l'application Android de test. Le reverse-proxy Caddy du serveur (`/srv/proxy`) est partagé avec d'autres sites. Il apporte :

- le HTTPS automatique ;
- des noms gratuits `sslip.io`, qui résolvent vers l'IP du serveur ;
- un mot de passe d'accès (authentification HTTP, utilisateur `menewa`) sur les deux sites web ;
- une clé d'en-tête `X-Menewa-Key` sur l'API mobile.

> **Zone de test.** Les codes OTP s'affichent à l'écran et aucun SMS n'est envoyé. N'y mettez jamais de vraies données personnelles.

## Sommaire

1. [Architecture](#architecture)
2. [Prérequis](#prérequis)
3. [Première installation](#première-installation)
4. [Mettre à jour](#mettre-à-jour)
5. [Revenir à la version précédente](#revenir-à-la-version-précédente)
6. [Vérifier après un déploiement](#vérifier-après-un-déploiement)
7. [Application Android de test](#application-android-de-test)
8. [Sauvegardes et restauration](#sauvegardes-et-restauration)
9. [Exploitation courante](#exploitation-courante)
10. [Configuration (`.env`)](#configuration-env)
11. [Protections de l'API](#protections-de-lapi)
12. [Dépannage](#dépannage)
13. [Retirer Menewa du serveur](#retirer-menewa-du-serveur)

## Architecture

| Adresse | Conteneur | Accès |
|---|---|---|
| `https://menewa.<ip-avec-tirets>.sslip.io` | `menewa-saas` (espace membre et site public) | mot de passe d'accès |
| `https://menewa-admin.<ip-avec-tirets>.sslip.io` | `menewa-admin` (administration) | mot de passe d'accès, puis téléphone `ADMIN_PHONE` |
| `https://api-menewa.<ip-avec-tirets>.sslip.io/api` | `menewa-backend` (pour l'application mobile) | en-tête `X-Menewa-Key` ; seul `/api/*` est relayé |
| — | `menewa-db` (PostgreSQL 16) | réseau interne seulement |

- Le projet Docker Compose s'appelle toujours `menewa`. Les volumes (`menewa_postgres-data`, `menewa_storage`, `menewa_downloads`) ne dépendent donc pas du dossier : ils survivent au remplacement de `/opt/menewa`.
- PostgreSQL reste sur le réseau interne `menewa_internal`. Le backend, l'admin et l'espace membre rejoignent aussi le réseau externe `proxy` de Caddy.
- Les frontends n'accèdent jamais à la base : ils appellent l'API (`http://backend:8080/api`).

| Fichier | Rôle |
|---|---|
| `deploy.sh` | installation et mise à jour : `.env`, build, démarrage, blocs Caddy, tâche cron de sauvegarde |
| `docker-compose.yml` | les quatre services |
| `web.Dockerfile` | image des apps Next.js (`APP=admin` ou `APP=saas`) |
| `Caddyfile.snippet`, `Caddyfile.api.snippet` | blocs ajoutés une seule fois au Caddyfile partagé |
| `seed-demo.sh`, `seed-demo.mjs` | données de démonstration |
| `build-apk.sh` | APK Android de test |
| `backup.sh`, `restore.sh` | sauvegarde et restauration |

## Prérequis

Sur le serveur :

- Docker et le plugin Compose. L'utilisateur qui déploie doit être dans le groupe `docker` et avoir `sudo`.
- Le Caddy partagé : un conteneur nommé `caddy`, son Caddyfile dans `/srv/proxy/Caddyfile` et le réseau Docker externe `proxy`. **Ne lancez jamais de second proxy** : les ports 80 et 443 sont déjà pris.
- De la mémoire libre pendant les builds (Gradle, Next.js, Flutter) : lancez-en un seul à la fois.

Sur le poste qui prépare la livraison : `git` et un accès SSH par clé au serveur.

Serveur actuel : `ubuntu@169.58.93.234`, avec la clé `id_ed25519` du poste de dev et `sudo` sans mot de passe.

Ce serveur héberge d'autres sites (bossiz, yedi…). Tenez `/srv/README.md` à jour quand vous touchez au proxy.

## Première installation

```sh
# 1. Sur le poste, depuis la racine du dépôt : archive du code commité (sans sample/, l'ancien code).
#    Chemin relatif pour l'archive : le tar de Git Bash ne comprend pas les chemins « C:\… ».
git -c core.autocrlf=false archive --format=tar.gz -o ../menewa.tar.gz HEAD -- . ':(exclude)sample'
scp ../menewa.tar.gz ubuntu@<serveur>:/tmp/ && rm ../menewa.tar.gz

# 2. Sur le serveur
sudo install -d -o "$USER" -g "$USER" /opt/menewa
tar -xzf /tmp/menewa.tar.gz -C /opt/menewa && rm /tmp/menewa.tar.gz
cd /opt/menewa/deploy/dev && ./deploy.sh

# 3. Données de démonstration (facultatif)
./seed-demo.sh
```

Au premier lancement, `deploy.sh` :

1. génère `.env` (secrets aléatoires, noms d'hôte calculés à partir de l'IP publique) et `.access-password`. Le mot de passe d'accès n'est affiché qu'une seule fois ; notez-le ;
2. construit les images et démarre les conteneurs ;
3. ajoute les deux blocs Caddy à `/srv/proxy/Caddyfile`, après une sauvegarde `Caddyfile.avant-menewa-<date>`. Le Caddyfile est validé avant le rechargement. S'il est invalide, la sauvegarde est restaurée et le proxy reste tel quel ;
4. planifie la sauvegarde nocturne (`/etc/cron.d/menewa-backup`).

`seed-demo.sh` charge les données de Menewa Naya : les comptes `+228 90 00 00 01` à `08`, les 11 dons du catalogue, 3 prestataires, 2 campagnes, la famille Mensah et une demande d'exemple. Il est idempotent.

## Mettre à jour

On ne modifie jamais `/opt/menewa` en place. La nouvelle version est préparée à côté, puis on bascule. L'ancienne version reste dans `/opt/menewa.prev` pour un retour arrière.

```sh
# 1. Sur le poste : commiter, puis archiver HEAD (seul le code commité part en production).
#    core.autocrlf=false : sous Windows, sans cette option, l'archive convertit les fins de ligne en CRLF.
git -c core.autocrlf=false archive --format=tar.gz -o ../menewa.tar.gz HEAD -- . ':(exclude)sample'
scp ../menewa.tar.gz ubuntu@<serveur>:/tmp/ && rm ../menewa.tar.gz

# 2. Sur le serveur : sauvegarde avant toute modification
cd /opt/menewa/deploy/dev && sudo ./backup.sh

# 3. Préparer la nouvelle version et y recopier les secrets
sudo rm -rf /opt/menewa.new && sudo install -d -o "$USER" -g "$USER" /opt/menewa.new
tar -xzf /tmp/menewa.tar.gz -C /opt/menewa.new && rm /tmp/menewa.tar.gz
cp -p /opt/menewa/deploy/dev/.env /opt/menewa/deploy/dev/.access-password /opt/menewa.new/deploy/dev/

# 4. Bascule (l'ancienne version est gardée dans /opt/menewa.prev), puis redéploiement
sudo rm -rf /opt/menewa.prev && sudo mv /opt/menewa /opt/menewa.prev && sudo mv /opt/menewa.new /opt/menewa
cd /opt/menewa/deploy/dev && ./deploy.sh
```

- La base et les fichiers déposés sont conservés : ils vivent dans les volumes Docker. Les migrations Flyway s'appliquent au démarrage du backend.
- Les blocs Caddy ne sont ajoutés qu'une seule fois. La tâche cron pointe vers `/opt/menewa/deploy/dev/backup.sh`, un chemin qui ne change pas à la bascule.
- **Ne redéployer qu'un frontend** (sans redémarrer l'API) : `docker compose --env-file .env up -d --build --no-deps saas` (ou `admin`). Sans `--no-deps`, le backend redémarre aussi.
- **Machine à 8 Go de RAM côté poste :** compilez et testez le backend avec une JDK 21 (Gradle 8.14 ne fonctionne pas avec une JDK 25), puis lancez `./gradlew --stop`.

## Revenir à la version précédente

```sh
sudo mv /opt/menewa /opt/menewa.rate && sudo mv /opt/menewa.prev /opt/menewa
cd /opt/menewa/deploy/dev && ./deploy.sh
```

Si la version abandonnée a appliqué une migration de base, l'ancien code peut ne plus correspondre au schéma. Dans ce cas, restaurez aussi la sauvegarde prise à l'étape 2 de la mise à jour :

```sh
sudo ./restore.sh /var/backups/menewa/menewa-db-<date>.dump --production
```

Supprimez ensuite `/opt/menewa.rate` une fois le problème compris.

## Vérifier après un déploiement

```sh
# Sur le serveur
docker compose --env-file .env ps                      # 4 conteneurs « Up », menewa-db « healthy »
docker compose --env-file .env logs --since 10m backend | grep -iE "error|exception"
curl -s -o /dev/null -w '%{http_code}\n' https://menewa.<ip-avec-tirets>.sslip.io   # 401 attendu (mot de passe d'accès)
```

Tests de bout en bout (Playwright), depuis le poste :

```sh
cd e2e
npx playwright install chromium   # une seule fois
DEMO_PASSWORD=<contenu de .access-password> npx playwright test
```

`SAAS_URL`, `ADMIN_URL` et `ADMIN_PHONE` permettent de viser une autre zone. Par défaut, les tests visent la zone de dev actuelle. Ils créent des comptes en `+22899…`, un préfixe réservé aux tests.

## Application Android de test

```sh
cd /opt/menewa/deploy/dev && sudo ./build-apk.sh
```

- Le script compile `apps/mobile` (inclus dans l'archive) avec l'image Flutter de la CI. L'APK appelle l'API de la zone de dev. Il est publié sur `https://<SAAS_HOST>/telechargement/menewa-test.apk`, derrière le mot de passe d'accès.
- Les dépendances Dart et Gradle et les composants Android SDK sont gardés dans des volumes Docker (`menewa_flutter_cache`, `menewa_gradle_cache`, `menewa_android_sdk`). Seul le premier build les télécharge.
- La clé de signature de test est créée au premier lancement dans `/opt/menewa-mobile-keys`, lisible par root seulement, en dehors de `/opt/menewa` : elle survit aux mises à jour. **Conservez-la** : elle permet d'installer chaque version par-dessus la précédente. Si elle est perdue, il faut désinstaller l'app.
- La clé `MOBILE_API_KEY` est intégrée à l'APK. Elle ne protège que la zone de test ; ce n'est pas un secret fort.
- Installation : ouvrir le lien sur le téléphone, saisir le mot de passe d'accès, puis autoriser l'installation depuis le navigateur (« sources inconnues »).

## Sauvegardes et restauration

- `backup.sh` sauvegarde la base (`pg_dump` au format personnalisé, contrôlé par `pg_restore --list`) et les fichiers déposés (volume `menewa_storage`). Les sauvegardes vont dans `/var/backups/menewa`, lisible par root seulement, et sont gardées 14 jours.
- La sauvegarde tourne chaque nuit à 02:30 (heure du serveur). Le journal est dans `/var/log/menewa-backup.log`.
- **Copie hors serveur : pas encore configurée.** Définir `BACKUP_RSYNC_TARGET=utilisateur@machine:/dossier` dans `.env`, et autoriser une clé SSH de root sur la machine cible. Sans cette copie, les sauvegardes disparaîtraient avec le serveur. Le journal le rappelle chaque nuit.
- Les anciennes sauvegardes manuelles (`*.sql.gz`, format SQL simple) sont dans `~/menewa-backups`.

| Action | Commande |
|---|---|
| Sauvegarder maintenant | `sudo ./backup.sh` |
| Vérifier qu'une sauvegarde se restaure (base temporaire, service intact) | `sudo ./restore.sh /var/backups/menewa/menewa-db-<date>.dump` |
| Remplacer la base en service (demande `RESTAURER`, sauvegarde d'abord l'état actuel) | `sudo ./restore.sh <fichier> --production` |

## Exploitation courante

Depuis `/opt/menewa/deploy/dev` :

```sh
docker compose --env-file .env logs -f backend     # journaux de l'API (ou saas, admin, postgres)
docker compose --env-file .env restart backend     # redémarrer un service
docker compose --env-file .env down                # tout arrêter (données conservées)
docker compose --env-file .env down -v             # tout arrêter ET effacer les données (irréversible)
cat .access-password                                # mot de passe d'accès
```

## Configuration (`.env`)

Généré par `deploy.sh` (droits `600`). Il n'est pas versionné et il est recopié à chaque mise à jour.

| Variable | Rôle |
|---|---|
| `SAAS_HOST`, `ADMIN_HOST`, `API_HOST` | noms d'hôte servis par Caddy |
| `POSTGRES_PASSWORD` | mot de passe de la base |
| `OTP_SECRET` | secret de signature des codes et des sessions |
| `ADMIN_PHONE` | téléphone du premier administrateur (`+22890000001`) |
| `MOBILE_API_KEY` | clé attendue dans l'en-tête `X-Menewa-Key` |
| `BACKUP_RSYNC_TARGET`, `BACKUP_DIR`, `BACKUP_KEEP_DAYS` | facultatives, voir [Sauvegardes](#sauvegardes-et-restauration) |

Le Caddyfile contient un hash du mot de passe d'accès. Modifier `.access-password` ne suffit donc pas : il faut aussi remplacer le hash dans `/srv/proxy/Caddyfile` (`docker exec caddy caddy hash-password`), puis valider et recharger Caddy.

## Protections de l'API

- **Demandes de code :** au plus 3 par numéro en 10 minutes, et 30 par adresse IP et par heure (`OTP_MAX_REQUESTS_PER_IP`).
- **Codes erronés :** au plus 5 essais par code, et 30 échecs par adresse IP et par heure (`OTP_MAX_FAILURES_PER_IP`).
- Les limites par IP sont larges, car beaucoup d'abonnés mobiles partagent la même adresse. Les compteurs sont en mémoire : un redémarrage les remet à zéro.
- Caddy remplace l'en-tête `X-Forwarded-For` envoyé par le client, puis les apps web relaient l'IP réelle à l'API.
- **Ménage horaire :** suppression des sessions expirées, des codes de plus de 24 h et des compteurs d'IP périmés.

## Dépannage

| Symptôme | Piste |
|---|---|
| 401 sur un site | normal sans le mot de passe d'accès (`cat .access-password`) |
| 401 « Clé d'application requise » sur l'API | en-tête `X-Menewa-Key` absent ou différent de `MOBILE_API_KEY` |
| 404 sur `api-menewa…/actuator/...` | normal : seul `/api/*` est relayé |
| 502 sur un site | le conteneur ne tourne pas ou démarre encore : `docker compose --env-file .env ps` et ses journaux |
| Le backend redémarre en boucle | migration Flyway en échec ou base injoignable : `logs backend` |
| `deploy.sh` s'arrête sur « Caddyfile invalide » | le proxy n'a pas été modifié ; corriger le bloc puis relancer |
| `deploy.sh` s'arrête sur « Build failed because of webpack errors » | souvent passager (téléchargement raté pendant `next build`) : relancer `./deploy.sh`. Les conteneurs en service ne sont remplacés qu'une fois tous les builds réussis |
| « Permission denied » sur un script | bit exécutable perdu : `chmod +x *.sh`, et côté dépôt `git update-index --chmod=+x <script>` |
| Build interrompu, mémoire insuffisante | un seul build à la fois (ne pas lancer `build-apk.sh` pendant `deploy.sh`) |

## Retirer Menewa du serveur

1. Supprimez les deux blocs Menewa dans `/srv/proxy/Caddyfile`, puis lancez `docker exec caddy caddy validate --config /etc/caddy/Caddyfile` et `docker exec caddy caddy reload --config /etc/caddy/Caddyfile`.
2. Arrêtez les conteneurs : `docker compose --env-file .env down` (avec `-v` pour effacer aussi les données).
3. Supprimez `/etc/cron.d/menewa-backup` et mettez à jour `/srv/README.md`.
