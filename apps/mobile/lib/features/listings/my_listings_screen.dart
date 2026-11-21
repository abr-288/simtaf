import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api.g.dart';
import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';

/// Publications d'un partenaire : suivi de la modération et des contributions, création d'une publication.
class MyListingsScreen extends ConsumerWidget {
  const MyListingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final api = ref.read(apiProvider);
    return AsyncView<List<ApiListingView>>(
      load: () async => asList(await api.get('/v1/listings/mine')).map(ApiListingView.fromJson).toList(),
      builder: (context, rows, reload) => ListView(padding: const EdgeInsets.all(16), children: [
        FilledButton.icon(
          icon: const Icon(Icons.add),
          label: const Text('Nouvelle publication'),
          onPressed: () async {
            final input = await showModalBottomSheet<Map<String, Object?>>(context: context, isScrollControlled: true, builder: (_) => const _ListingForm());
            if (input == null || !context.mounted) return;
            if (await runAction(context, () => api.post('/v1/listings', input))) await reload();
          },
        ),
        const SizedBox(height: 12),
        if (rows.isEmpty) const EmptyState('Aucune publication. Chaque publication est modérée avant sa mise en ligne.'),
        ...rows.where((r) => r.listing != null).map((r) {
          final l = r.listing!;
          return Card(
            margin: const EdgeInsets.only(bottom: 10),
            child: ListTile(
              title: Text(l.title ?? '', style: const TextStyle(fontWeight: FontWeight.w600)),
              subtitle: Text([
                listingTypes[l.type] ?? l.type ?? '',
                if (l.goalAmount > 0) '${fcfa(l.raisedAmount)} / ${fcfa(l.goalAmount)} (${r.confirmedContributions} contrib.)',
                if ((l.rejectionReason ?? '').isNotEmpty) 'Refus : ${l.rejectionReason}',
              ].join('\n')),
              isThreeLine: true,
              trailing: StatusChip(listingStatus, l.status ?? ''),
            ),
          );
        }),
      ]),
    );
  }
}

class _ListingForm extends StatefulWidget {
  const _ListingForm();

  @override
  State<_ListingForm> createState() => _ListingFormState();
}

class _ListingFormState extends State<_ListingForm> {
  final _title = TextEditingController();
  final _description = TextEditingController();
  final _zone = TextEditingController();
  final _goal = TextEditingController();
  String _type = 'crowdfunding';
  DateTime? _endsAt;

  @override
  void dispose() {
    for (final c in [_title, _description, _zone, _goal]) {
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
          const Text('Nouvelle publication', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
          DropdownButtonFormField<String>(
            initialValue: _type,
            decoration: const InputDecoration(labelText: 'Type'),
            items: listingTypes.entries.map((e) => DropdownMenuItem(value: e.key, child: Text(e.value))).toList(),
            onChanged: (v) => setState(() => _type = v ?? _type),
          ),
          TextField(controller: _title, decoration: const InputDecoration(labelText: 'Titre')),
          TextField(controller: _description, maxLines: 3, decoration: const InputDecoration(labelText: 'Description')),
          TextField(controller: _zone, decoration: const InputDecoration(labelText: 'Zone')),
          TextField(controller: _goal, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Objectif (FCFA)')),
          const SizedBox(height: 8),
          OutlinedButton.icon(
            icon: const Icon(Icons.event),
            label: Text(_endsAt == null ? 'Date de fin (facultatif)' : 'Fin : ${fdate(_endsAt!.toIso8601String())}'),
            onPressed: () async {
              final now = DateTime.now();
              final picked = await showDatePicker(context: context, firstDate: now, lastDate: now.add(const Duration(days: 730)), initialDate: _endsAt ?? now);
              if (picked != null) setState(() => _endsAt = picked);
            },
          ),
          const SizedBox(height: 12),
          FilledButton(
            onPressed: () {
              if (_title.text.trim().length < 3) return;
              final end = _endsAt;
              Navigator.pop(context, <String, Object?>{
                'type': _type,
                'title': _title.text.trim(),
                'description': _description.text.trim(),
                'zone': _zone.text.trim(),
                'goalAmount': int.tryParse(_goal.text.replaceAll(RegExp(r'[^0-9]'), '')) ?? 0,
                if (end != null) 'endsAt': '${end.year}-${end.month.toString().padLeft(2, '0')}-${end.day.toString().padLeft(2, '0')}',
              });
            },
            child: const Text('Soumettre à modération'),
          ),
        ]),
      ),
    );
  }
}
