# Exemple : URL API Flutter

```bash
# Android Emulator
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080/api

# iOS Simulator
flutter run --dart-define=API_BASE_URL=http://localhost:8080/api

# Appareil physique : remplacez par l’adresse LAN de votre ordinateur
flutter run --dart-define=API_BASE_URL=http://192.168.1.10:8080/api
```
