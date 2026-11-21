import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api.g.dart';
import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';

/// Campagnes et actions publiées par les partenaires : progression, contribution financière, soutien en Mena.
class CampaignsScreen extends ConsumerWidget {
  const CampaignsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final api = ref.read(apiProvider);
    return AsyncView<List<ApiListingView>>(
      load: () async => asList(await api.get('/v1/listings/published')).map(ApiListingView.fromJson).toList(),
      builder: (context, rows, reload) => ListView(padding: const EdgeInsets.all(16), children: [
        if (rows.isEmpty) const EmptyState('Aucune campagne publiée pour le moment.'),
        ...rows.where((r) => r.listing != null).map((r) => _CampaignCard(view: r, reload: reload)),
      ]),
    );
  }
}

class _CampaignCard extends ConsumerWidget {
  const _CampaignCard({required this.view, required this.reload});

  final ApiListingView view;
  final Future<void> Function() reload;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = view.listing!;
    final api = ref.read(apiProvider);
    final ratio = l.goalAmount > 0 ? (l.raisedAmount / l.goalAmount).clamp(0.0, 1.0) : 0.0;
    final owner = (view.organization ?? '').isNotEmpty ? view.organization! : (view.ownerName ?? '');
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(children: [
            Chip(label: Text(listingTypes[l.type] ?? l.type ?? ''), backgroundColor: cream, visualDensity: VisualDensity.compact),
            const Spacer(),
            Flexible(child: Text(owner, style: TextStyle(fontSize: 12, color: Colors.grey.shade700), overflow: TextOverflow.ellipsis)),
          ]),
          Text(l.title ?? '', style: const TextStyle(fontSize: 17, fontWeight: FontWeight.bold, color: brandDark)),
          if ((l.description ?? '').isNotEmpty) Padding(padding: const EdgeInsets.only(top: 4), child: Text(l.description!)),
          Text('${l.zone ?? ''}${l.endsAt != null ? ' · jusqu’au ${fdate(l.endsAt)}' : ''}', style: TextStyle(fontSize: 12, color: Colors.grey.shade700)),
          if (l.goalAmount > 0) ...[
            const SizedBox(height: 10),
            ClipRRect(borderRadius: BorderRadius.circular(6), child: LinearProgressIndicator(value: ratio, minHeight: 8, color: brand, backgroundColor: mint)),
            const SizedBox(height: 4),
            Text('${fcfa(l.raisedAmount)} sur ${fcfa(l.goalAmount)} (${(ratio * 100).round()} %) · ${l.menaSupport} Mena offerts', style: const TextStyle(fontSize: 12)),
          ],
          if (l.type == 'crowdfunding') ...[
            const SizedBox(height: 8),
            Wrap(spacing: 8, children: [
              FilledButton(
                onPressed: () async {
                  final input = await _askContribution(context);
                  if (input == null || !context.mounted) return;
                  if (await runAction(context, () => api.post('/v1/listings/${l.id}/contributions', input))) await reload();
                },
                child: const Text('Contribuer'),
              ),
              OutlinedButton(
                onPressed: () async {
                  final text = await promptText(context, title: 'Mena à offrir', hint: 'Nombre de Mena', minLength: 1, maxLines: 1);
                  final amount = int.tryParse((text ?? '').replaceAll(RegExp(r'[^0-9]'), ''));
                  if (amount == null || amount < 1 || !context.mounted) return;
                  if (await runAction(context, () => api.post('/v1/listings/${l.id}/mena', {'amount': amount}))) await reload();
                },
                child: const Text('Offrir des Mena'),
              ),
            ]),
          ],
        ]),
      ),
    );
  }

  static Future<Map<String, Object?>?> _askContribution(BuildContext context) {
    final amount = TextEditingController();
    final reference = TextEditingController();
    var method = 'mobile_money';
    return showDialog<Map<String, Object?>>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setState) => AlertDialog(
          title: const Text('Ma contribution'),
          content: Column(mainAxisSize: MainAxisSize.min, children: [
            TextField(controller: amount, keyboardType: TextInputType.number, autofocus: true, decoration: const InputDecoration(labelText: 'Montant (FCFA)')),
            DropdownButtonFormField<String>(
              initialValue: method,
              decoration: const InputDecoration(labelText: 'Moyen de paiement'),
              items: payMethods.entries.map((e) => DropdownMenuItem(value: e.key, child: Text(e.value))).toList(),
              onChanged: (v) => setState(() => method = v ?? method),
            ),
            TextField(controller: reference, decoration: const InputDecoration(labelText: 'Référence de transaction')),
            const SizedBox(height: 8),
            const Text('La contribution est confirmée par l’administration après vérification du paiement.', style: TextStyle(fontSize: 12)),
          ]),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Annuler')),
            FilledButton(
              onPressed: () {
                final value = int.tryParse(amount.text.replaceAll(RegExp(r'[^0-9]'), '')) ?? 0;
                if (value < 500) return;
                Navigator.pop(ctx, <String, Object?>{'amount': value, 'method': method, 'reference': reference.text.trim()});
              },
              child: const Text('Enregistrer'),
            ),
          ],
        ),
      ),
    );
  }
}
