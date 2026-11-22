import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_client.dart';
import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';

/// Opérateurs Mobile Money proposés au Togo.
const operators = {'tmoney': 'Mixx by Yas (T-Money)', 'flooz': 'Flooz (Moov Africa)'};
const _txStatus = {'pending': 'En attente de confirmation', 'succeeded': 'Confirmé', 'failed': 'Échoué'};

/// Fournisseur de paiement actif (« simulated » en zone de test : aucun argent ne circule).
final paymentConfigProvider = FutureProvider<Json>((ref) async => Json.from(await ref.read(apiProvider).get('/v1/payments/online/config') as Map));

/// Paiement Mobile Money d'un paiement du membre : bouton « Payer », transaction en attente et historique.
/// En mode simulé, le testeur choisit l'issue (confirmé / échec) ; sinon le membre confirme sur son téléphone.
class OnlinePaymentPanel extends ConsumerStatefulWidget {
  const OnlinePaymentPanel({super.key, required this.payment, required this.onChanged});

  final Json payment;
  final Future<void> Function() onChanged;

  @override
  ConsumerState<OnlinePaymentPanel> createState() => _OnlinePaymentPanelState();
}

class _OnlinePaymentPanelState extends ConsumerState<OnlinePaymentPanel> {
  List<Json> _txs = const [];
  bool _busy = false;

  int get _id => asInt(widget.payment['id']);

  bool get _payable {
    final p = widget.payment;
    return asStr(p['mode']) == 'money' && asInt(p['remaining']) > 0 &&
        const {'planned', 'declared', 'validated', 'partial'}.contains(asStr(p['status']));
  }

  Json? get _pending => _txs.where((t) => t['status'] == 'pending').firstOrNull;

  @override
  void initState() {
    super.initState();
    if (asStr(widget.payment['mode']) == 'money') _load();
  }

  Future<void> _load() async {
    try {
      final list = await ref.read(apiProvider).get('/v1/payments/$_id/transactions');
      if (mounted) setState(() => _txs = asList(list));
    } on ApiException {
      // Historique indisponible : le bouton reste utilisable, l'API refusera un doublon.
    }
  }

  Future<void> _act(Future<dynamic> Function() call) async {
    final messenger = ScaffoldMessenger.of(context);
    setState(() => _busy = true);
    try {
      final res = await call();
      final message = res is Map ? asStr(res['message']) : null;
      if (message != null) messenger.showSnackBar(SnackBar(content: Text(message)));
      await _load();
      await widget.onChanged();
    } on ApiException catch (e) {
      messenger.showSnackBar(SnackBar(content: Text(e.message), backgroundColor: Colors.red.shade700));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _pay() async {
    final phone = TextEditingController(text: ref.read(sessionProvider).user?.phone ?? '');
    var operator = operators.keys.first;
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setDialog) => AlertDialog(
          title: Text('Payer ${fcfa(asInt(widget.payment['remaining']))}'),
          content: Column(mainAxisSize: MainAxisSize.min, children: [
            DropdownButtonFormField<String>(
              initialValue: operator,
              isExpanded: true,
              decoration: const InputDecoration(labelText: 'Opérateur'),
              items: operators.entries.map((e) => DropdownMenuItem(value: e.key, child: Text(e.value))).toList(),
              onChanged: (v) => setDialog(() => operator = v ?? operator),
            ),
            const SizedBox(height: 10),
            TextField(controller: phone, keyboardType: TextInputType.phone, decoration: const InputDecoration(labelText: 'Numéro Mobile Money')),
            const SizedBox(height: 8),
            const Text('Vous recevrez une demande de confirmation sur ce téléphone.', style: TextStyle(fontSize: 12)),
          ]),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Annuler')),
            FilledButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('Payer')),
          ],
        ),
      ),
    );
    if (ok != true || !mounted) return;
    await _act(() => ref.read(apiProvider).post('/v1/payments/$_id/pay', {'phone': phone.text.trim(), 'operator': operator}));
  }

  @override
  Widget build(BuildContext context) {
    final simulated = ref.watch(paymentConfigProvider).valueOrNull?['simulated'] == true;
    final pending = _pending;
    return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
      if (pending != null)
        Container(
          margin: const EdgeInsets.only(top: 8),
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(color: Colors.amber.shade50, border: Border.all(color: Colors.amber.shade300), borderRadius: BorderRadius.circular(10)),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('${fcfa(asInt(pending['amount']))} en attente : confirmez sur le ${pending['phone']} '
                '(${operators[pending['operator']] ?? pending['operator']}).'),
            const SizedBox(height: 6),
            if (simulated) ...[
              const Text('Zone de test : aucun argent ne circule, choisissez l’issue.', style: TextStyle(fontSize: 12)),
              Wrap(spacing: 8, children: [
                FilledButton(
                  onPressed: _busy ? null : () => _act(() => ref.read(apiProvider).post('/v1/payments/transactions/${pending['id']}/simulate', {'outcome': 'success'})),
                  child: const Text('Simuler : confirmé'),
                ),
                OutlinedButton(
                  onPressed: _busy ? null : () => _act(() => ref.read(apiProvider).post('/v1/payments/transactions/${pending['id']}/simulate', {'outcome': 'failure'})),
                  child: const Text('Simuler : échec'),
                ),
              ]),
            ] else
              TextButton(onPressed: _busy ? null : () => _act(() async => null), child: const Text('J’ai confirmé — actualiser')),
          ]),
        )
      else if (_payable)
        Padding(
          padding: const EdgeInsets.only(top: 8),
          child: FilledButton.icon(
            icon: const Icon(Icons.phone_android),
            label: Text(_busy ? 'Envoi…' : 'Payer ${fcfa(asInt(widget.payment['remaining']))} par Mobile Money'),
            onPressed: _busy ? null : _pay,
          ),
        ),
      for (final t in _txs)
        Padding(
          padding: const EdgeInsets.only(top: 4),
          child: Text('${fdatetime(asStr(t['createdAt']))} · ${fcfa(asInt(t['amount']))} · ${_txStatus[t['status']] ?? t['status']}'
              '${t['providerRef'] != null ? ' · réf. ${t['providerRef']}' : ''}',
              style: const TextStyle(fontSize: 11, color: Colors.black54)),
        ),
    ]);
  }
}
