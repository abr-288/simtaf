import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'core/session.dart';
import 'core/widgets.dart';
import 'features/public/public_shell.dart';
import 'features/home/home_shell.dart';

void main() {
  runApp(const ProviderScope(child: MenewaApp()));
}

class MenewaApp extends StatelessWidget {
  const MenewaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Menewa Naya',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: brand, primary: brand, secondary: sun),
        scaffoldBackgroundColor: const Color(0xFFF8FAFC),
        cardTheme: CardThemeData(
          elevation: 0,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16), side: BorderSide(color: Colors.grey.shade200)),
        ),
        inputDecorationTheme: InputDecorationTheme(
          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
          isDense: true,
        ),
      ),
      home: const SessionGate(),
    );
  }
}

/// Aiguille vers le site public ou l'espace membre selon la session restaurée depuis le trousseau.
class SessionGate extends ConsumerWidget {
  const SessionGate({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // Déconnexion (ou session révoquée) depuis un écran ouvert par-dessus : retour à l'écran de connexion.
    // Connexion ou déconnexion depuis un écran ouvert par-dessus : retour au premier écran (espace ou site public).
    ref.listen(sessionProvider, (previous, next) {
      if (previous?.authenticated != next.authenticated && !next.loading) Navigator.of(context).popUntil((route) => route.isFirst);
    });
    final session = ref.watch(sessionProvider);
    if (session.loading) return const Scaffold(body: Center(child: CircularProgressIndicator()));
    if (!session.authenticated) return const PublicShell();
    return HomeShell(user: session.user!);
  }
}
