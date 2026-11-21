import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api.g.dart';
import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';
import 'service_order_screen.dart';

/// Recherche d'un prestataire local et envoi d'une demande de service.
class ProviderSearchScreen extends ConsumerStatefulWidget {
  const ProviderSearchScreen({super.key});

  @override
  ConsumerState<ProviderSearchScreen> createState() => _ProviderSearchScreenState();
}

class _ProviderSearchScreenState extends ConsumerState<ProviderSearchScreen> {
  final _q = TextEditingController();
  final _zone = TextEditingController();
  String? _category;
  int _search = 0; // change la clé de la liste pour relancer la recherche

  @override
  void dispose() {
    _q.dispose();
    _zone.dispose();
    super.dispose();
  }

  String? _text(TextEditingController c) => c.text.trim().isEmpty ? null : c.text.trim();

  @override
  Widget build(BuildContext context) {
    final api = ref.read(apiProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Trouver un prestataire')),
      body: Column(children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
          child: Column(children: [
            TextField(
              controller: _q,
              decoration: const InputDecoration(labelText: 'Mot-clé', prefixIcon: Icon(Icons.search)),
              textInputAction: TextInputAction.search,
              onSubmitted: (_) => setState(() => _search++),
            ),
            Row(children: [
              Expanded(
                child: DropdownButtonFormField<String?>(
                  initialValue: _category,
                  isExpanded: true,
                  decoration: const InputDecoration(labelText: 'Catégorie'),
                  items: [
                    const DropdownMenuItem<String?>(value: null, child: Text('Toutes')),
                    ...providerCategories.entries.map((e) => DropdownMenuItem<String?>(value: e.key, child: Text(e.value))),
                  ],
                  onChanged: (v) => setState(() {
                    _category = v;
                    _search++;
                  }),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: TextField(
                  controller: _zone,
                  decoration: const InputDecoration(labelText: 'Zone'),
                  onSubmitted: (_) => setState(() => _search++),
                ),
              ),
            ]),
          ]),
        ),
        Expanded(
          child: AsyncView<List<ApiProviderCard>>(
            key: ValueKey(_search),
            load: () async => asList(await api.get('/v1/providers/search', query: {'q': _text(_q), 'cat': _category, 'zone': _text(_zone)}))
                .map(ApiProviderCard.fromJson)
                .toList(),
            builder: (context, rows, reload) => ListView(padding: const EdgeInsets.all(16), children: [
              if (rows.isEmpty) const EmptyState('Aucun prestataire trouvé.'),
              ...rows.map((p) => _ProviderCard(p)),
            ]),
          ),
        ),
      ]),
    );
  }
}

class _ProviderCard extends ConsumerWidget {
  const _ProviderCard(this.p);

  final ApiProviderCard p;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final meta = '${providerCategories[p.category] ?? p.category ?? ''} · ${p.zone ?? '—'} · ★ ${p.rating.toStringAsFixed(1)} (${p.reviews} avis)';
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(children: [
            Expanded(child: Text(p.name ?? '', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: brandDark))),
            if (p.verified) const Chip(label: Text('Vérifié'), visualDensity: VisualDensity.compact),
          ]),
          Text(meta, style: TextStyle(color: Colors.grey.shade700, fontSize: 12)),
          if ((p.description ?? '').isNotEmpty) Padding(padding: const EdgeInsets.only(top: 6), child: Text(p.description!)),
          ...p.services.where((s) => s.active).map((s) => Text('• ${s.label ?? ''} — ${fcfa(s.unitCost)} / ${s.unit ?? ''}', style: const TextStyle(fontSize: 13))),
          const SizedBox(height: 8),
          Align(
            alignment: Alignment.centerRight,
            child: FilledButton(
              onPressed: p.available ? () => _request(context, ref) : null,
              child: Text(p.available ? 'Demander ce prestataire' : 'Indisponible'),
            ),
          ),
        ]),
      ),
    );
  }

  Future<void> _request(BuildContext context, WidgetRef ref) async {
    final input = await showModalBottomSheet<Map<String, Object?>>(context: context, isScrollControlled: true, builder: (_) => const _OrderForm());
    if (input == null || !context.mounted) return;
    final api = ref.read(apiProvider);
    Json? created;
    final ok = await runAction(context, () async {
      created = Json.from(await api.post('/v1/services/orders', {...input, 'providerId': p.id}) as Map);
      return created;
    });
    final id = asInt(created?['id']);
    if (ok && id > 0 && context.mounted) {
      await Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => ServiceOrderScreen(id: id)));
    }
  }
}

class _OrderForm extends StatefulWidget {
  const _OrderForm();

  @override
  State<_OrderForm> createState() => _OrderFormState();
}

class _OrderFormState extends State<_OrderForm> {
  final _title = TextEditingController();
  final _description = TextEditingController();
  final _price = TextEditingController();
  final _exchange = TextEditingController();
  final _zone = TextEditingController();
  String _mode = 'money';

  @override
  void dispose() {
    for (final c in [_title, _description, _price, _exchange, _zone]) {
      c.dispose();
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.fromLTRB(16, 16, 16, MediaQuery.of(context).viewInsets.bottom + 16),
      child: SingleChildScrollView(
        child: Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.stretch, children: [
          const Text('Demande de service', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
          TextField(controller: _title, decoration: const InputDecoration(labelText: 'Service demandé')),
          TextField(controller: _description, maxLines: 2, decoration: const InputDecoration(labelText: 'Détails')),
          DropdownButtonFormField<String>(
            initialValue: _mode,
            decoration: const InputDecoration(labelText: 'Rémunération'),
            items: const [
              DropdownMenuItem(value: 'money', child: Text('Paiement monétaire')),
              DropdownMenuItem(value: 'exchange', child: Text('Échange de services')),
            ],
            onChanged: (v) => setState(() => _mode = v ?? 'money'),
          ),
          if (_mode == 'money')
            TextField(controller: _price, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Prix proposé (FCFA)'))
          else
            TextField(controller: _exchange, decoration: const InputDecoration(labelText: 'Engagements réciproques')),
          TextField(controller: _zone, decoration: const InputDecoration(labelText: 'Lieu / zone')),
          const SizedBox(height: 12),
          FilledButton(
            onPressed: () {
              if (_title.text.trim().length < 3) return;
              Navigator.pop(context, <String, Object?>{
                'title': _title.text.trim(),
                'description': _description.text.trim(),
                'compensationMode': _mode,
                'price': int.tryParse(_price.text.replaceAll(RegExp(r'[^0-9]'), '')) ?? 0,
                'exchangeDescription': _exchange.text.trim(),
                'zone': _zone.text.trim(),
              });
            },
            child: const Text('Envoyer la demande'),
          ),
        ]),
      ),
    );
  }
}
