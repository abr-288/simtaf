import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';
import 'mission_detail_screen.dart';

/// Missions attribuées au prestataire connecté.
class MissionsScreen extends ConsumerWidget {
  const MissionsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return AsyncView<List<Mission>>(
      load: () async => asList(await ref.read(apiProvider).get('/v1/missions/mine')).map(Mission.new).toList(),
      builder: (context, rows, reload) => ListView(padding: const EdgeInsets.all(16), children: [
        if (rows.isEmpty) const EmptyState('Aucune mission pour le moment.'),
        ...rows.map((m) => Card(
              margin: const EdgeInsets.only(bottom: 10),
              child: ListTile(
                title: Text(m.title, style: const TextStyle(fontWeight: FontWeight.w600)),
                subtitle: Text('Échéance ${fdate(m.dueDate)}${m.late ? ' · EN RETARD' : ''}\n'
                    '${m.exchange ? 'Échange de services' : fcfa(m.providerFee)}${m.validated ? ' · validée' : ''}'),
                isThreeLine: true,
                trailing: StatusChip(missionStatus, m.status),
                onTap: () async {
                  await Navigator.push(context, MaterialPageRoute(builder: (_) => MissionDetailScreen(id: m.id)));
                  await reload();
                },
              ),
            )),
      ]),
    );
  }
}
