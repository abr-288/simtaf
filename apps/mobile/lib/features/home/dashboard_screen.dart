import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';
import '../missions/mission_detail_screen.dart';
import '../requests/request_detail_screen.dart';

/// Tableau de bord selon le profil (donateur/client, prestataire, partenaire, bénéficiaire).
class DashboardScreen extends ConsumerWidget {
  const DashboardScreen({super.key, required this.user});

  final AppUser user;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final api = ref.read(apiProvider);
    if (user.role == 'provider') {
      return AsyncView<Json>(
        load: () async => Json.from(await api.get('/v1/missions/dashboard') as Map),
        builder: (context, d, reload) => ListView(padding: const EdgeInsets.all(16), children: [
          _greeting(),
          GridView.count(crossAxisCount: 2, shrinkWrap: true, physics: const NeverScrollableScrollPhysics(), childAspectRatio: 1.8, children: [
            StatTile(label: 'Proposées', value: '${d['proposed']}', color: Colors.orange.shade800),
            StatTile(label: 'Acceptées', value: '${d['accepted']}', color: Colors.blue.shade800),
            StatTile(label: 'En cours', value: '${d['inProgress']}'),
            StatTile(label: 'Terminées', value: '${d['completed']}'),
          ]),
          const SizedBox(height: 12),
          SectionCard(
            title: 'Missions proposées',
            child: asList(d['proposals']).isEmpty
                ? const EmptyState('Aucune proposition en attente.')
                : Column(children: asList(d['proposals']).map((j) {
                    final m = Mission(j);
                    return ListTile(
                      contentPadding: EdgeInsets.zero,
                      title: Text(m.title),
                      subtitle: Text('Échéance ${fdate(m.dueDate)}'),
                      trailing: const Icon(Icons.chevron_right),
                      onTap: () async {
                        await Navigator.push(context, MaterialPageRoute(builder: (_) => MissionDetailScreen(id: m.id)));
                        await reload();
                      },
                    );
                  }).toList()),
          ),
        ]),
      );
    }
    if (user.role == 'partner') {
      return AsyncView<Json>(
        load: () async => Json.from(await api.get('/v1/listings/dashboard') as Map),
        builder: (context, d, reload) => ListView(padding: const EdgeInsets.all(16), children: [
          _greeting(),
          GridView.count(crossAxisCount: 2, shrinkWrap: true, physics: const NeverScrollableScrollPhysics(), childAspectRatio: 1.8, children: [
            StatTile(label: 'Publications', value: '${d['total']}'),
            StatTile(label: 'Publiées', value: '${d['published']}'),
            StatTile(label: 'En modération', value: '${d['pending']}', color: Colors.orange.shade800),
            StatTile(label: 'Fonds collectés', value: fcfa(asInt(d['raised']))),
          ]),
          const SizedBox(height: 12),
          const EmptyState('La création et le suivi des publications se font depuis l’espace web.'),
        ]),
      );
    }
    if (user.role == 'beneficiary') {
      return AsyncView<List<Json>>(
        load: () async => asList(await api.get('/v1/beneficiary/requests')),
        builder: (context, rows, reload) => ListView(padding: const EdgeInsets.all(16), children: [
          _greeting(),
          SectionCard(
            title: 'Dons reçus',
            child: rows.isEmpty
                ? const EmptyState('Aucun don pour le moment.')
                : Column(children: rows.map((r) => ListTile(
                      contentPadding: EdgeInsets.zero,
                      title: Text(asStr(r['title']) ?? ''),
                      subtitle: Text(fdate(asStr(r['createdAt']))),
                      trailing: StatusChip(requestStatus, asStr(r['status']) ?? ''),
                    )).toList()),
          ),
        ]),
      );
    }
    return AsyncView<List<Json>>(
      load: () async => Future.wait([
        api.get('/v1/requests/dashboard').then((v) => Json.from(v as Map)),
        api.get('/v1/rewards/me').then((v) => Json.from(v as Map)),
      ]),
      builder: (context, data, reload) {
        final d = data[0];
        final rewards = data[1];
        final level = rewards['level'] is Map ? Json.from(rewards['level'] as Map) : <String, dynamic>{};
        final next = rewards['next'] is Map ? Json.from(rewards['next'] as Map) : null;
        final recent = asList(d['recent']).map(GiftRequest.new).toList();
        return ListView(padding: const EdgeInsets.all(16), children: [
          _greeting(),
          GridView.count(crossAxisCount: 2, shrinkWrap: true, physics: const NeverScrollableScrollPhysics(), childAspectRatio: 1.8, children: [
            StatTile(label: 'En attente', value: '${d['pending']}', color: Colors.orange.shade800),
            StatTile(label: 'En cours', value: '${d['ongoing']}', color: Colors.blue.shade800),
            StatTile(label: 'Réalisées', value: '${d['done']}'),
            StatTile(label: 'Preuves', value: '${d['proofsAvailable']}'),
          ]),
          const SizedBox(height: 8),
          Container(
            padding: const EdgeInsets.all(16),
            margin: const EdgeInsets.only(bottom: 14),
            decoration: BoxDecoration(gradient: const LinearGradient(colors: [brand, Color(0xFF2F9A63)]), borderRadius: BorderRadius.circular(16)),
            child: Row(children: [
              const Text('🪙', style: TextStyle(fontSize: 34)),
              const SizedBox(width: 12),
              Expanded(
                child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  Text('${rewards['balance']} Mena · niveau ${level['label'] ?? ''}', style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16)),
                  Text(next == null ? 'Niveau maximal atteint' : 'Encore ${asInt(next['min']) - asInt(rewards['earned'])} Mena pour atteindre ${next['label']}',
                      style: const TextStyle(color: Colors.white70, fontSize: 12)),
                ]),
              ),
            ]),
          ),
          SectionCard(
            title: 'Mes dernières demandes',
            child: recent.isEmpty
                ? const EmptyState('Vous n’avez pas encore de demande. Parcourez le catalogue pour faire un don.')
                : Column(children: recent.map((r) => ListTile(
                      contentPadding: EdgeInsets.zero,
                      title: Text(r.title),
                      subtitle: Text('${r.ref} · ${fcfa(r.totalAmount)}'),
                      trailing: StatusChip(requestStatus, r.status),
                      onTap: () async {
                        await Navigator.push(context, MaterialPageRoute(builder: (_) => RequestDetailScreen(id: r.id)));
                        await reload();
                      },
                    )).toList()),
          ),
        ]);
      },
    );
  }

  Widget _greeting() => Padding(
        padding: const EdgeInsets.only(bottom: 12),
        child: Text('Bonjour ${user.name} 👋', style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: brandDark)),
      );
}
