class AppConfig {
  const AppConfig._();

  /// URL de l'API : `--dart-define=API_BASE_URL=https://api-menewa.169-58-93-234.sslip.io/api` pour la zone de dev.
  static const apiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080/api',
  );

  /// Clé d'application exigée par le proxy de la zone de dev (en-tête X-Menewa-Key) : `--dart-define=API_KEY=...`.
  /// Vide en local, où l'API est appelée directement.
  static const apiKey = String.fromEnvironment('API_KEY');
}
