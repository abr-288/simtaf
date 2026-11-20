/// Modèles des réponses de l'API (champs utilisés par l'application mobile).
typedef Json = Map<String, dynamic>;

int asInt(dynamic v) => v is num ? v.toInt() : int.tryParse('$v') ?? 0;
String? asStr(dynamic v) => v?.toString();
List<Json> asList(dynamic v) => v is List ? v.whereType<Map>().map((e) => Json.from(e)).toList() : <Json>[];

class AppUser {
  AppUser({required this.id, required this.phone, required this.name, required this.role, this.email, this.zone, this.organization,
      this.bio, this.notifInapp = true, this.notifSms = true, this.notifEmail = false});

  factory AppUser.fromJson(Json j) {
    final prefs = j['notifPrefs'] is Map ? Json.from(j['notifPrefs'] as Map) : const <String, dynamic>{};
    return AppUser(
      id: asInt(j['id']),
      phone: asStr(j['phone']) ?? '',
      name: asStr(j['name']) ?? '',
      role: asStr(j['role']) ?? 'donor',
      email: asStr(j['email']),
      zone: asStr(j['zone']),
      organization: asStr(j['organization']),
      bio: asStr(j['bio']),
      notifInapp: prefs['inapp'] != false,
      notifSms: prefs['sms'] != false,
      notifEmail: prefs['email'] == true,
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

  factory CatalogItem.fromJson(Json j) => CatalogItem(asInt(j['id']), asStr(j['title']) ?? '', asStr(j['category']) ?? 'autre',
      asStr(j['description']), asInt(j['unitPrice']), asStr(j['unit']) ?? 'unité', asStr(j['emoji']) ?? '🎁');

  final int id;
  final String title;
  final String category;
  final String? description;
  final int unitPrice;
  final String unit;
  final String emoji;
}

class GiftRequest {
  GiftRequest(this.json);

  final Json json;
  int get id => asInt(json['id']);
  String get ref => asStr(json['ref']) ?? '';
  String get title => asStr(json['title']) ?? '';
  String get status => asStr(json['status']) ?? '';
  String get kind => asStr(json['kind']) ?? '';
  int get totalAmount => asInt(json['totalAmount']);
  String? get createdAt => asStr(json['createdAt']);
}

class Mission {
  Mission(this.json);

  final Json json;
  int get id => asInt(json['id']);
  String get title => asStr(json['title']) ?? '';
  String get status => asStr(json['status']) ?? '';
  String? get dueDate => asStr(json['dueDate']);
  int get providerFee => asInt(json['providerFee']);
  bool get exchange => json['compensationMode'] == 'exchange';
  bool get late => json['late'] == true;
  bool get validated => json['validatedAt'] != null;
}

class AppNotification {
  AppNotification(this.json);

  final Json json;
  int get id => asInt(json['id']);
  String get title => asStr(json['title']) ?? '';
  String? get body => asStr(json['body']);
  String? get link => asStr(json['link']);
  bool get read => json['readAt'] != null;
  String? get createdAt => asStr(json['createdAt']);
}
