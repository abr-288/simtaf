import 'dart:convert';

import 'package:http/http.dart' as http;

import '../config/app_config.dart';

/// Erreur renvoyée par l'API Menewa, avec un message affichable à l'utilisateur.
class ApiException implements Exception {
  ApiException(this.message, this.status);

  final String message;
  final int status;

  @override
  String toString() => message;
}

const _errorMessages = <String, String>{
  'account_exists': 'Ce numéro possède déjà un compte. Connectez-vous.',
  'account_not_found': "Aucun compte pour ce numéro. Inscrivez-vous d'abord.",
  'account_unavailable': "Ce compte est suspendu ou archivé. Contactez l'administration.",
  'name_required': 'Veuillez indiquer votre nom.',
  'otp_rate_limited': 'Trop de demandes de code. Réessayez dans quelques minutes.',
  'otp_ip_rate_limited': 'Trop de tentatives depuis votre connexion. Réessayez dans une heure.',
  'sms_unavailable': "Le SMS n'a pas pu être envoyé. Vérifiez le numéro ou réessayez dans quelques minutes.",
  'otp_expired_or_missing': 'Code expiré ou introuvable. Demandez un nouveau code.',
  'otp_attempts_exceeded': 'Trop de tentatives. Demandez un nouveau code.',
  'otp_invalid': 'Code incorrect.',
  'invalid_phone': 'Numéro invalide. Exemple : +228 90 00 00 00',
  'unauthenticated': 'Session expirée. Reconnectez-vous.',
  'session_expired': 'Session expirée. Reconnectez-vous.',
};

/// Client HTTP du backend Menewa : jeton Bearer, JSON, multipart (preuves, reçus).
/// Les permissions ne sont pas réimplémentées ici : l'application respecte les refus de l'API.
class ApiClient {
  ApiClient({http.Client? client, String? baseUrl})
      : _http = client ?? http.Client(),
        baseUrl = baseUrl ?? AppConfig.apiBaseUrl;

  final http.Client _http;
  final String baseUrl;
  String? token;

  static const _timeout = Duration(seconds: 25);

  /// En-têtes communs : clé d'application (zone de dev) et jeton de session.
  Map<String, String> get authHeaders => {
        if (AppConfig.apiKey.isNotEmpty) 'X-Menewa-Key': AppConfig.apiKey,
        if (token != null) 'Authorization': 'Bearer $token',
      };

  /// URL d'un fichier protégé (preuve, reçu) ; à charger avec [authHeaders].
  String fileUrl(String kind, int id) => '$baseUrl/v1/files/$kind/$id';

  Uri _uri(String path, [Map<String, String?>? query]) {
    final params = <String, String>{};
    query?.forEach((key, value) {
      if (value != null && value.isNotEmpty) params[key] = value;
    });
    final uri = Uri.parse('$baseUrl$path');
    return params.isEmpty ? uri : uri.replace(queryParameters: params);
  }

  Future<dynamic> get(String path, {Map<String, String?>? query}) =>
      _send(() => _http.get(_uri(path, query), headers: authHeaders));

  Future<dynamic> post(String path, [Object? body]) => _send(() => _http.post(_uri(path),
      headers: {...authHeaders, 'Content-Type': 'application/json'}, body: jsonEncode(body ?? <String, dynamic>{})));

  Future<dynamic> put(String path, [Object? body]) => _send(() => _http.put(_uri(path),
      headers: {...authHeaders, 'Content-Type': 'application/json'}, body: jsonEncode(body ?? <String, dynamic>{})));

  Future<dynamic> delete(String path) => _send(() => _http.delete(_uri(path), headers: authHeaders));

  Future<dynamic> multipart(String path, Map<String, String> fields, {String? fileField, String? filePath}) async {
    final request = http.MultipartRequest('POST', _uri(path))
      ..headers.addAll(authHeaders)
      ..fields.addAll(fields);
    if (fileField != null && filePath != null) {
      request.files.add(await http.MultipartFile.fromPath(fileField, filePath));
    }
    return _send(() async => http.Response.fromStream(await _http.send(request)));
  }

  Future<dynamic> _send(Future<http.Response> Function() call) async {
    http.Response response;
    try {
      response = await call().timeout(_timeout);
    } catch (_) {
      throw ApiException('Impossible de joindre le serveur. Vérifiez votre connexion.', 0);
    }
    final text = utf8.decode(response.bodyBytes);
    dynamic body;
    try {
      body = text.isEmpty ? null : jsonDecode(text);
    } catch (_) {
      body = null;
    }
    if (response.statusCode >= 200 && response.statusCode < 300) return body;
    throw ApiException(_describe(response.statusCode, body), response.statusCode);
  }

  static String _describe(int status, dynamic body) {
    final detail = body is Map ? (body['detail'] ?? body['message'] ?? '').toString() : '';
    if (_errorMessages.containsKey(detail)) return _errorMessages[detail]!;
    final generic = RegExp(r'^(Bad Request|Forbidden|Unauthorized|Not Found|Invalid request content\.)$');
    if (detail.isNotEmpty && !generic.hasMatch(detail)) return detail;
    switch (status) {
      case 401:
        return 'Session expirée. Reconnectez-vous.';
      case 403:
        return 'Action non autorisée.';
      case 404:
        return 'Élément introuvable.';
      case 413:
        return 'Fichier trop volumineux (8 Mo maximum).';
    }
    return status >= 500 ? 'Le service est momentanément indisponible.' : 'Requête invalide. Vérifiez les valeurs saisies.';
  }
}
