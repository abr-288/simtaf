import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';
import '../public/public_pages.dart';

/// Questions fréquentes (mêmes textes que la page « Aide » du site, voir [siteFaq]) et message à l'équipe.
class HelpScreen extends ConsumerStatefulWidget {
  const HelpScreen({super.key, required this.user});

  final AppUser user;

  @override
  ConsumerState<HelpScreen> createState() => _HelpScreenState();
}

class _HelpScreenState extends ConsumerState<HelpScreen> {
  final _subject = TextEditingController();
  final _message = TextEditingController();
  bool _sending = false;

  Future<void> _send() async {
    if (_subject.text.trim().length < 3 || _message.text.trim().length < 10) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Indiquez un objet et un message (10 caractères minimum).')));
      return;
    }
    setState(() => _sending = true);
    final ok = await runAction(context, () => ref.read(apiProvider).post('/v1/contact', {
          'name': widget.user.name.isEmpty ? widget.user.phone : widget.user.name,
          'phone': widget.user.phone,
          'subject': _subject.text.trim(),
          'message': _message.text.trim(),
        }));
    if (!mounted) return;
    setState(() => _sending = false);
    if (ok) {
      _subject.clear();
      _message.clear();
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListView(padding: const EdgeInsets.all(16), children: [
      SectionCard(
        title: 'Questions fréquentes',
        child: Column(children: siteFaq.map((q) => ExpansionTile(
              tilePadding: EdgeInsets.zero,
              childrenPadding: const EdgeInsets.only(bottom: 12),
              expandedAlignment: Alignment.centerLeft,
              title: Text(q.$1, style: const TextStyle(fontWeight: FontWeight.w600, color: brandDark)),
              children: [Text(q.$2, style: const TextStyle(fontSize: 13))],
            )).toList()),
      ),
      SectionCard(
        title: 'Écrire à l’équipe',
        child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
          TextField(controller: _subject, decoration: const InputDecoration(labelText: 'Objet')),
          const SizedBox(height: 10),
          TextField(controller: _message, maxLines: 4, decoration: const InputDecoration(labelText: 'Votre message')),
          const SizedBox(height: 6),
          Text('Réponse au ${widget.user.phone}.', style: const TextStyle(fontSize: 12, color: Colors.black54)),
          const SizedBox(height: 10),
          FilledButton(onPressed: _sending ? null : _send, child: Text(_sending ? 'Envoi…' : 'Envoyer')),
        ]),
      ),
    ]);
  }
}
