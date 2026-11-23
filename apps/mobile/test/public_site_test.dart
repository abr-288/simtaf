import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:menewa_mobile/core/pending.dart';
import 'package:menewa_mobile/features/public/public_home_screen.dart';
import 'package:menewa_mobile/features/public/public_pages.dart';

Widget _wrap(Widget child, [ProviderContainer? container]) {
  final app = MaterialApp(home: Scaffold(body: child));
  return container == null ? ProviderScope(child: app) : UncontrolledProviderScope(container: container, child: app);
}

void main() {
  testWidgets('À propos reprend le texte du site', (tester) async {
    await tester.pumpWidget(_wrap(const AboutPage()));
    expect(find.text('Une plateforme de solidarité de proximité'), findsOneWidget);
    expect(find.text('Transparence'), findsOneWidget);
    expect(find.text('Token Mena et gamification'), findsOneWidget);
  });

  testWidgets('pages légales et « Comment ça marche »', (tester) async {
    await tester.pumpWidget(_wrap(const LegalPage(title: 'Politique de confidentialité')));
    expect(find.textContaining('Nous collectons uniquement les données nécessaires'), findsOneWidget);
    await tester.pumpWidget(_wrap(const HowItWorksPage()));
    expect(find.text('Donateur / Client'), findsOneWidget);
    expect(find.text('Créer mon compte', skipOffstage: false), findsOneWidget);
  });

  testWidgets('le simulateur calcule les dons finançables et garde le don choisi pour après la connexion', (tester) async {
    tester.view.physicalSize = const Size(1080, 4000);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.reset);
    final container = ProviderContainer();
    addTearDown(container.dispose);
    final items = [
      {'id': 7, 'title': 'Pack riz', 'unit': 'pack', 'unitPrice': 10000, 'emoji': '🍚'},
      {'id': 8, 'title': 'Bélier', 'unit': 'bélier', 'unitPrice': 90000},
    ];
    await tester.pumpWidget(_wrap(SingleChildScrollView(child: ImpactSimulator(items: items)), container));

    // 50 000 FCFA par défaut : 5 packs, et le bélier reste hors de portée.
    expect(find.text('5'), findsOneWidget);
    expect(find.text('encore 40 000 FCFA'), findsOneWidget);
    expect(find.text('Faire un don de 50 000 FCFA'), findsOneWidget);

    await tester.tap(find.text('Pack riz'));
    await tester.pump();
    expect(find.text('Financer 5 × Pack riz'), findsOneWidget);

    // Sans session, la connexion s'ouvre et le don attend dans le panier en attente.
    await tester.tap(find.text('Financer 5 × Pack riz'));
    await tester.pumpAndSettle();
    final pending = container.read(pendingActionProvider);
    expect(pending?.isCart, isTrue);
    expect(pending?.itemId, 7);
    expect(pending?.quantity, 5);
    expect(find.textContaining('Connectez-vous pour financer le don choisi'), findsOneWidget);
  });
}
