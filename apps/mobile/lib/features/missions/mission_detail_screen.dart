import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';

import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';

/// Mission du prestataire : réponse (refus motivé), démarrage, preuves photo, fin de mission.
class MissionDetailScreen extends ConsumerWidget {
  const MissionDetailScreen({super.key, required this.id});

  final int id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final api = ref.read(apiProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Mission')),
      body: AsyncView<Json>(
        load: () async => Json.from(await api.get('/v1/missions/$id') as Map),
        builder: (context, d, reload) {
          final m = Mission(Json.from(d['mission'] as Map));
          final r = Json.from(d['request'] as Map);
          final ben = d['beneficiary'] is Map ? Json.from(d['beneficiary'] as Map) : null;
          final active = m.status == 'accepted' || m.status == 'in_progress';
          return ListView(padding: const EdgeInsets.all(16), children: [
            Row(children: [
              Expanded(child: Text(m.title, style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: brandDark))),
              StatusChip(missionStatus, m.status),
            ]),
            Text('Échéance : ${fdate(m.dueDate)}', style: const TextStyle(color: Colors.black54)),
            const SizedBox(height: 12),
            SectionCard(
              title: 'Informations nécessaires à l’exécution',
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text('Don concerné : ${r['title']}'),
                Text('Cible / zone : ${r['targetName'] ?? '—'}${r['targetZone'] != null ? ' · ${r['targetZone']}' : ''}'),
                Text('Rémunération : ${m.exchange ? 'Échange de services' : fcfa(m.providerFee)}', style: const TextStyle(fontWeight: FontWeight.bold, color: brand)),
                if (m.json['instructions'] != null) ...[const SizedBox(height: 8), Text(asStr(m.json['instructions'])!)],
                const SizedBox(height: 8),
                ...asList(d['items']).map((i) => Text('• ${i['quantity']} × ${i['label']}${i['target'] != null ? ' — ${i['target']}' : ''}')),
                const SizedBox(height: 8),
                if (ben != null)
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(color: Colors.amber.shade50, borderRadius: BorderRadius.circular(10)),
                    child: Text('Contact du bénéficiaire (confidentiel)\n${ben['fullName']} · ${ben['phone'] ?? '—'} · ${ben['address'] ?? ben['zone'] ?? '—'}'),
                  )
                else if (m.status == 'proposed')
                  const Text('Les coordonnées du bénéficiaire sont communiquées après acceptation de la mission.', style: TextStyle(fontSize: 12, color: Colors.black54)),
                if (m.json['refusalReason'] != null) Text('Motif de refus : ${m.json['refusalReason']}', style: const TextStyle(color: Colors.red)),
              ]),
            ),
            SectionCard(
              title: 'Preuves déposées',
              child: asList(d['proofs']).isEmpty
                  ? const EmptyState('Aucune preuve.')
                  : Column(children: asList(d['proofs']).map((p) => ListTile(
                        contentPadding: EdgeInsets.zero,
                        title: Text('${proofTypes[p['type']] ?? p['type']} — ${p['caption'] ?? p['fileName']}'),
                        subtitle: p['rejectionReason'] != null ? Text('Rejet : ${p['rejectionReason']}', style: const TextStyle(color: Colors.red)) : null,
                        trailing: StatusChip(proofStatus, asStr(p['status']) ?? ''),
                      )).toList()),
            ),
            if (m.status == 'proposed') ...[
              FilledButton(
                onPressed: () async {
                  if (await runAction(context, () => api.post('/v1/missions/$id/respond', {'decision': 'accept'}))) await reload();
                },
                child: const Text('Accepter la mission'),
              ),
              OutlinedButton(
                onPressed: () async {
                  final reason = await promptText(context, title: 'Motif du refus (obligatoire)', minLength: 5);
                  if (reason == null || !context.mounted) return;
                  if (await runAction(context, () => api.post('/v1/missions/$id/respond', {'decision': 'refuse', 'reason': reason}))) await reload();
                },
                child: const Text('Refuser', style: TextStyle(color: Colors.red)),
              ),
            ],
            if (m.status == 'accepted')
              FilledButton(
                onPressed: () async {
                  if (await runAction(context, () => api.post('/v1/missions/$id/start'))) await reload();
                },
                child: const Text('Démarrer la mission'),
              ),
            if (active) ...[
              const SizedBox(height: 8),
              FilledButton.tonalIcon(
                icon: const Icon(Icons.photo_camera_outlined),
                label: const Text('Déposer une preuve photo'),
                onPressed: () async {
                  if (await _uploadProof(context, ref)) await reload();
                },
              ),
              const SizedBox(height: 8),
              FilledButton(
                onPressed: () async {
                  if (await runAction(context, () => api.post('/v1/missions/$id/complete'))) await reload();
                },
                child: const Text('Marquer comme terminée'),
              ),
              const Padding(
                padding: EdgeInsets.only(top: 6),
                child: Text('Au moins une preuve est requise. L’administration la contrôle avant validation et rémunération. Respectez le consentement des personnes photographiées.',
                    style: TextStyle(fontSize: 12, color: Colors.black54)),
              ),
            ],
          ]);
        },
      ),
    );
  }

  Future<bool> _uploadProof(BuildContext context, WidgetRef ref) async {
    final source = await showModalBottomSheet<ImageSource>(
      context: context,
      builder: (ctx) => SafeArea(
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          ListTile(leading: const Icon(Icons.photo_camera), title: const Text('Prendre une photo'), onTap: () => Navigator.pop(ctx, ImageSource.camera)),
          ListTile(leading: const Icon(Icons.photo_library), title: const Text('Choisir dans la galerie'), onTap: () => Navigator.pop(ctx, ImageSource.gallery)),
        ]),
      ),
    );
    if (source == null) return false;
    final file = await ImagePicker().pickImage(source: source, imageQuality: 80, maxWidth: 2000);
    if (file == null || !context.mounted) return false;
    final caption = await promptText(context, title: 'Légende (facultatif)', maxLines: 1);
    if (!context.mounted) return false;
    final api = ref.read(apiProvider);
    return runAction(context, () => api.multipart('/v1/proofs', {
          'missionId': '$id',
          'type': 'photo',
          'caption': caption ?? '',
          'hidden': 'false',
        }, fileField: 'file', filePath: file.path));
  }
}
