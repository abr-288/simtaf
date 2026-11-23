import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/reference.g.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';
import '../missions/mission_detail_screen.dart';
import '../requests/request_detail_screen.dart';
import '../services/service_order_screen.dart';

/// Mes paiements (donateur, client) ou mes rémunérations (prestataire) : prévu, validé, payé, reste dû.
/// Le membre ne voit que ses propres opérations (contrôlé par l'API).
class MyPaymentsScreen extends ConsumerWidget {
  const MyPaymentsScreen({super.key, required this.user});

  final AppUser user;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final api = ref.read(apiProvider);
    final provider = user.role == 'provider';
    return AsyncView<List<Json>>(
      load: () async => asList(await api.get('/v1/payments/mine')),
      builder: (context, rows, reload) {
        if (rows.isEmpty) return ListView(padding: const EdgeInsets.all(16), children: const [EmptyState('Aucune opération financière.')]);
        final money = rows.where((p) => p['mode'] != 'exchange');
        final paid = money.fold<int>(0, (s, p) => s + asInt(p['amountPaid']));
        final due = money.where((p) => p['status'] != 'cancelled').fold<int>(0, (s, p) => s + asInt(p['remaining']));
        return RefreshIndicator(
          onRefresh: reload,
          child: ListView(padding: const EdgeInsets.all(16), children: [
            GridView.count(crossAxisCount: 2, shrinkWrap: true, physics: const NeverScrollableScrollPhysics(), childAspectRatio: 1.8, children: [
              StatTile(label: provider ? 'Reçu' : 'Payé', value: fcfa(paid)),
              StatTile(label: provider ? 'À recevoir' : 'Reste dû', value: fcfa(due), color: Colors.orange.shade800),
            ]),
            const SizedBox(height: 8),
            ...rows.map((p) => _PaymentCard(payment: p, provider: provider, onChanged: reload)),
          ]),
        );
      },
    );
  }
}

class _PaymentCard extends StatelessWidget {
  const _PaymentCard({required this.payment, required this.provider, required this.onChanged});

  final Json payment;
  final bool provider;
  final Future<void> Function() onChanged;

  Widget? _target() {
    final p = payment;
    if (p['missionId'] != null && provider) return MissionDetailScreen(id: asInt(p['missionId']));
    if (p['requestId'] != null && !provider) return RequestDetailScreen(id: asInt(p['requestId']));
    if (p['serviceOrderId'] != null) return ServiceOrderScreen(id: asInt(p['serviceOrderId']));
    return null;
  }

  @override
  Widget build(BuildContext context) {
    final p = payment;
    final exchange = p['mode'] == 'exchange';
    final target = _target();
    final validated = p['amountValidated'];
    final method = asStr(p['method']);
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: target == null
            ? null
            : () async {
                await Navigator.push(context, MaterialPageRoute(builder: (_) => target));
                await onChanged();
              },
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Row(children: [
              Icon(p['direction'] == 'out' ? Icons.south_west : Icons.north_east, size: 18, color: brand),
              const SizedBox(width: 6),
              Expanded(child: Text(asStr(p['note']) ?? asStr(p['kind']) ?? '', style: const TextStyle(fontWeight: FontWeight.w600))),
              StatusChip(paymentStatus, asStr(p['status']) ?? ''),
            ]),
            const SizedBox(height: 6),
            if (exchange)
              Text('Échange de services : ${p['exchangeDescription'] ?? ''} (${p['exchangeStatus'] ?? ''})', style: const TextStyle(fontSize: 13))
            else
              Text('Prévu ${fcfa(asInt(p['amountPlanned']))}'
                  '${validated != null ? ' · validé ${fcfa(asInt(validated))}' : ''}'
                  ' · payé ${fcfa(asInt(p['amountPaid']))} · reste ${fcfa(asInt(p['remaining']))}',
                  style: const TextStyle(fontSize: 13)),
            const SizedBox(height: 2),
            Text('Échéance ${fdate(asStr(p['dueDate']))}'
                '${method != null ? ' · ${referenceLabels['payMethod']![method] ?? method}' : ''}'
                '${p['reference'] != null ? ' · réf. ${p['reference']}' : ''}',
                style: const TextStyle(fontSize: 12, color: Colors.black54)),
            if (target != null)
              const Align(alignment: Alignment.centerRight, child: Icon(Icons.chevron_right, color: Colors.black38)),
          ]),
        ),
      ),
    );
  }
}
