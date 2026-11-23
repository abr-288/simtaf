import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:menewa_mobile/core/format.dart';
import 'package:menewa_mobile/core/models.dart';
import 'package:menewa_mobile/features/help/help_screen.dart';
import 'package:menewa_mobile/features/payments/online_payment.dart';
import 'package:menewa_mobile/features/public/public_pages.dart';

void main() {
  final user = AppUser(id: 1, phone: '+22890000001', name: 'Afi', role: 'donor');

  testWidgets('l’aide affiche la FAQ et le formulaire de contact', (tester) async {
    await tester.pumpWidget(ProviderScope(child: MaterialApp(home: Scaffold(body: HelpScreen(user: user)))));
    expect(find.text('Comment payer ?'), findsOneWidget);
    expect(find.text('Écrire à l’équipe'), findsOneWidget);
    expect(find.text('Réponse au +22890000001.'), findsOneWidget);

    await tester.tap(find.text('Comment payer ?'));
    await tester.pumpAndSettle();
    expect(find.textContaining('Payer par Mobile Money'), findsOneWidget);
  });

  test('les réponses de l’aide reflètent le paiement Mobile Money et la clôture des missions', () {
    final answers = siteFaq.map((q) => q.$2).join(' ');
    expect(answers, contains('Mobile Money'));
    expect(answers, contains('Démarrez la mission'));
  });

  test('opérateurs et statuts Mena disponibles', () {
    expect(operators.keys, containsAll(['tmoney', 'flooz']));
    expect(tokenStatus, isNotEmpty);
  });
}
