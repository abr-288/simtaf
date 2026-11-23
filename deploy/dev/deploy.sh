#!/usr/bin/env sh
# Déploie (ou met à jour) la zone de dev Menewa sur le VPS mutualisé (Caddy partagé dans /srv/proxy).
# Première exécution : génère .env (secrets aléatoires).
set -eu
cd "$(dirname "$0")"
PROXY_DIR=${PROXY_DIR:-/srv/proxy}

if [ ! -f .env ]; then
  dashed=$(curl -fsS https://api.ipify.org | tr . -)
  rnd() { head -c 64 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c "$1"; }
  cat > .env <<ENV
SAAS_HOST=menewa.$dashed.sslip.io
ADMIN_HOST=menewa-admin.$dashed.sslip.io
POSTGRES_PASSWORD=$(rnd 32)
OTP_SECRET=$(rnd 48)
ADMIN_PHONE=+22890000001
ENV
  chmod 600 .env
fi
. ./.env
if [ -z "${API_HOST:-}" ]; then
  dashed=$(echo "$SAAS_HOST" | sed -e 's/^menewa\.//' -e 's/\.sslip\.io$//')
  key=$(head -c 64 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 40)
  { echo "API_HOST=api-menewa.$dashed.sslip.io"; echo "MOBILE_API_KEY=$key"; } >> .env
  . ./.env
fi

docker compose --env-file .env up -d --build

# Ajout (une seule fois) du bloc Menewa au Caddyfile partagé, après sauvegarde et validation.
if ! sudo grep -q "$SAAS_HOST" "$PROXY_DIR/Caddyfile"; then
  backup="$PROXY_DIR/Caddyfile.avant-menewa-$(date +%Y%m%d-%H%M%S)"
  sudo cp "$PROXY_DIR/Caddyfile" "$backup"
  sed -e "s|__SAAS_HOST__|$SAAS_HOST|" -e "s|__ADMIN_HOST__|$ADMIN_HOST|" Caddyfile.snippet > /tmp/menewa.caddy
  { echo; cat /tmp/menewa.caddy; } | sudo tee -a "$PROXY_DIR/Caddyfile" > /dev/null
  rm -f /tmp/menewa.caddy
  if ! sudo docker exec caddy caddy validate --config /etc/caddy/Caddyfile --adapter caddyfile; then
    sudo cp "$backup" "$PROXY_DIR/Caddyfile"
    echo "Caddyfile invalide : sauvegarde restaurée, proxy inchangé." >&2
    exit 1
  fi
  sudo docker exec caddy caddy reload --config /etc/caddy/Caddyfile --adapter caddyfile
fi

# Bloc de l'API mobile (une seule fois), même procédure : sauvegarde, validation, restauration si invalide.
if ! sudo grep -q "$API_HOST" "$PROXY_DIR/Caddyfile"; then
  backup="$PROXY_DIR/Caddyfile.avant-menewa-api-$(date +%Y%m%d-%H%M%S)"
  sudo cp "$PROXY_DIR/Caddyfile" "$backup"
  sed -e "s|__API_HOST__|$API_HOST|" -e "s|__API_KEY__|$MOBILE_API_KEY|" Caddyfile.api.snippet > /tmp/menewa-api.caddy
  { echo; cat /tmp/menewa-api.caddy; } | sudo tee -a "$PROXY_DIR/Caddyfile" > /dev/null
  rm -f /tmp/menewa-api.caddy
  if ! sudo docker exec caddy caddy validate --config /etc/caddy/Caddyfile --adapter caddyfile; then
    sudo cp "$backup" "$PROXY_DIR/Caddyfile"
    echo "Caddyfile invalide : sauvegarde restaurée, proxy inchangé." >&2
    exit 1
  fi
  sudo docker exec caddy caddy reload --config /etc/caddy/Caddyfile --adapter caddyfile
fi

# Sauvegarde quotidienne à 02:30 (heure du serveur), installée une fois ; voir backup.sh.
cron_line="30 2 * * * root $(pwd)/backup.sh >> /var/log/menewa-backup.log 2>&1"
if [ "$(sudo cat /etc/cron.d/menewa-backup 2>/dev/null)" != "$cron_line" ]; then
  echo "$cron_line" | sudo tee /etc/cron.d/menewa-backup > /dev/null
  sudo chmod 644 /etc/cron.d/menewa-backup
  echo "Sauvegarde quotidienne planifiée (/etc/cron.d/menewa-backup)."
fi
echo
echo "Espace membre  : https://$SAAS_HOST"
echo "Administration : https://$ADMIN_HOST  (téléphone $ADMIN_PHONE, code affiché à l'écran)"
echo "API mobile     : https://$API_HOST/api  (en-tête X-Menewa-Key, clé MOBILE_API_KEY dans .env)"
