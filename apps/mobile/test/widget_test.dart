import 'package:flutter_test/flutter_test.dart';
import 'package:menewa_mobile/main.dart';

void main() {
  testWidgets('affiche la page d’accueil Menewa', (tester) async {
    await tester.pumpWidget(const MenewaApp());

    expect(find.text('Bienvenue sur Menewa'), findsOneWidget);
  });
}
