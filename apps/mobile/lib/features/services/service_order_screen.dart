import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api.g.dart';
import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';

/// Demande de service : négociation (messages, contre-propositions), étapes et avis final.
class ServiceOrderScreen extends ConsumerWidget {
  const ServiceOrderScreen({super.key, required this.id});

  final int id;

  static const _open = ['requested', 'negotiating'];
  static const _talking = ['requested', 'negotiating', 'agreed', 'in_progress'];

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final api = ref.read(apiProvider);
    final me = ref.watch(sessionProvider).user;
    return Scaffold(
      appBar: AppBar(title: const Text('Demande de service')),
      body: AsyncView<ApiOrderDetail>(
        load: () async => ApiOrderDetail.fromJson(Json.from(await api.get('/v1/services/orders/$id') as Map)),
        builder: (context, d, reload) {
          final o = d.order;
          if (o == null) return const EmptyState('Demande introuvable.');
          final status = o.status ?? '';
          final isProv = d.iAmProvider;
          final money = o.compensationMode != 'exchange';

          Future<void> transition(String action, {int? rating, String? comment}) async {
            final body = <String, Object?>{'action': action, if (rating != null) 'rating': rating, if (comment != null) 'comment': comment};
            if (await runAction(context, () => api.post('/v1/services/orders/$id/transition', body))) await reload();
          }

          return ListView(padding: const EdgeInsets.all(16), children: [
            Row(children: [
              Expanded(child: Text(o.title ?? '', style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: brandDark))),
              StatusChip(orderStatus, status),
            ]),
            if ((o.description ?? '').isNotEmpty) Padding(padding: const EdgeInsets.only(top: 4), child: Text(o.description!)),
            section('Conditions', [
              Text(isProv ? 'Client : ${d.clientName ?? '—'}' : 'Prestataire : ${d.providerName ?? '—'}'),
              Text(money ? 'Rémunération : monétaire' : 'Rémunération : échange de services'),
              if (money) Text('Prix ${o.agreedPrice != null ? 'convenu' : 'en négociation'} : ${fcfa(o.agreedPrice ?? o.proposedPrice)}',
                  style: const TextStyle(fontWeight: FontWeight.bold, color: brand)),
              if (!money && (o.exchangeDescription ?? '').isNotEmpty) Text(o.exchangeDescription!),
              Text('Lieu : ${o.zone ?? '—'}'),
              ...d.payments.map((p) => Text('Suivi financier : ${paymentStatus[p.status]?.label ?? p.status} · payé ${fcfa(p.amountPaid)}',
                  style: TextStyle(fontSize: 12, color: Colors.grey.shade700))),
            ]),
            section('Échanges', [
              if (d.messages.isEmpty) const Text('Aucun message pour le moment.'),
              ...d.messages.where((m) => m.message != null).map((m) {
                final msg = m.message!;
                final mine = msg.authorId == me?.id;
                return Align(
                  alignment: mine ? Alignment.centerRight : Alignment.centerLeft,
                  child: Container(
                    margin: const EdgeInsets.symmetric(vertical: 4),
                    padding: const EdgeInsets.all(10),
                    constraints: const BoxConstraints(maxWidth: 300),
                    decoration: BoxDecoration(color: mine ? mint : Colors.grey.shade100, borderRadius: BorderRadius.circular(14)),
                    child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                      Text('${m.authorName ?? ''} · ${fdatetime(msg.createdAt)}', style: TextStyle(fontSize: 11, color: Colors.grey.shade700)),
                      if ((msg.body ?? '').isNotEmpty) Text(msg.body!),
                      if (msg.price != null) Text('Proposition : ${fcfa(msg.price)}', style: const TextStyle(fontWeight: FontWeight.bold, color: brand)),
                    ]),
                  ),
                );
              }),
              if (_talking.contains(status))
                Align(
                  alignment: Alignment.centerRight,
                  child: TextButton.icon(
                    icon: const Icon(Icons.chat_bubble_outline),
                    label: Text(money && _open.contains(status) ? 'Écrire / proposer un prix' : 'Écrire'),
                    onPressed: () async {
                      final input = await _askMessage(context, withPrice: money && _open.contains(status));
                      if (input == null || !context.mounted) return;
                      if (await runAction(context, () => api.post('/v1/services/orders/$id/messages', input))) await reload();
                    },
                  ),
                ),
            ]),
            const SizedBox(height: 8),
            if (_open.contains(status)) FilledButton(onPressed: () => transition('accept'), child: const Text('Accepter les termes')),
            if (_open.contains(status))
              TextButton(
                onPressed: () => transition('decline'),
                child: Text(isProv ? 'Décliner' : 'Annuler la demande', style: const TextStyle(color: Colors.red)),
              ),
            if (isProv && status == 'agreed') FilledButton(onPressed: () => transition('start'), child: const Text('Démarrer la prestation')),
            if (isProv && status == 'in_progress') FilledButton(onPressed: () => transition('done'), child: const Text('Marquer comme réalisée')),
            if (!isProv && status == 'done')
              FilledButton.icon(
                icon: const Icon(Icons.star_outline),
                label: const Text('Confirmer et noter'),
                onPressed: () async {
                  final review = await _askReview(context);
                  if (review == null) return;
                  await transition('confirm', rating: review.$1, comment: review.$2);
                },
              ),
            if (['confirmed', 'declined', 'cancelled'].contains(status))
              const Padding(padding: EdgeInsets.all(8), child: Text('Cette demande est terminée.', textAlign: TextAlign.center)),
          ]);
        },
      ),
    );
  }

  static Future<Map<String, Object?>?> _askMessage(BuildContext context, {required bool withPrice}) {
    final body = TextEditingController();
    final price = TextEditingController();
    return showDialog<Map<String, Object?>>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Message'),
        content: Column(mainAxisSize: MainAxisSize.min, children: [
          TextField(controller: body, maxLines: 3, autofocus: true, decoration: const InputDecoration(hintText: 'Votre message')),
          if (withPrice)
            TextField(controller: price, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Contre-proposition (FCFA), facultatif')),
        ]),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Annuler')),
          FilledButton(
            onPressed: () {
              final p = int.tryParse(price.text.replaceAll(RegExp(r'[^0-9]'), ''));
              if (body.text.trim().isEmpty && p == null) return;
              Navigator.pop(ctx, <String, Object?>{'body': body.text.trim(), if (p != null) 'price': p});
            },
            child: const Text('Envoyer'),
          ),
        ],
      ),
    );
  }

  static Future<(int, String)?> _askReview(BuildContext context) {
    final comment = TextEditingController();
    var rating = 5;
    return showDialog<(int, String)>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setState) => AlertDialog(
          title: const Text('Votre avis'),
          content: Column(mainAxisSize: MainAxisSize.min, children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(5, (i) => IconButton(
                    icon: Icon(i < rating ? Icons.star : Icons.star_border, color: sun),
                    onPressed: () => setState(() => rating = i + 1),
                  )),
            ),
            TextField(controller: comment, maxLines: 2, decoration: const InputDecoration(hintText: 'Commentaire')),
          ]),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Annuler')),
            FilledButton(onPressed: () => Navigator.pop(ctx, (rating, comment.text.trim())), child: const Text('Confirmer')),
          ],
        ),
      ),
    );
  }
}
