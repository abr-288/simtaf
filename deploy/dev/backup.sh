#!/usr/bin/env sh
# Sauvegarde quotidienne de la zone Menewa : base PostgreSQL (format pg_dump personnalisé) et fichiers déposés (preuves, justificatifs).
# Lancée par cron (/etc/cron.d/menewa-backup, installé par deploy.sh) en root ; manuellement : sudo ./backup.sh
#   BACKUP_DIR          dossier local des sauvegardes            (défaut /var/backups/menewa)
#   BACKUP_KEEP_DAYS    durée de conservation locale, en jours    (défaut 14)
#   BACKUP_RSYNC_TARGET copie hors serveur, ex. backup@autre-serveur:/srv/menewa (vide : pas de copie distante)
# Réglages lus dans .env s'ils y sont définis.
set -eu
cd "$(dirname "$0")"
[ -f .env ] && . ./.env
BACKUP_DIR=${BACKUP_DIR:-/var/backups/menewa}
BACKUP_KEEP_DAYS=${BACKUP_KEEP_DAYS:-14}
BACKUP_RSYNC_TARGET=${BACKUP_RSYNC_TARGET:-}
stamp=$(date +%Y%m%d-%H%M%S)
log() { echo "$(date '+%F %T') $*"; }

mkdir -p "$BACKUP_DIR" && chmod 700 "$BACKUP_DIR"   # données personnelles : lecture root seulement
umask 077

# 1. Base : dump cohérent pris à chaud, puis contrôle de lisibilité (liste des objets).
db="$BACKUP_DIR/menewa-db-$stamp.dump"
docker compose exec -T postgres pg_dump -U menewa -d menewa -Fc > "$db.part"
docker compose exec -T postgres pg_restore --list < "$db.part" > /dev/null
mv "$db.part" "$db"
log "base sauvegardée : $db ($(du -h "$db" | cut -f1))"

# 2. Fichiers déposés (volume de stockage du backend), lus sans arrêter le service.
files="$BACKUP_DIR/menewa-files-$stamp.tar.gz"
docker run --rm -v menewa_storage:/data:ro -v "$BACKUP_DIR":/backup alpine:3.20 \
  tar -czf "/backup/$(basename "$files").part" -C /data .
mv "$files.part" "$files" && chmod 600 "$files"   # créé dans le conteneur : droits resserrés
log "fichiers sauvegardés : $files ($(du -h "$files" | cut -f1))"

# 3. Rotation locale.
find "$BACKUP_DIR" -maxdepth 1 -name 'menewa-*' -type f -mtime +"$BACKUP_KEEP_DAYS" -print -delete | sed 's/^/supprimé (ancien) : /'

# 4. Copie hors serveur (une sauvegarde restée sur le serveur disparaît avec lui).
if [ -n "$BACKUP_RSYNC_TARGET" ]; then
  rsync -a --delete -e "ssh -o BatchMode=yes" "$BACKUP_DIR"/ "$BACKUP_RSYNC_TARGET"/
  log "copie distante faite vers $BACKUP_RSYNC_TARGET"
else
  log "ATTENTION : aucune copie hors serveur (BACKUP_RSYNC_TARGET non défini)"
fi
