import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'api/api_client.dart';
import 'models.dart';

final apiProvider = Provider<ApiClient>((ref) => ApiClient());

final sessionProvider = NotifierProvider<SessionNotifier, SessionState>(SessionNotifier.new);

class SessionState {
  const SessionState({this.loading = false, this.user});

  final bool loading;
  final AppUser? user;

  bool get authenticated => user != null;
}

/// Session mobile : jeton d'accès conservé dans le trousseau sécurisé (Keychain / Keystore).
class SessionNotifier extends Notifier<SessionState> {
  static const _storage = FlutterSecureStorage();
  static const _tokenKey = 'menewa_access_token';

  ApiClient get _api => ref.read(apiProvider);

  @override
  SessionState build() {
    Future.microtask(restore);
    return const SessionState(loading: true);
  }

  Future<void> restore() async {
    final token = await _storage.read(key: _tokenKey);
    if (token == null) {
      state = const SessionState();
      return;
    }
    _api.token = token;
    try {
      final me = await _api.get('/auth/me');
      state = SessionState(user: AppUser.fromJson(me as Map<String, dynamic>));
    } on ApiException catch (e) {
      if (e.status == 401 || e.status == 403) await _clear();
      state = const SessionState();
    }
  }

  /// Retourne le code de démonstration lorsque l'API est en mode développement.
  Future<String?> requestOtp({required String phone, required bool register, String? name, String? role}) async {
    final res = await _api.post('/auth/otp/request', {
      'phone': phone,
      'mode': register ? 'register' : 'login',
      if (register) 'name': name,
      if (register) 'role': role,
    }) as Map<String, dynamic>;
    return res['devCode'] as String?;
  }

  Future<void> verifyOtp(String phone, String code) async {
    final res = await _api.post('/auth/otp/verify', {'phone': phone, 'code': code}) as Map<String, dynamic>;
    final token = res['accessToken'] as String;
    _api.token = token;
    await _storage.write(key: _tokenKey, value: token);
    state = SessionState(user: AppUser.fromJson(res['user'] as Map<String, dynamic>));
  }

  Future<void> refreshUser() async {
    final me = await _api.get('/auth/me');
    state = SessionState(user: AppUser.fromJson(me as Map<String, dynamic>));
  }

  Future<void> logout() async {
    try {
      await _api.post('/auth/logout');
    } catch (_) {
      // Session déjà révoquée côté serveur : on nettoie localement.
    }
    await _clear();
    state = const SessionState();
  }

  Future<void> _clear() async {
    _api.token = null;
    await _storage.delete(key: _tokenKey);
  }
}
