import 'package:flutter_test/flutter_test.dart';
import 'package:menewa_mobile/features/update/app_update.dart';

void main() {
  test('la fiche de version publiée est lue et comparée à la version installée', () {
    final r = AppRelease.fromJson({'build': 470100, 'apk': 'https://menewa.example/telechargement/menewa-test.apk'});
    expect(r, isNotNull);
    expect(r!.isNewerThan(469948), isTrue);
    expect(r.isNewerThan(470100), isFalse);
    // Version de développement (0) : jamais de proposition de mise à jour.
    expect(r.isNewerThan(0), isFalse);
  });

  test('une fiche invalide ou non sécurisée est ignorée', () {
    expect(AppRelease.fromJson(null), isNull);
    expect(AppRelease.fromJson({'build': 'abc', 'apk': 'https://x/a.apk'}), isNull);
    expect(AppRelease.fromJson({'build': 5, 'apk': 'http://x/a.apk'}), isNull);
  });
}
