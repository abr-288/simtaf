import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';
import '../missions/mission_detail_screen.dart';
import '../requests/request_detail_screen.dart';

/// Notifications in-app ; les liens vers une demande ou une mission ouvrent l'écran correspondant.
class NotificationsScreen extends ConsumerWidget {
  const NotificationsScreen({super.key});

  Widget? _target(String? link) {
    if (link == null) return null;
    final request = RegExp(r'^/espace/demandes/(\d+)').firstMatch(link);
    if (request != null) return RequestDetailScreen(id: int.parse(request.group(1)!));
    final mission = RegExp(r'^/espace/missions/(\d+)').firstMatch(link);
    if (mission != null) return MissionDetailScreen(id: int.parse(mission.group(1)!));
    return null;
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final api = ref.read(apiProvider);
    return AsyncView<List<AppNotification>>(
      load: () async => asList(await api.get('/v1/notifications')).map(AppNotification.new).toList(),
      builder: (context, rows, reload) => ListView(padding: const EdgeInsets.all(16), children: [
        Align(
          alignment: Alignment.centerRight,
          child: TextButton.icon(
            icon: const Icon(Icons.done_all),
            label: const Text('Tout marquer comme lu'),
            onPressed: () async {
              if (await runAction(context, () => api.post('/v1/notifications/read-all'))) await reload();
            },
          ),
        ),
        if (rows.isEmpty) const EmptyState('Aucune notification.'),
        ...rows.map((n) {
          final target = _target(n.link);
          return Card(
            color: n.read ? null : cream,
            margin: const EdgeInsets.only(bottom: 8),
            child: ListTile(
              leading: Icon(n.read ? Icons.notifications_none : Icons.notifications_active, color: n.read ? Colors.grey : sun),
              title: Text(n.title, style: TextStyle(fontWeight: n.read ? FontWeight.normal : FontWeight.w600)),
              subtitle: Text([if (n.body != null) n.body!, fdatetime(n.createdAt)].join('\n')),
              trailing: target == null ? null : const Icon(Icons.chevron_right),
              onTap: target == null ? null : () => Navigator.push(context, MaterialPageRoute(builder: (_) => target)),
            ),
          );
        }),
      ]),
    );
  }
}
