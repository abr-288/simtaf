import 'package:flutter_test/flutter_test.dart';
import 'package:menewa_mobile/core/format.dart';

void main() {
  test('formate les montants en FCFA avec séparateur de milliers', () {
    expect(fcfa(0), '0 FCFA');
    expect(fcfa(15000), '15 000 FCFA');
    expect(fcfa(1234567), '1 234 567 FCFA');
  });

  test('formate les dates ISO en français', () {
    expect(fdate(null), '—');
    expect(fdate('2026-03-05T10:00:00Z').endsWith('2026'), isTrue);
  });

  test('libellés de statuts alignés sur la plateforme web', () {
    expect(requestStatus['info_requested']!.label, 'Infos demandées');
    expect(missionStatus['proposed']!.label, 'Proposée');
  });
}
