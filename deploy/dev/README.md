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

Pour retirer Menewa du proxy, supprimez ses deux blocs dans `/srv/proxy/Caddyfile`, puis lancez `docker exec caddy caddy reload --config /etc/caddy/Caddyfile`.
