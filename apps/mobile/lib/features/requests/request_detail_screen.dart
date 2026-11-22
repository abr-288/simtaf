import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api.g.dart';
import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';
import '../disputes/dispute_screen.dart';
import '../payments/online_payment.dart';

const _steps = ['submitted', 'accepted', 'in_progress', 'completed', 'closed'];
const _reached = {'submitted': 0, 'info_requested': 0, 'accepted': 1, 'in_progress': 2, 'completed': 3, 'closed': 4};

/// Détail d'une demande : suivi, preuves validées, paiements, échéancier, historique et actions du donateur.
class RequestDetailScreen extends ConsumerWidget {
  const RequestDetailScreen({super.key, required this.id});

  final int id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final api = ref.read(apiProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Ma demande')),
      body: AsyncView<Json>(
        load: () async => Json.from(await api.get('/v1/requests/$id') as Map),
        builder: (context, d, reload) {
          final r = Json.from(d['request'] as Map);
          final status = asStr(r['status']) ?? '';
          final annex = r['annexServices'] is Map ? Json.from(r['annexServices'] as Map) : <String, dynamic>{};
          final annexText = annexLabels.entries.where((e) => annex[e.key] == true).map((e) => e.value).join(', ');
          return ListView(padding: const EdgeInsets.all(16), children: [
            Text(asStr(r['title']) ?? '', style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: brandDark)),
            Text('${r['ref']} · ${kindLabels[r['kind']] ?? r['kind']} · créée le ${fdate(asStr(r['createdAt']))}', style: const TextStyle(color: Colors.black54)),
            const SizedBox(height: 12),
            _Tracker(status: status),
            if (status == 'rejected') _banner(Colors.red.shade50, 'Demande refusée. Motif : ${r['rejectionReason'] ?? '—'}'),
            if (status == 'info_requested')
              Card(
                color: Colors.amber.shade50,
                margin: const EdgeInsets.only(bottom: 14),
                child: Padding(
                  padding: const EdgeInsets.all(14),
                  child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                    const Text('L’équipe a besoin d’informations complémentaires :', style: TextStyle(fontWeight: FontWeight.bold)),
                    Text(asStr(r['infoRequest']) ?? ''),
                    const SizedBox(height: 8),
                    FilledButton(
                      onPressed: () async {
                        final answer = await promptText(context, title: 'Votre réponse', minLength: 3);
                        if (answer == null || !context.mounted) return;
                        if (await runAction(context, () => api.post('/v1/requests/$id/answer', {'answer': answer}))) await reload();
                      },
                      child: const Text('Répondre'),
                    ),
                  ]),
                ),
              ),
            SectionCard(
              title: 'Détails',
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                _row('Cible', '${r['targetName'] ?? '—'}${r['targetZone'] != null ? ' (${r['targetZone']})' : ''}'),
                _row('Montant total', fcfa(asInt(r['totalAmount']))),
                _row('Paiement', paymentModes[r['paymentMode']] ?? ''),
                _row('Fréquence', '${recurrences[r['recurrence']] ?? ''}${asInt(r['occurrences']) > 1 ? ' × ${r['occurrences']}' : ''}'),
                if (annexText.isNotEmpty) _row('Services annexes', annexText),
                const Divider(),
                ...asList(d['items']).map((i) => Text('• ${i['quantity']} × ${i['label']} (${fcfa(asInt(i['unitPrice']))})${i['target'] != null ? ' — ${i['target']}' : ''}')),
              ]),
            ),
            SectionCard(
              title: 'Preuves de remise',
              child: asList(d['proofs']).isEmpty
                  ? const EmptyState('Aucune preuve disponible pour le moment. Elles apparaissent après contrôle de l’administration.')
                  : Column(children: asList(d['proofs']).map((p) => Padding(
                        padding: const EdgeInsets.only(bottom: 10),
                        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                          if ((asStr(p['mime']) ?? '').startsWith('image/')) ProtectedImage(kind: 'proof', id: asInt(p['id'])),
                          Text('${proofTypes[p['type']] ?? p['type']} — ${p['caption'] ?? p['fileName']}'),
                        ]),
                      )).toList()),
            ),
            SectionCard(
              title: 'Paiements',
              child: asList(d['payments']).isEmpty
                  ? const EmptyState('Aucun paiement.')
                  : Column(children: asList(d['payments']).map((p) => _PaymentTile(payment: p, onChanged: reload)).toList()),
            ),
            if (asList(d['missions']).isNotEmpty)
              SectionCard(
                title: 'Avancement de la réalisation',
                child: Column(children: asList(d['missions']).map((m) => ListTile(
                      contentPadding: EdgeInsets.zero,
                      title: Text(asStr(m['title']) ?? ''),
                      subtitle: Text('Prestataire : ${m['providerName'] ?? 'Équipe Menewa'} · échéance ${fdate(asStr(m['dueDate']))}'),
                      trailing: StatusChip(missionStatus, asStr(m['status']) ?? ''),
                    )).toList()),
              ),
            if (asList(d['installments']).length > 1)
              SectionCard(
                title: 'Échéancier',
                child: Column(children: asList(d['installments']).map((i) => _row('#${i['seq']} · ${fdate(asStr(i['dueDate']))}',
                    '${fcfa(asInt(i['amount']))} ${i['status'] == 'done' ? '✅' : '⏳'}')).toList()),
              ),
            if (asList(d['disputes']).isNotEmpty)
              section('Litiges', [
                ...asList(d['disputes']).map(ApiDisputeRow.fromJson).map((x) => ListTile(
                      contentPadding: EdgeInsets.zero,
                      title: Text(x.subject ?? ''),
                      subtitle: Text('${x.ref ?? ''} · ${fdate(x.createdAt)}'),
                      trailing: StatusChip(disputeStatus, x.status ?? ''),
                      onTap: () async {
                        await Navigator.push(context, MaterialPageRoute(builder: (_) => DisputeScreen(id: x.id ?? 0)));
                        await reload();
                      },
                    )),
              ]),
            SectionCard(
              title: 'Historique',
              child: Column(children: asList(d['history']).map((h) => ListTile(
                    contentPadding: EdgeInsets.zero,
                    leading: StatusChip(requestStatus, asStr(h['status']) ?? ''),
                    title: Text(asStr(h['note']) ?? ''),
                    subtitle: Text(fdatetime(asStr(h['createdAt']))),
                  )).toList()),
            ),
            if (status == 'submitted' || status == 'info_requested')
              OutlinedButton.icon(
                icon: const Icon(Icons.cancel_outlined, color: Colors.red),
                label: const Text('Annuler cette demande', style: TextStyle(color: Colors.red)),
                onPressed: () async {
                  if (await runAction(context, () => api.post('/v1/requests/$id/cancel'))) await reload();
                },
              ),
            if (d['canReview'] == true)
              FilledButton.icon(
                icon: const Icon(Icons.star_outline),
                label: const Text('Noter le prestataire'),
                onPressed: () async {
                  final rating = await _askRating(context);
                  if (rating == null || !context.mounted) return;
                  if (await runAction(context, () => api.post('/v1/requests/$id/review', {'rating': rating.$1, 'comment': rating.$2}))) await reload();
                },
              ),
            if (status != 'cancelled' && status != 'rejected')
              TextButton.icon(
                icon: const Icon(Icons.report_problem_outlined),
                label: const Text('Signaler un problème'),
                onPressed: () async {
                  final subject = await promptText(context, title: 'Objet du litige', minLength: 4, maxLines: 1);
                  if (subject == null || !context.mounted) return;
                  final description = await promptText(context, title: 'Décrivez le problème', minLength: 10);
                  if (description == null || !context.mounted) return;
                  if (await runAction(context, () => api.post('/v1/requests/$id/disputes', {'subject': subject, 'description': description}))) await reload();
                },
              ),
          ]);
        },
      ),
    );
  }

  static Widget _row(String label, String value) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 3),
        child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
          SizedBox(width: 130, child: Text(label, style: const TextStyle(color: Colors.black54, fontSize: 13))),
          Expanded(child: Text(value, style: const TextStyle(fontWeight: FontWeight.w500))),
        ]),
      );

  static Widget _banner(Color color, String text) => Container(
        margin: const EdgeInsets.only(bottom: 14),
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(14)),
        child: Text(text),
      );

  static Future<(int, String)?> _askRating(BuildContext context) {
    var rating = 5;
    final comment = TextEditingController();
    return showDialog<(int, String)>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setDialog) => AlertDialog(
          title: const Text('Votre avis'),
          content: Column(mainAxisSize: MainAxisSize.min, children: [
            Row(mainAxisAlignment: MainAxisAlignment.center, children: List.generate(5, (i) => IconButton(
                  icon: Icon(i < rating ? Icons.star : Icons.star_border, color: sun),
                  onPressed: () => setDialog(() => rating = i + 1),
                ))),
            TextField(controller: comment, maxLines: 2, decoration: const InputDecoration(hintText: 'Votre commentaire')),
          ]),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Annuler')),
            FilledButton(onPressed: () => Navigator.pop(ctx, (rating, comment.text.trim())), child: const Text('Publier')),
          ],
        ),
      ),
    );
  }
}

class _Tracker extends StatelessWidget {
  const _Tracker({required this.status});

  final String status;

  @override
  Widget build(BuildContext context) {
    final blocked = status == 'rejected' || status == 'cancelled';
    final warn = status == 'info_requested';
    final current = _reached[status] ?? 1;
    return Card(
      color: const Color(0xFFFFFDF8),
      margin: const EdgeInsets.only(bottom: 14),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 8),
        child: Row(children: List.generate(_steps.length, (i) {
          Color color;
          IconData? icon;
          String label = requestStatus[_steps[i]]!.label;
          if ((blocked || warn) && i == 1) {
            color = blocked ? Colors.red.shade600 : Colors.amber.shade700;
            icon = blocked ? Icons.close : Icons.priority_high;
            label = requestStatus[status]!.label;
          } else if (i < current || status == 'closed' || ((blocked || warn) && i == 0)) {
            color = brand;
            icon = Icons.check;
          } else if (i == current && !blocked && !warn) {
            color = sun;
          } else {
            color = Colors.grey.shade300;
          }
          return Expanded(
            child: Column(children: [
              CircleAvatar(radius: 14, backgroundColor: color, child: icon == null ? Text('${i + 1}', style: const TextStyle(fontSize: 12, color: Colors.black87)) : Icon(icon, size: 16, color: Colors.white)),
              const SizedBox(height: 4),
              Text(label, textAlign: TextAlign.center, style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w600)),
            ]),
          );
        })),
      ),
    );
  }
}

class _PaymentTile extends ConsumerWidget {
  const _PaymentTile({required this.payment, required this.onChanged});

  final Json payment;
  final Future<void> Function() onChanged;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final p = payment;
    final status = asStr(p['status']) ?? '';
    final canDeclare = status == 'planned' || status == 'declared';
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(border: Border.all(color: Colors.grey.shade300), borderRadius: BorderRadius.circular(12)),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Row(children: [
          Expanded(child: Text('${p['note']} — ${fcfa(asInt(p['amountPlanned']))}', style: const TextStyle(fontWeight: FontWeight.w600))),
          StatusChip(paymentStatus, status),
        ]),
        const SizedBox(height: 4),
        Text('Échéance : ${fdate(asStr(p['dueDate']))} · Payé : ${fcfa(asInt(p['amountPaid']))} · Reste dû : ${fcfa(asInt(p['remaining']))}',
            style: const TextStyle(fontSize: 12, color: Colors.black54)),
        OnlinePaymentPanel(key: ValueKey('pay-${p['id']}'), payment: p, onChanged: onChanged),
        if (canDeclare)
          Align(
            alignment: Alignment.centerRight,
            child: TextButton(
                onPressed: () => _declare(context, ref),
                child: Text(status == 'declared' ? 'Mettre à jour ma déclaration' : 'Déjà payé autrement ? Déclarer')),
          ),
      ]),
    );
  }

  Future<void> _declare(BuildContext context, WidgetRef ref) async {
    var method = asStr(payment['method']) ?? 'mobile_money';
    if (!payMethods.containsKey(method)) method = 'mobile_money';
    final reference = TextEditingController(text: asStr(payment['reference']) ?? '');
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setDialog) => AlertDialog(
          title: const Text('Déclarer mon paiement'),
          content: Column(mainAxisSize: MainAxisSize.min, children: [
            DropdownButtonFormField<String>(
              initialValue: method,
              isExpanded: true,
              decoration: const InputDecoration(labelText: 'Moyen de paiement'),
              items: payMethods.entries.map((e) => DropdownMenuItem(value: e.key, child: Text(e.value, overflow: TextOverflow.ellipsis))).toList(),
              onChanged: (v) => setDialog(() => method = v ?? method),
            ),
            const SizedBox(height: 10),
            TextField(controller: reference, decoration: const InputDecoration(labelText: 'Référence de transaction')),
            const SizedBox(height: 8),
            const Text('L’administration valide le paiement après vérification.', style: TextStyle(fontSize: 12)),
          ]),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Annuler')),
            FilledButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('Déclarer')),
          ],
        ),
      ),
    );
    if (ok != true || !context.mounted) return;
    final api = ref.read(apiProvider);
    if (await runAction(context, () => api.multipart('/v1/payments/${payment['id']}/declare', {'method': method, 'reference': reference.text.trim()}))) {
      await onChanged();
    }
  }
}
