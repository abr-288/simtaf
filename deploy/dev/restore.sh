#!/usr/bin/env sh
# Restaure une sauvegarde de la base. Par défaut dans une base de contrôle « menewa_restore_test » (sans risque) ;
# avec --production, remplace la base en service après arrêt du backend (demande une confirmation).
#   sudo ./restore.sh /var/backups/menewa/menewa-db-AAAAMMJJ-HHMMSS.dump [--production]
set -eu
cd "$(dirname "$0")"
dump=${1:?indiquer le fichier .dump à restaurer}
mode=${2:-test}
[ -f "$dump" ] || { echo "Fichier introuvable : $dump" >&2; exit 1; }
psql() { docker compose exec -T postgres psql -U menewa -d postgres -v ON_ERROR_STOP=1 "$@"; }

if [ "$mode" = "--production" ]; then
  printf 'Remplacer la base « menewa » en service par %s ? Tapez RESTAURER : ' "$dump"; read -r answer
  [ "$answer" = "RESTAURER" ] || { echo "Annulé."; exit 1; }
  ./backup.sh   # filet de sécurité : sauvegarde de l'état actuel avant écrasement
  docker compose stop backend
  psql -c "DROP DATABASE IF EXISTS menewa WITH (FORCE)" -c "CREATE DATABASE menewa OWNER menewa"
  docker compose exec -T postgres pg_restore -U menewa -d menewa --no-owner < "$dump"
  docker compose start backend
  echo "Base restaurée ; backend relancé."
else
  psql -c "DROP DATABASE IF EXISTS menewa_restore_test" -c "CREATE DATABASE menewa_restore_test OWNER menewa"
  docker compose exec -T postgres pg_restore -U menewa -d menewa_restore_test --no-owner < "$dump"
  docker compose exec -T postgres psql -U menewa -d menewa_restore_test -tA -c \
    "SELECT 'migrations: ' || max(version::int) FROM flyway_schema_history WHERE success" -c \
    "SELECT 'comptes: ' || count(*) FROM identity_users" -c "SELECT 'demandes: ' || count(*) FROM giving_requests"
  psql -c "DROP DATABASE menewa_restore_test"
  echo "Restauration de contrôle réussie (base temporaire supprimée)."
fi
