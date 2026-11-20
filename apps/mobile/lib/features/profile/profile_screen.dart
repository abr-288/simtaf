import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';

/// Profil, préférences de notification, Token Mena et déconnexion.
class ProfileScreen extends ConsumerStatefulWidget {
  const ProfileScreen({super.key, required this.user});

  final AppUser user;

  @override
  ConsumerState<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends ConsumerState<ProfileScreen> {
  late final _name = TextEditingController(text: widget.user.name);
  late final _email = TextEditingController(text: widget.user.email ?? '');
  late final _zone = TextEditingController(text: widget.user.zone ?? '');
  late bool _inapp = widget.user.notifInapp;
  late bool _sms = widget.user.notifSms;
  late bool _emailPref = widget.user.notifEmail;
  bool _saving = false;

  Future<void> _save() async {
    setState(() => _saving = true);
    final u = widget.user;
    final ok = await runAction(context, () => ref.read(apiProvider).put('/auth/me', {
          'name': _name.text.trim(),
          'email': _email.text.trim(),
          'zone': _zone.text.trim(),
          'organization': u.organization,
          'bio': u.bio,
          'notifInapp': _inapp,
          'notifSms': _sms,
          'notifEmail': _emailPref,
        }));
    if (ok) await ref.read(sessionProvider.notifier).refreshUser();
    if (mounted) setState(() => _saving = false);
  }

  @override
  Widget build(BuildContext context) {
    final u = widget.user;
    return ListView(padding: const EdgeInsets.all(16), children: [
      Text(u.displayName, style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: brandDark)),
      Text('${roleLabels[u.role] ?? u.role} · ${u.phone}', style: const TextStyle(color: Colors.black54)),
      const SizedBox(height: 14),
      _RewardsCard(),
      SectionCard(
        title: 'Informations personnelles',
        child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
          TextField(controller: _name, decoration: const InputDecoration(labelText: 'Nom complet')),
          const SizedBox(height: 10),
          TextField(controller: _email, keyboardType: TextInputType.emailAddress, decoration: const InputDecoration(labelText: 'E-mail')),
          const SizedBox(height: 10),
          TextField(controller: _zone, decoration: const InputDecoration(labelText: 'Zone / ville')),
          const SizedBox(height: 10),
          SwitchListTile(contentPadding: EdgeInsets.zero, title: const Text('Notifications dans l’application'), value: _inapp, onChanged: (v) => setState(() => _inapp = v)),
          SwitchListTile(contentPadding: EdgeInsets.zero, title: const Text('SMS (événements importants)'), value: _sms, onChanged: (v) => setState(() => _sms = v)),
          SwitchListTile(contentPadding: EdgeInsets.zero, title: const Text('E-mail (canal à brancher)'), value: _emailPref, onChanged: (v) => setState(() => _emailPref = v)),
          const SizedBox(height: 8),
          FilledButton(onPressed: _saving ? null : _save, child: Text(_saving ? 'Enregistrement…' : 'Enregistrer')),
        ]),
      ),
      OutlinedButton.icon(
        icon: const Icon(Icons.logout),
        label: const Text('Se déconnecter'),
        onPressed: () => ref.read(sessionProvider.notifier).logout(),
      ),
    ]);
  }
}

class _RewardsCard extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return FutureBuilder<dynamic>(
      future: ref.read(apiProvider).get('/v1/rewards/me'),
      builder: (context, snapshot) {
        if (!snapshot.hasData || snapshot.data is! Map) return const SizedBox.shrink();
        final r = Json.from(snapshot.data as Map);
        final level = r['level'] is Map ? Json.from(r['level'] as Map) : <String, dynamic>{};
        final badges = asList(r['badges']).where((b) => b['earned'] == true).toList();
        return SectionCard(
          title: 'Token Mena',
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('${r['balance']} Mena · niveau ${level['label'] ?? ''}', style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: brand)),
            Text('Total gagné : ${r['earned']} Mena', style: const TextStyle(color: Colors.black54)),
            if (badges.isNotEmpty) ...[
              const SizedBox(height: 8),
              Wrap(spacing: 6, runSpacing: 6, children: badges.map((b) => Chip(label: Text('${b['emoji']} ${b['label']}'))).toList()),
            ],
            const SizedBox(height: 6),
            Text('Dernière opération : ${asList(r['transactions']).isEmpty ? '—' : fdate(asStr(asList(r['transactions']).first['createdAt']))}',
                style: const TextStyle(fontSize: 12, color: Colors.black54)),
          ]),
        );
      },
    );
  }
}
