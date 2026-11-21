import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';

import '../../core/api.g.dart';
import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';

/// Litige : décision, échanges avec l'équipe et l'autre partie, réponse avec photo jointe facultative.
class DisputeScreen extends ConsumerWidget {
  const DisputeScreen({super.key, required this.id});

  final int id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final api = ref.read(apiProvider);
    final me = ref.watch(sessionProvider).user;
    return Scaffold(
      appBar: AppBar(title: const Text('Litige')),
      body: AsyncView<ApiDisputeDetail>(
        load: () async => ApiDisputeDetail.fromJson(Json.from(await api.get('/v1/disputes/$id') as Map)),
        builder: (context, d, reload) {
          final dispute = d.dispute;
          if (dispute == null) return const EmptyState('Litige introuvable.');
          final open = dispute.status != 'closed' && dispute.status != 'resolved';
          return ListView(padding: const EdgeInsets.all(16), children: [
            Row(children: [
              Expanded(child: Text(dispute.subject ?? '', style: const TextStyle(fontSize: 19, fontWeight: FontWeight.bold, color: brandDark))),
              StatusChip(disputeStatus, dispute.status ?? ''),
            ]),
            Text('${dispute.ref ?? ''} · ouvert le ${fdate(dispute.createdAt)}', style: TextStyle(fontSize: 12, color: Colors.grey.shade700)),
            const SizedBox(height: 8),
            section('Détails', [
              if ((dispute.description ?? '').isNotEmpty) Text(dispute.description!),
              Text('Ouvert par : ${d.openedByName ?? '—'}'),
              if (d.againstName != null) Text('Concerne : ${d.againstName}'),
              Text('Suivi par : ${d.responsibleName ?? 'en attente d’attribution'}'),
              if ((dispute.decision ?? '').isNotEmpty) ...[
                const SizedBox(height: 6),
                Text('Décision : ${dispute.decision}', style: const TextStyle(fontWeight: FontWeight.bold, color: brand)),
              ],
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
                      if (msg.attachmentName != null && msg.id != null) ...[
                        const SizedBox(height: 6),
                        ProtectedImage(kind: 'dispute', id: msg.id!, height: 140),
                      ],
                    ]),
                  ),
                );
              }),
            ]),
            if (open)
              FilledButton.icon(
                icon: const Icon(Icons.reply),
                label: const Text('Répondre'),
                onPressed: () async {
                  if (await _reply(context, ref)) await reload();
                },
              ),
          ]);
        },
      ),
    );
  }

  Future<bool> _reply(BuildContext context, WidgetRef ref) async {
    final body = await promptText(context, title: 'Votre message', minLength: 2);
    if (body == null || !context.mounted) return false;
    final attach = await showDialog<ImageSource?>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Joindre une photo ?'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Non')),
          TextButton(onPressed: () => Navigator.pop(ctx, ImageSource.gallery), child: const Text('Galerie')),
          FilledButton(onPressed: () => Navigator.pop(ctx, ImageSource.camera), child: const Text('Appareil photo')),
        ],
      ),
    );
    XFile? file;
    if (attach != null) file = await ImagePicker().pickImage(source: attach, imageQuality: 80, maxWidth: 2000);
    if (!context.mounted) return false;
    final api = ref.read(apiProvider);
    return runAction(context, () => api.multipart('/v1/disputes/$id/messages', {'body': body},
        fileField: file == null ? null : 'attachment', filePath: file?.path));
  }
}
