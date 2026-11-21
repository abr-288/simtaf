#!/usr/bin/env sh
# Compile l'APK Android de test, branchée sur l'API de la zone de dev, et la publie sur
#   https://$SAAS_HOST/telechargement/menewa-test.apk   (derrière le mot de passe d'accès)
# Usage (sur le serveur, après avoir copié apps/mobile dans /opt/menewa/apps/mobile) : sudo ./build-apk.sh
# La clé de signature de test est créée au premier lancement dans /opt/menewa-mobile-keys (root seulement) :
# la conserver permet d'installer les versions suivantes par-dessus, sans désinstaller.
set -eu
cd "$(dirname "$0")"
. ./.env
SRC=$(cd ../../apps/mobile && pwd)
KEYS=/opt/menewa-mobile-keys
IMAGE=ghcr.io/cirruslabs/flutter:3.44.0   # même version que la CI
OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT

# 1. Clé de signature de test (une seule fois).
if [ ! -f "$KEYS/key.properties" ]; then
  mkdir -p "$KEYS" && chmod 700 "$KEYS"
  pass=$(head -c 48 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 32)
  docker run --rm -v "$KEYS":/keys "$IMAGE" keytool -genkeypair -v -keystore /keys/menewa-test.jks -storepass "$pass" -keypass "$pass" \
    -alias menewa-test -keyalg RSA -keysize 2048 -validity 3650 -dname "CN=Menewa Naya (test), O=Menewa, C=TG" >/dev/null 2>&1
  printf 'storeFile=/keys/menewa-test.jks\nstorePassword=%s\nkeyAlias=menewa-test\nkeyPassword=%s\n' "$pass" "$pass" > "$KEYS/key.properties"
  chmod 600 "$KEYS"/*
  echo "Clé de signature de test créée dans $KEYS (à conserver)."
fi

# 2. Compilation (copie de travail : les fichiers produits par le conteneur n'encombrent pas les sources).
#    Caches persistants : dépendances Dart et Gradle, composants Android SDK installés au premier build.
work="$OUT/src"; cp -a "$SRC" "$work"; cp "$KEYS/key.properties" "$work/android/key.properties"
build=$(( ($(date +%s) - 1767225600) / 60 ))   # numéro de version croissant (minutes depuis 2026) : mise à jour possible
docker run --rm --cpus=2 -v "$work":/app -v "$KEYS":/keys:ro -v menewa_flutter_cache:/root/.pub-cache -v menewa_gradle_cache:/root/.gradle \
  -v menewa_android_sdk:/opt/android-sdk-linux -w /app "$IMAGE" sh -c "flutter pub get >/dev/null && flutter build apk --release --build-number=$build \
    --dart-define=API_BASE_URL=https://$API_HOST/api --dart-define=API_KEY=$MOBILE_API_KEY"
apk="$work/build/app/outputs/flutter-apk/app-release.apk"
[ -f "$apk" ] || { echo "APK introuvable après la compilation." >&2; exit 1; }

# 3. Publication dans le volume lu par le site (menewa-saas).
cp "$apk" "$OUT/menewa-test.apk"
docker run --rm -v menewa_downloads:/d -v "$OUT":/o:ro alpine:3.20 sh -c 'cp /o/menewa-test.apk /d/menewa-test.apk.part && mv /d/menewa-test.apk.part /d/menewa-test.apk'
echo "APK publiée ($(du -h "$apk" | cut -f1), version $build) : https://$SAAS_HOST/telechargement/menewa-test.apk"
