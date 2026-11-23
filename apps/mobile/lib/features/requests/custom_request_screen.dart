import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_client.dart';
import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';
import 'request_detail_screen.dart';

/// Don personnel, action ou visite : demande sur mesure, hors catalogue. L'équipe peut ajuster le budget à l'examen.
class CustomRequestScreen extends ConsumerStatefulWidget {
  const CustomRequestScreen({super.key, required this.user});

  final AppUser user;

  @override
  ConsumerState<CustomRequestScreen> createState() => _CustomRequestScreenState();
}

class _CustomRequestScreenState extends ConsumerState<CustomRequestScreen> {
  final _title = TextEditingController();
  final _description = TextEditingController();
  final _zone = TextEditingController();
  final _amount = TextEditingController();
  final _customTarget = TextEditingController();
  final _occurrences = TextEditingController(text: '3');
  final _packagingNotes = TextEditingController();
  final _other = TextEditingController();
  final _annex = <String, bool>{for (final k in annexLabels.keys) k: false};
  // Clé d'idempotence : un double envoi ne crée qu'une seule demande.
  final _idempotencyKey = '${DateTime.now().microsecondsSinceEpoch}-${Random().nextInt(1 << 30)}';
  String _kind = 'personnalise';
  String? _target;
  String _recurrence = 'none';
  String _paymentMode = 'immediate';
  bool _sending = false;

  Future<(List<String>, Json)> _load() async {
    final api = ref.read(apiProvider);
    final targets = asList(await api.get('/v1/catalog/targets')).map((t) => asStr(t['label']) ?? '').toList();
    final settings = Json.from(await api.get('/v1/settings/public') as Map);
    final prices = settings['annex.prices'] is Map ? Json.from(settings['annex.prices'] as Map) : <String, dynamic>{};
    return (targets, prices);
  }

  void _toast(String message) => ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));

  Future<void> _submit() async {
    final target = _target == '__custom__' ? _customTarget.text.trim() : (_target ?? '');
    final amount = int.tryParse(_amount.text.replaceAll(RegExp(r'\s'), '')) ?? 0;
    if (_title.text.trim().length < 3) {
      _toast('Indiquez l’intitulé de la demande.');
      return;
    }
    if (target.isEmpty) {
      _toast('Veuillez préciser la cible du don.');
      return;
    }
    if (amount < 500) {
      _toast('Le budget minimal est de 500 FCFA.');
      return;
    }
    setState(() => _sending = true);
    try {
      final created = await ref.read(apiProvider).post('/v1/requests', {
        'kind': _kind,
        'title': _title.text.trim(),
        'description': _description.text.trim(),
        'targetName': target,
        'targetZone': _zone.text.trim(),
        'amount': amount,
        'schedule': {'recurrence': _recurrence, 'occurrences': int.tryParse(_occurrences.text) ?? 2, 'paymentMode': _paymentMode},
        'annex': {..._annex, 'packagingNotes': _packagingNotes.text.trim(), 'other': _other.text.trim()},
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
      appBar: AppBar(title: const Text('Don personnel / action')),
      body: AsyncView<(List<String>, Json)>(
        load: _load,
        builder: (context, data, reload) {
          final (targets, prices) = data;
          return ListView(padding: const EdgeInsets.all(16), children: [
            const Text('Définissez librement votre don ou l’acte à poser (visite, remise de message, action de bienfaisance…). '
                'L’équipe pourra ajuster le budget lors de l’examen.', style: TextStyle(fontSize: 13, color: Colors.black54)),
            const SizedBox(height: 12),
            SectionCard(
              title: 'Ma demande',
              child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
                DropdownButtonFormField<String>(
                  initialValue: _kind,
                  decoration: const InputDecoration(labelText: 'Nature'),
                  items: const [
                    DropdownMenuItem(value: 'personnalise', child: Text('Don selon mes spécificités')),
                    DropdownMenuItem(value: 'action', child: Text('Acte à poser / visite à effectuer')),
                  ],
                  onChanged: (v) => setState(() => _kind = v ?? _kind),
                ),
                const SizedBox(height: 10),
                TextField(controller: _title, decoration: const InputDecoration(labelText: 'Intitulé', hintText: 'Ex. Repas de fête pour l’orphelinat')),
                const SizedBox(height: 10),
                TextField(controller: _description, maxLines: 4, decoration: const InputDecoration(
                    labelText: 'Description détaillée', hintText: 'Que souhaitez-vous offrir ou faire ? Pour qui ? Dans quelles conditions ?')),
                const SizedBox(height: 10),
                DropdownButtonFormField<String>(
                  initialValue: _target,
                  isExpanded: true,
                  decoration: const InputDecoration(labelText: 'Cible du don'),
                  items: [
                    ...targets.map((t) => DropdownMenuItem(value: t, child: Text(t, overflow: TextOverflow.ellipsis))),
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
                TextField(controller: _amount, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Budget par échéance (FCFA, 500 minimum)')),
              ]),
            ),
            SectionCard(
              title: 'Échéancier et paiement',
              child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
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
              ]),
            ),
            SectionCard(
              title: 'Services annexes (optionnels)',
              child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
                ...annexLabels.entries.map((e) => CheckboxListTile(
                      contentPadding: EdgeInsets.zero,
                      value: _annex[e.key],
                      onChanged: (v) => setState(() => _annex[e.key] = v ?? false),
                      title: Text(e.value),
                      subtitle: Text('+${fcfa(asInt(prices[e.key]))}'),
                    )),
                TextField(controller: _packagingNotes, decoration: const InputDecoration(labelText: 'Personnalisation du packaging')),
                const SizedBox(height: 10),
                TextField(controller: _other, decoration: const InputDecoration(labelText: 'Autre spécificité à demander')),
              ]),
            ),
            const Text('Le montant définitif est recalculé par la plateforme. Vous pourrez ensuite payer par Mobile Money depuis la page de la demande.',
                style: TextStyle(fontSize: 12, color: Colors.black54)),
            const SizedBox(height: 12),
            FilledButton(onPressed: _sending ? null : _submit, child: Text(_sending ? 'Envoi…' : 'Envoyer la demande')),
            const SizedBox(height: 24),
          ]);
        },
      ),
    );
  }
}
