import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';

/// Questions fréquentes (mêmes réponses que la page « Aide » du site) et message à l'équipe.
const helpFaq = [
  ('Comment me connecter ?', 'Saisissez votre numéro de téléphone : un code à 6 chiffres vous est envoyé par SMS. Il est valable 5 minutes (5 essais maximum).'),
  ('Comment faire un don ?', 'Catalogue → panier → cible du don → échéancier. Vous pouvez aussi créer un don personnel, une action ou un engagement récurrent depuis le menu.'),
  ('Comment payer ?', 'Depuis la page de votre demande, touchez « Payer par Mobile Money », choisissez l’opérateur et confirmez sur votre téléphone. '
      'Vous pouvez aussi payer autrement (virement, espèces) puis déclarer le paiement avec sa référence : l’administration le valide.'),
  ('Pourquoi ma demande a été refusée ?', 'Le motif est toujours indiqué sur la page de la demande. Vous pouvez écrire à l’équipe ci-dessous pour plus d’informations.'),
  ('Quand un don est-il « réalisé » ?', 'Après remise effective, dépôt de preuves (photos, reçus, attestations) et contrôle administratif.'),
  ('Prestataire : quand suis-je payé ?', 'Démarrez la mission, déposez vos preuves puis terminez-la. Après validation des preuves, votre rémunération est validée puis versée sur votre numéro Mobile Money.'),
  ('Que sont les Mena ?', 'Des unités de récompense virtuelles gagnées par vos actions. Transfert et conversion monétaire sont désactivés jusqu’à validation juridique.'),
  ('Comment signaler un problème ?', 'Ouvrez un litige depuis la page de votre demande ou de votre mission ; l’administration instruira le dossier.'),
];

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
        child: Column(children: helpFaq.map((q) => ExpansionTile(
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
