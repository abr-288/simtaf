import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_client.dart';
import '../../core/format.dart';
import '../../core/reference.g.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';

/// Connexion et inscription par téléphone + code SMS (OTP).
class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key, this.initialRegister = false, this.reason});

  /// Ouvre directement l'onglet « Inscription ».
  final bool initialRegister;

  /// Pourquoi la connexion est demandée (ex. financer le don choisi dans le simulateur).
  final String? reason;

  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  final _phone = TextEditingController();
  final _name = TextEditingController();
  final _code = TextEditingController();
  late bool _register = widget.initialRegister;
  String _role = 'donor';
  bool _codeStep = false;
  bool _busy = false;
  String? _devCode;
  String? _error;

  @override
  void dispose() {
    _phone.dispose();
    _name.dispose();
    _code.dispose();
    super.dispose();
  }

  Future<void> _requestCode() async {
    if (_register && _name.text.trim().length < 2) {
      setState(() => _error = 'Veuillez indiquer votre nom.');
      return;
    }
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      final dev = await ref.read(sessionProvider.notifier)
          .requestOtp(phone: _phone.text.trim(), register: _register, name: _name.text.trim(), role: _role);
      setState(() {
        _codeStep = true;
        _devCode = dev;
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _verify() async {
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      await ref.read(sessionProvider.notifier).verifyOtp(_phone.text.trim(), _code.text.replaceAll(RegExp(r'\D'), ''));
    } on ApiException catch (e) {
      if (mounted) setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: mint,
      appBar: Navigator.of(context).canPop() ? AppBar(backgroundColor: mint, surfaceTintColor: mint) : null,
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(20),
            child: Column(children: [
              const Text('Menewa Naya', style: TextStyle(fontSize: 30, fontWeight: FontWeight.w800, color: brand)),
              const Text('Ensemble, on fait la différence.', style: TextStyle(color: brandDark)),
              const SizedBox(height: 24),
              if (widget.reason != null)
                Container(
                  margin: const EdgeInsets.only(bottom: 14),
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(color: const Color(0xFFFFF3D6), borderRadius: BorderRadius.circular(10)),
                  child: Text(widget.reason!, style: const TextStyle(fontSize: 13, color: Color(0xFF6B4A07))),
                ),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: _codeStep ? _codeForm() : _phoneForm(),
                ),
              ),
            ]),
          ),
        ),
      ),
    );
  }

  Widget _phoneForm() {
    return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
      SegmentedButton<bool>(
        segments: const [ButtonSegment(value: false, label: Text('Connexion')), ButtonSegment(value: true, label: Text('Inscription'))],
        selected: {_register},
        onSelectionChanged: (s) => setState(() => _register = s.first),
      ),
      const SizedBox(height: 16),
      TextField(controller: _phone, keyboardType: TextInputType.phone, decoration: const InputDecoration(labelText: 'Numéro de téléphone', hintText: '+228 90 00 00 00')),
      if (_register) ...[
        const SizedBox(height: 12),
        TextField(controller: _name, decoration: const InputDecoration(labelText: 'Nom complet')),
        const SizedBox(height: 12),
        DropdownButtonFormField<String>(
          initialValue: _role,
          decoration: const InputDecoration(labelText: 'Je suis…'),
          items: selfRegisterRoles.map((r) => DropdownMenuItem(value: r, child: Text(roleLabels[r]!))).toList(),
          onChanged: (v) => setState(() => _role = v ?? 'donor'),
        ),
      ],
      if (_error != null) ...[const SizedBox(height: 12), Text(_error!, style: const TextStyle(color: Colors.red))],
      const SizedBox(height: 16),
      FilledButton(onPressed: _busy ? null : _requestCode, child: Text(_busy ? 'Envoi…' : 'Recevoir un code par SMS')),
    ]);
  }

  Widget _codeForm() {
    return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
      const Text('Vérifiez votre numéro', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: brandDark)),
      const SizedBox(height: 8),
      Text('Un code à 6 chiffres a été envoyé au ${_phone.text.trim()}.'),
      if (_devCode != null)
        Container(
          margin: const EdgeInsets.only(top: 12),
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(color: cream, borderRadius: BorderRadius.circular(12)),
          child: Text('Mode démonstration — code : $_devCode', style: const TextStyle(fontWeight: FontWeight.bold)),
        ),
      const SizedBox(height: 12),
      TextField(
        controller: _code,
        keyboardType: TextInputType.number,
        maxLength: 6,
        autofillHints: const [AutofillHints.oneTimeCode],
        textAlign: TextAlign.center,
        style: const TextStyle(fontSize: 22, letterSpacing: 8),
        decoration: const InputDecoration(labelText: 'Code de vérification', counterText: ''),
      ),
      if (_error != null) ...[const SizedBox(height: 8), Text(_error!, style: const TextStyle(color: Colors.red))],
      const SizedBox(height: 12),
      FilledButton(onPressed: _busy ? null : _verify, child: Text(_busy ? 'Vérification…' : 'Vérifier et continuer')),
      TextButton(onPressed: _busy ? null : () => setState(() => _codeStep = false), child: const Text('Changer de numéro')),
    ]);
  }
}
