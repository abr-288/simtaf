import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'core/session.dart';
import 'core/widgets.dart';
import 'features/auth/login_screen.dart';
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

/// Aiguille vers la connexion ou l'espace selon la session restaurée depuis le trousseau.
class SessionGate extends ConsumerWidget {
  const SessionGate({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final session = ref.watch(sessionProvider);
    if (session.loading) return const Scaffold(body: Center(child: CircularProgressIndicator()));
    if (!session.authenticated) return const LoginScreen();
    return HomeShell(user: session.user!);
  }
}
