import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api.g.dart';
import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';
import 'provider_search_screen.dart';
import 'service_order_screen.dart';

/// Services communautaires : demandes envoyées (donateur, client) ou reçues (prestataire).
class ServicesScreen extends ConsumerWidget {
  const ServicesScreen({super.key, required this.user});

  final AppUser user;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isProvider = user.role == 'provider';
    return AsyncView<List<ApiOrderView>>(
      load: () async => asList(await ref.read(apiProvider).get('/v1/services/orders')).map(ApiOrderView.fromJson).toList(),
      builder: (context, rows, reload) => ListView(padding: const EdgeInsets.all(16), children: [
        if (!isProvider)
          FilledButton.icon(
            icon: const Icon(Icons.search),
            label: const Text('Trouver un prestataire'),
            onPressed: () async {
              await Navigator.push(context, MaterialPageRoute(builder: (_) => const ProviderSearchScreen()));
              await reload();
            },
          ),
        const SizedBox(height: 12),
        if (rows.isEmpty) EmptyState(isProvider ? 'Aucune demande de service reçue.' : 'Aucune demande de service.'),
        ...rows.where((r) => r.order != null).map((r) {
          final o = r.order!;
          final price = o.compensationMode == 'exchange' ? 'Échange de services' : fcfa(o.agreedPrice ?? o.proposedPrice);
          final other = r.iAmProvider ? 'Client : ${r.clientName ?? '—'}' : 'Prestataire : ${r.providerName ?? '—'}';
          return Card(
            margin: const EdgeInsets.only(bottom: 10),
            child: ListTile(
              title: Text(o.title ?? '', style: const TextStyle(fontWeight: FontWeight.w600)),
              subtitle: Text('$other\n$price · ${fdate(o.createdAt)}'),
              isThreeLine: true,
              trailing: StatusChip(orderStatus, o.status ?? ''),
              onTap: () async {
                await Navigator.push(context, MaterialPageRoute(builder: (_) => ServiceOrderScreen(id: o.id ?? 0)));
                await reload();
              },
            ),
          );
        }),
      ]),
    );
  }
}
