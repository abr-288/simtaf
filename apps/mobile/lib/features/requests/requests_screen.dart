import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';
import 'request_detail_screen.dart';

/// Historique et suivi des demandes du donateur / client.
class RequestsScreen extends ConsumerWidget {
  const RequestsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return AsyncView<List<GiftRequest>>(
      load: () async => asList(await ref.read(apiProvider).get('/v1/requests')).map(GiftRequest.new).toList(),
      builder: (context, rows, reload) => ListView(padding: const EdgeInsets.all(16), children: [
        if (rows.isEmpty) const EmptyState('Aucune demande. Parcourez le catalogue pour faire un don.'),
        ...rows.map((r) => Card(
              margin: const EdgeInsets.only(bottom: 10),
              child: ListTile(
                title: Text(r.title, style: const TextStyle(fontWeight: FontWeight.w600)),
                subtitle: Text('${r.ref} · ${kindLabels[r.kind] ?? r.kind}\n${fcfa(r.totalAmount)} · ${fdate(r.createdAt)}'),
                isThreeLine: true,
                trailing: StatusChip(requestStatus, r.status),
                onTap: () async {
                  await Navigator.push(context, MaterialPageRoute(builder: (_) => RequestDetailScreen(id: r.id)));
                  await reload();
                },
              ),
            )),
      ]),
    );
  }
}
