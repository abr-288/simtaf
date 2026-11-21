#!/usr/bin/env sh
# Charge les données de démonstration de Menewa Naya (comptes, catalogue, prestataires, campagnes, demande d'exemple).
# Idempotent. À lancer après ./deploy.sh, une fois l'API démarrée.
set -eu
cd "$(dirname "$0")"
until sudo docker logs menewa-backend 2>&1 | grep -q "Started MenewaApplication"; do sleep 2; done
sudo docker exec -i menewa-db psql -q -U menewa -d menewa -v ON_ERROR_STOP=1 < ../../backend/scripts/seed-demo.sql
sudo docker run --rm --network menewa_internal -v "$PWD/seed-demo.mjs:/seed-demo.mjs:ro" -e API=http://backend:8080/api node:22-alpine node /seed-demo.mjs
