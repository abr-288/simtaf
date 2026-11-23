import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';

/// Token Mena : solde, niveau et progression, badges, historique ; transfert et conversion selon les réglages.
class RewardsScreen extends ConsumerWidget {
  const RewardsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final api = ref.read(apiProvider);
    return AsyncView<Json>(
      load: () async => Json.from(await api.get('/v1/rewards/me') as Map),
      builder: (context, r, reload) {
        final level = r['level'] is Map ? Json.from(r['level'] as Map) : <String, dynamic>{};
        final next = r['next'] is Map ? Json.from(r['next'] as Map) : null;
        final earned = asInt(r['earned']);
        final min = asInt(level['min']);
        final progress = next == null ? 1.0 : ((earned - min) / (asInt(next['min']) - min)).clamp(0.0, 1.0);
        final levels = asList(r['levels']);
        final badges = asList(r['badges']);
        final txs = asList(r['transactions']);
        final transfer = r['transferEnabled'] == true;
        final conversion = r['conversionEnabled'] == true;
        return RefreshIndicator(
          onRefresh: reload,
          child: ListView(padding: const EdgeInsets.all(16), children: [
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(gradient: const LinearGradient(colors: [brand, Color(0xFF2F9A63)]), borderRadius: BorderRadius.circular(16)),
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Row(children: [
                  const Text('🪙', style: TextStyle(fontSize: 34)),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                      Text('${r['balance']} Mena', style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 24)),
                      Text('Niveau ${level['label'] ?? ''} · $earned Mena gagnés au total', style: const TextStyle(color: Colors.white70, fontSize: 12)),
                    ]),
                  ),
                ]),
                const SizedBox(height: 12),
                ClipRRect(
                  borderRadius: BorderRadius.circular(6),
                  child: LinearProgressIndicator(value: progress, minHeight: 8, backgroundColor: Colors.white24, color: const Color(0xFFF5B800)),
                ),
                const SizedBox(height: 6),
                Text(next == null ? 'Niveau maximal atteint' : 'Encore ${asInt(next['min']) - earned} Mena pour atteindre ${next['label']}',
                    style: const TextStyle(color: Colors.white, fontSize: 12)),
              ]),
            ),
            const SizedBox(height: 8),
            Text('Niveaux : ${levels.map((l) => '${l['label']} (${l['min']}+)').join(' · ')}', style: const TextStyle(fontSize: 12, color: Colors.black54)),
            const SizedBox(height: 12),
            SectionCard(
              title: 'Badges',
              child: Wrap(
                spacing: 8,
                runSpacing: 8,
                children: badges.map((b) {
                  final got = b['earned'] == true;
                  return Opacity(
                    opacity: got ? 1 : 0.45,
                    child: Container(
                      width: 150,
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: got ? cream : Colors.transparent,
                        border: Border.all(color: got ? const Color(0xFFF5B800) : Colors.grey.shade300),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Column(children: [
                        Text(asStr(b['emoji']) ?? '🏅', style: const TextStyle(fontSize: 22)),
                        Text(asStr(b['label']) ?? '', textAlign: TextAlign.center, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 12)),
                        Text(asStr(b['desc']) ?? '', textAlign: TextAlign.center, style: const TextStyle(fontSize: 11, color: Colors.black54)),
                      ]),
                    ),
                  );
                }).toList(),
              ),
            ),
            SectionCard(
              title: 'Historique',
              child: txs.isEmpty
                  ? const EmptyState('Aucune opération.')
                  : Column(children: txs.map((t) {
                      final amount = asInt(t['amount']);
                      return ListTile(
                        contentPadding: EdgeInsets.zero,
                        title: Text(asStr(t['reason']) ?? ''),
                        subtitle: Text(fdatetime(asStr(t['createdAt']))),
                        trailing: Column(mainAxisAlignment: MainAxisAlignment.center, crossAxisAlignment: CrossAxisAlignment.end, children: [
                          Text('${amount > 0 ? '+' : ''}$amount',
                              style: TextStyle(fontWeight: FontWeight.bold, color: amount > 0 ? Colors.green.shade700 : Colors.red.shade700)),
                          StatusChip(tokenStatus, asStr(t['status']) ?? ''),
                        ]),
                      );
                    }).toList()),
            ),
            SectionCard(
              title: 'Utiliser mes Mena',
              child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
                const Text('Offrez vos Mena en soutien symbolique à une campagne (onglet « Campagnes »).', style: TextStyle(fontSize: 13)),
                const SizedBox(height: 10),
                OutlinedButton(
                  onPressed: transfer ? () => _transfer(context, ref, reload) : null,
                  child: Text(transfer ? 'Transférer à un membre' : 'Transfert désactivé (validation juridique en attente)'),
                ),
                const SizedBox(height: 6),
                OutlinedButton(
                  onPressed: conversion ? () => _convert(context, ref, reload) : null,
                  child: Text(conversion ? 'Convertir en monnaie' : 'Conversion désactivée (validation réglementaire en attente)'),
                ),
              ]),
            ),
          ]),
        );
      },
    );
  }

  Future<void> _transfer(BuildContext context, WidgetRef ref, Future<void> Function() reload) async {
    final phone = TextEditingController();
    final amount = TextEditingController();
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Transférer des Mena'),
        content: Column(mainAxisSize: MainAxisSize.min, children: [
          TextField(controller: phone, keyboardType: TextInputType.phone, decoration: const InputDecoration(labelText: 'Téléphone du destinataire')),
          TextField(controller: amount, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Nombre de Mena')),
        ]),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Annuler')),
          FilledButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('Transférer')),
        ],
      ),
    );
    if (ok != true || !context.mounted) return;
    final api = ref.read(apiProvider);
    if (await runAction(context, () => api.post('/v1/rewards/transfer', {'phone': phone.text.trim(), 'amount': int.tryParse(amount.text) ?? 0}))) {
      await reload();
    }
  }

  Future<void> _convert(BuildContext context, WidgetRef ref, Future<void> Function() reload) async {
    final value = await promptText(context, title: 'Mena à convertir', hint: 'Nombre de Mena', minLength: 1, maxLines: 1);
    if (value == null || !context.mounted) return;
    final api = ref.read(apiProvider);
    if (await runAction(context, () => api.post('/v1/rewards/conversion', {'amount': int.tryParse(value) ?? 0}))) await reload();
  }
}
