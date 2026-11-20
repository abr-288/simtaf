// Modèles utilisés par les écrans, construits sur les modèles typés générés depuis le contrat (`api.g.dart`).
// Un champ renommé ou supprimé côté API fait échouer `flutter analyze` ici, pas à l'exécution.
import 'api.g.dart';

typedef Json = Map<String, dynamic>;

int asInt(dynamic v) => v is num ? v.toInt() : int.tryParse('$v') ?? 0;
String? asStr(dynamic v) => v?.toString();
List<Json> asList(dynamic v) => v is List ? v.whereType<Map>().map((e) => Json.from(e)).toList() : <Json>[];

class AppUser {
  AppUser({required this.id, required this.phone, required this.name, required this.role, this.email, this.zone, this.organization,
      this.bio, this.notifInapp = true, this.notifSms = true, this.notifEmail = false});

  factory AppUser.fromJson(Json j) {
    final u = ApiIdentityUserResponse.fromJson(j);
    final prefs = u.notifPrefs;
    return AppUser(
      id: u.id ?? 0,
      phone: u.phone ?? '',
      name: u.name ?? '',
      role: u.role ?? 'donor',
      email: u.email,
      zone: u.zone,
      organization: u.organization,
      bio: u.bio,
      notifInapp: prefs?.inapp ?? true,
      notifSms: prefs?.sms ?? true,
      notifEmail: prefs?.email ?? false,
    );
  }

  final int id;
  final String phone;
  final String name;
  final String role;
  final String? email;
  final String? zone;
  final String? organization;
  final String? bio;
  final bool notifInapp;
  final bool notifSms;
  final bool notifEmail;

  String get displayName => name.isEmpty ? phone : name;
  bool get isRequester => role == 'donor' || role == 'client';
  bool get isAdmin => role == 'admin' || role == 'subadmin';
}

class CatalogItem {
  CatalogItem(this.id, this.title, this.category, this.description, this.unitPrice, this.unit, this.emoji);

  factory CatalogItem.fromJson(Json j) {
    final i = ApiCatalogItemResponse.fromJson(j);
    return CatalogItem(i.id ?? 0, i.title ?? '', i.category ?? 'autre', i.description, i.unitPrice, i.unit ?? 'unité', i.emoji ?? '🎁');
  }

  final int id;
  final String title;
  final String category;
  final String? description;
  final int unitPrice;
  final String unit;
  final String emoji;
}

class GiftRequest {
  GiftRequest(Json json) : row = ApiRequestRow.fromJson(json);

  final ApiRequestRow row;
  int get id => row.id ?? 0;
  String get ref => row.ref ?? '';
  String get title => row.title ?? '';
  String get status => row.status ?? '';
  String get kind => row.kind ?? '';
  int get totalAmount => row.totalAmount;
  String? get createdAt => row.createdAt;
}

class Mission {
  Mission(Json json) : row = ApiMissionRow.fromJson(json);

  final ApiMissionRow row;
  int get id => row.id ?? 0;
  String get title => row.title ?? '';
  String get status => row.status ?? '';
  String? get dueDate => row.dueDate;
  int get providerFee => row.providerFee;
  bool get exchange => row.compensationMode == 'exchange';
  bool get late => row.late;
  bool get validated => row.validatedAt != null;
  String? get instructions => row.instructions;
  String? get refusalReason => row.refusalReason;
}

class AppNotification {
  AppNotification(Json json) : row = ApiNotificationView.fromJson(json);

  final ApiNotificationView row;
  int get id => row.id ?? 0;
  String get title => row.title ?? '';
  String? get body => row.body;
  String? get link => row.link;
  bool get read => row.readAt != null;
  String? get createdAt => row.createdAt;
}
