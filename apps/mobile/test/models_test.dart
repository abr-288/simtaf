import 'package:flutter_test/flutter_test.dart';
import 'package:menewa_mobile/core/api.g.dart';
import 'package:menewa_mobile/core/models.dart';

void main() {
  test('lit un utilisateur et ses préférences depuis la réponse de l’API', () {
    final u = AppUser.fromJson({
      'id': 12,
      'phone': '+22890000002',
      'name': 'Awa',
      'role': 'donor',
      'permissions': <String>[],
      'notifPrefs': {'inapp': true, 'sms': false, 'email': true},
    });
    expect(u.id, 12);
    expect(u.displayName, 'Awa');
    expect(u.isRequester, isTrue);
    expect(u.notifSms, isFalse);
    expect(u.notifEmail, isTrue);
  });

  test('un champ absent prend une valeur neutre au lieu de faire planter l’écran', () {
    final r = GiftRequest({'id': 3, 'ref': 'MN-2026-00003', 'status': 'submitted'});
    expect(r.totalAmount, 0);
    expect(r.title, '');
    expect(AppUser.fromJson({'phone': '+22890000003'}).notifInapp, isTrue);
  });

  test('les objets imbriqués sont typés (détail de mission prestataire)', () {
    final d = ApiProviderMissionDetail.fromJson({
      'mission': {'id': 7, 'title': 'Livraison', 'status': 'proposed', 'late': false, 'providerFee': 8000},
      'request': {'id': 3, 'title': 'Pack riz'},
      'items': [
        {'label': 'Riz 25 kg', 'quantity': 2},
      ],
      'proofs': <Object>[],
    });
    expect(d.mission!.providerFee, 8000);
    expect(d.items.single.quantity, 2);
    expect(d.beneficiary, isNull);
  });
}
