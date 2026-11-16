import 'package:flutter/material.dart';

void main() {
  runApp(const MenewaApp());
}

class MenewaApp extends StatelessWidget {
  const MenewaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Menewa',
      theme: ThemeData(colorSchemeSeed: const Color(0xFF2563EB), useMaterial3: true),
      home: const _HomePage(),
    );
  }
}

class _HomePage extends StatelessWidget {
  const _HomePage();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Menewa')),
      body: const Center(child: Text('Bienvenue sur Menewa')),
    );
  }
}
