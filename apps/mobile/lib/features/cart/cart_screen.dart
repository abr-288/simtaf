import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_client.dart';
import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';
import '../requests/request_detail_screen.dart';

/// Panier et validation de la demande (cible, échéancier, services annexes).
class CartScreen extends ConsumerStatefulWidget {
  const CartScreen({super.key, required this.user});

  final AppUser user;

  @override
  ConsumerState<CartScreen> createState() => _CartScreenState();
}

class _CartScreenState extends ConsumerState<CartScreen> {
  final _zone = TextEditingController();
  final _description = TextEditingController();
  final _customTarget = TextEditingController();
  final _occurrences = TextEditingController(text: '3');
  final _annex = <String, bool>{for (final k in annexLabels.keys) k: false};
  // Clé d'idempotence : un double envoi ne crée qu'une seule demande.
  final _idempotencyKey = '${DateTime.now().microsecondsSinceEpoch}-${Random().nextInt(1 << 30)}';
  String? _target;
  String _recurrence = 'none';
  String _paymentMode = 'immediate';
  bool _sending = false;

  Future<(Json, List<String>)> _load() async {
    final api = ref.read(apiProvider);
    final cart = Json.from(await api.get('/v1/cart') as Map);
    final targets = asList(await api.get('/v1/catalog/targets')).map((t) => asStr(t['label']) ?? '').toList();
    return (cart, targets);
  }

  Future<void> _checkout() async {
    final target = _target == '__custom__' ? _customTarget.text.trim() : (_target ?? '');
    if (target.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Veuillez préciser la cible du don.')));
      return;
    }
    setState(() => _sending = true);
    try {
      final created = await ref.read(apiProvider).post('/v1/cart/checkout', {
        'targetName': target,
        'targetZone': _zone.text.trim(),
        'description': _description.text.trim(),
        'schedule': {'recurrence': _recurrence, 'occurrences': int.tryParse(_occurrences.text) ?? 2, 'paymentMode': _paymentMode},
        'annex': {..._annex},
        'idempotencyKey': _idempotencyKey,
      }) as Map;
      if (!mounted) return;
      await Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => RequestDetailScreen(id: asInt(created['id']))));
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message), backgroundColor: Colors.red.shade700));
    } finally {
      if (mounted) setState(() => _sending = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Mon panier')),
      body: AsyncView<(Json, List<String>)>(
        load: _load,
        builder: (context, data, reload) {
          final (cart, targets) = data;
          final lines = asList(cart['lines']);
          final prices = cart['annexPrices'] is Map ? Json.from(cart['annexPrices'] as Map) : <String, dynamic>{};
          if (lines.isEmpty) return ListView(padding: const EdgeInsets.all(16), children: const [EmptyState('Votre panier est vide.')]);
          return ListView(padding: const EdgeInsets.all(16), children: [
            SectionCard(
              title: 'Articles',
              child: Column(children: [
                ...lines.map((l) => ListTile(
                      contentPadding: EdgeInsets.zero,
                      leading: Text(asStr(l['emoji']) ?? '🎁', style: const TextStyle(fontSize: 26)),
                      title: Text(asStr(l['title']) ?? ''),
                      subtitle: Text('${l['quantity']} × ${fcfa(asInt(l['unitPrice']))}${l['target'] != null ? ' · cible : ${l['target']}' : ''}'),
                      trailing: IconButton(
                        icon: const Icon(Icons.delete_outline, color: Colors.red),
                        tooltip: 'Retirer',
                        onPressed: () async {
                          if (await runAction(context, () => ref.read(apiProvider).delete('/v1/cart/${l['id']}'))) await reload();
                        },
                      ),
                    )),
                const Divider(),
                Row(children: [
                  const Expanded(child: Text('Total (hors annexes)', style: TextStyle(fontWeight: FontWeight.bold))),
                  Text(fcfa(asInt(cart['total'])), style: const TextStyle(fontWeight: FontWeight.bold, color: brand)),
                ]),
              ]),
            ),
            SectionCard(
              title: 'Valider ma demande',
              child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
                DropdownButtonFormField<String>(
                  initialValue: _target,
                  decoration: const InputDecoration(labelText: 'Cible du don'),
                  items: [
                    ...targets.map((t) => DropdownMenuItem(value: t, child: Text(t))),
                    if (widget.user.role == 'donor') const DropdownMenuItem(value: '__custom__', child: Text('Autre cible (personnalisée)…')),
                  ],
                  onChanged: (v) => setState(() => _target = v),
                ),
                if (_target == '__custom__') ...[
                  const SizedBox(height: 10),
                  TextField(controller: _customTarget, decoration: const InputDecoration(labelText: 'Précisez la cible (ex. Famille Mensah)')),
                ],
                const SizedBox(height: 10),
                TextField(controller: _zone, decoration: const InputDecoration(labelText: 'Zone / ville')),
                const SizedBox(height: 10),
                TextField(controller: _description, maxLines: 2, decoration: const InputDecoration(labelText: 'Précisions pour la remise')),
                const SizedBox(height: 16),
                const Text('Échéancier et paiement', style: TextStyle(fontWeight: FontWeight.bold, color: brandDark)),
                const SizedBox(height: 8),
                DropdownButtonFormField<String>(
                  initialValue: _recurrence,
                  decoration: const InputDecoration(labelText: 'Fréquence'),
                  items: recurrences.entries.map((e) => DropdownMenuItem(value: e.key, child: Text(e.value))).toList(),
                  onChanged: (v) => setState(() => _recurrence = v ?? 'none'),
                ),
                if (_recurrence != 'none') ...[
                  const SizedBox(height: 10),
                  TextField(controller: _occurrences, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Nombre d’échéances (2 à 60)')),
                  const SizedBox(height: 10),
                  DropdownButtonFormField<String>(
                    initialValue: _paymentMode,
                    decoration: const InputDecoration(labelText: 'Mode de paiement'),
                    items: paymentModes.entries.map((e) => DropdownMenuItem(value: e.key, child: Text(e.value))).toList(),
                    onChanged: (v) => setState(() => _paymentMode = v ?? 'immediate'),
                  ),
                ],
                const SizedBox(height: 16),
                const Text('Services annexes (optionnels)', style: TextStyle(fontWeight: FontWeight.bold, color: brandDark)),
                ...annexLabels.entries.map((e) => CheckboxListTile(
                      contentPadding: EdgeInsets.zero,
                      value: _annex[e.key],
                      onChanged: (v) => setState(() => _annex[e.key] = v ?? false),
                      title: Text(e.value),
                      subtitle: Text('+${fcfa(asInt(prices[e.key]))}'),
                    )),
                const SizedBox(height: 8),
                const Text('Le montant définitif est recalculé par la plateforme. Vous pourrez ensuite payer par Mobile Money depuis la page de la demande.',
                    style: TextStyle(fontSize: 12, color: Colors.black54)),
                const SizedBox(height: 12),
                FilledButton(onPressed: _sending ? null : _checkout, child: Text(_sending ? 'Envoi…' : 'Envoyer la demande')),
              ]),
            ),
          ]);
        },
      ),
    );
  }
}
