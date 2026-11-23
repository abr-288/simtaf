import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/pending.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';
import 'public_widgets.dart';

/// Pages intérieures du site public, reprises mot pour mot du site web (apps/saas, groupe « (public) »).
Widget? publicPage(String name) => switch (name) {
      'Social Kitchen' => const SocialKitchenPage(),
      'Social Network' => const SocialNetworkPage(),
      'Comment ça marche' => const HowItWorksPage(),
      'À propos' => const AboutPage(),
      'Partenaires' => const PartnersPage(),
      'Actualités' => const NewsPage(),
      'Contact' => const ContactPage(),
      'Aide' => const PublicHelpPage(),
      'Mentions légales' || 'Politique de confidentialité' || 'Conditions d’utilisation' => LegalPage(title: name),
      _ => null,
    };

/// Liste défilante d'une page du site, terminée par le pied de page.
class _Page extends StatelessWidget {
  const _Page({required this.hero, required this.children});

  final PageHero hero;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return ListView(children: [
      hero,
      Padding(padding: const EdgeInsets.fromLTRB(16, 20, 16, 12), child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: children)),
      PublicFooter(onNavigate: (p) => p == 'Accueil' ? Navigator.of(context).popUntil((r) => r.isFirst) : pushPublicPage(context, p)),
    ]);
  }
}

/* ───────── À propos ───────── */
class AboutPage extends ConsumerWidget {
  const AboutPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    const values = [
      ('Transparence', 'Chaque don est suivi de bout en bout : demande, mission, preuves de remise (photos, reçus, factures, attestations) et clôture.'),
      ('Proximité', 'Des prestataires locaux (commerçants, traiteurs, transporteurs, officiants…) réalisent les actions au plus près des bénéficiaires.'),
      ('Confiance', 'Authentification par téléphone, contrôle des accès par rôle, journal d’audit et protection des données personnelles des bénéficiaires.'),
    ];
    return _Page(
      hero: const PageHero(eyebrow: 'À propos', title: 'Une plateforme de solidarité de proximité',
          text: 'Menewa Naya connecte donateurs, bénéficiaires, prestataires locaux et partenaires pour transformer chaque intention de don en action concrète, documentée et vérifiable.'),
      children: [
        for (final v in values)
          PublicCard(accent: sun, top: true, child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(v.$1, style: h2), const SizedBox(height: 6), Text(v.$2, style: body)])),
        PublicCard(color: mint, child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Text('Menewa Social Kitchen', style: h2),
          const SizedBox(height: 6),
          const Text('Catalogue de dons (packs alimentaires, bélier, volailles, habits, fournitures, équipements), dons personnalisés, actions et visites, engagements récurrents et campagnes de financement participatif.', style: body),
          const SizedBox(height: 10),
          SiteButton('Découvrir', onPressed: () => pushPublicPage(context, 'Social Kitchen')),
        ])),
        PublicCard(color: cream, child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Text('Menewa Social Network', style: h2),
          const SizedBox(height: 6),
          const Text.rich(TextSpan(style: body, children: [
            TextSpan(text: 'Services communautaires : trouvez un prestataire, négociez le prix ou un échange de services, suivez la prestation et notez le résultat. Gagnez des tokens '),
            TextSpan(text: 'Mena', style: TextStyle(fontWeight: FontWeight.bold)),
            TextSpan(text: ' en rendant service.'),
          ])),
          const SizedBox(height: 10),
          SiteButton('Découvrir', yellow: true, onPressed: () => pushPublicPage(context, 'Social Network')),
        ])),
        const PublicCard(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text('Token Mena et gamification', style: h2),
          SizedBox(height: 6),
          Text('Les Mena récompensent la participation (missions validées, services rendus, dons clôturés). Des badges et niveaux d’engagement valorisent les membres. Les transferts et conversions monétaires sont désactivés tant que leur cadre juridique et réglementaire n’est pas validé.', style: body),
        ])),
      ],
    );
  }
}

/* ───────── Comment ça marche ───────── */
class HowItWorksPage extends ConsumerWidget {
  const HowItWorksPage({super.key});

  static const flows = [
    ('Donateur / Client', true, ['Créez votre compte avec votre numéro (OTP SMS).', 'Choisissez dans le catalogue, créez un don personnel, une action ou un engagement récurrent.', 'Payez (immédiat, préfinancement ou à chaque échéance) et suivez votre demande.', 'Consultez les preuves de remise validées, clôturez et notez.']),
    ('Prestataire', false, ['Inscrivez-vous et décrivez vos services et tarifs.', 'Recevez des missions proposées : acceptez ou refusez avec motif.', 'Suivez la mission, déposez les preuves (photos, reçus, factures).', 'Après validation administrative : paiement ou échange de services, tokens Mena et avis.']),
    ('Partenaire', true, ['Publiez des dons, actions solidaires ou campagnes de financement participatif.', 'L’équipe Menewa modère et publie.', 'Suivez les contributions reçues et l’avancement.']),
    ('Administration', false, ['Examine chaque demande : acceptation, refus motivé ou demande d’information.', 'Attribue les missions, contrôle les preuves et les paiements.', 'Gère litiges, statistiques, rôles, règles de tokens et journal d’audit.']),
  ];

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return _Page(
      hero: const PageHero(eyebrow: 'Fonctionnement', title: '3 étapes pour agir ensemble', text: 'Un parcours simple, sécurisé et traçable pour chaque profil.'),
      children: [
        for (final f in flows)
          PublicCard(accent: f.$2 ? brand : sun, child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(f.$1, style: h2),
            const SizedBox(height: 10),
            for (var i = 0; i < f.$3.length; i++)
              Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  CircleAvatar(radius: 12, backgroundColor: f.$2 ? brand : sun, child: Text('${i + 1}', style: const TextStyle(fontSize: 12, color: Colors.white, fontWeight: FontWeight.bold))),
                  const SizedBox(width: 10),
                  Expanded(child: Text(f.$3[i], style: const TextStyle(fontSize: 14))),
                ]),
              ),
          ])),
        Center(child: SiteButton('Créer mon compte', onPressed: () => openLogin(context, ref, register: true))),
      ],
    );
  }
}

/* ───────── Actualités ───────── */
class NewsPage extends StatelessWidget {
  const NewsPage({super.key});

  static const news = [
    ('Lancement de la plateforme (version MVP)', 'Dons du catalogue, demandes personnalisées, missions de prestataires et suivi des preuves sont disponibles.'),
    ('Token Mena : règles d’attribution', 'Les Mena récompensent les missions validées et les services rendus. Transferts et conversions restent désactivés en attente de validation juridique.'),
    ('Paiements mobiles : à venir', 'L’intégration des moyens de paiement adaptés au marché africain (Mobile Money, cartes) est prévue dans une prochaine version.'),
  ];

  @override
  Widget build(BuildContext context) {
    return _Page(
      hero: const PageHero(eyebrow: 'Actualités', title: 'Les nouvelles de Menewa'),
      children: [
        for (final n in news)
          PublicCard(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(n.$1, style: h2), const SizedBox(height: 4), Text(n.$2, style: body)])),
      ],
    );
  }
}

/* ───────── Aide ───────── */
/// Questions fréquentes, identiques à la page « Aide » du site.
const siteFaq = [
  ('Comment me connecter ?', 'Saisissez votre numéro de téléphone : un code à 6 chiffres (OTP) vous est envoyé par SMS. Il est valable 5 minutes (5 essais maximum).'),
  ('Comment faire un don ?', 'Depuis votre espace : Catalogue → panier → cible du don → mode de paiement. Vous pouvez aussi créer un don personnel, une action ou un engagement récurrent.'),
  ('Comment payer ?', 'Depuis la page de votre demande, cliquez sur « Payer par Mobile Money », choisissez l’opérateur (T-Money ou Flooz) et confirmez sur votre téléphone. Vous pouvez aussi payer autrement (virement, espèces) puis déclarer le paiement avec sa référence : l’administration le valide.'),
  ('Prestataire : quand suis-je payé ?', 'Démarrez la mission, déposez vos preuves puis terminez-la. Après validation des preuves, votre rémunération est validée puis versée sur votre numéro Mobile Money.'),
  ('Pourquoi ma demande a été refusée ?', 'Le motif est toujours indiqué sur la page de la demande. Vous pouvez contacter l’équipe pour plus d’informations.'),
  ('Quand un don est-il « réalisé » ?', 'Après remise effective, dépôt de preuves (photos, reçus, attestations) et contrôle administratif.'),
  ('Que sont les Mena ?', 'Des unités de récompense virtuelles gagnées par vos actions. Transfert et conversion monétaire sont désactivés jusqu’à validation juridique.'),
  ('Comment signaler un problème ?', 'Ouvrez un litige depuis la page de votre demande ; l’administration instruira le dossier.'),
];

class FaqList extends StatelessWidget {
  const FaqList({super.key});

  @override
  Widget build(BuildContext context) {
    return Column(children: [
      for (final q in siteFaq)
        PublicCard(
          child: Theme(
            data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
            child: ExpansionTile(
              tilePadding: EdgeInsets.zero,
              childrenPadding: const EdgeInsets.only(bottom: 4),
              expandedAlignment: Alignment.centerLeft,
              title: Text(q.$1, style: const TextStyle(fontWeight: FontWeight.w600, color: brandDark)),
              children: [Text(q.$2, style: body)],
            ),
          ),
        ),
    ]);
  }
}

class PublicHelpPage extends StatelessWidget {
  const PublicHelpPage({super.key});

  @override
  Widget build(BuildContext context) => const _Page(hero: PageHero(eyebrow: 'Aide', title: 'Questions fréquentes'), children: [FaqList()]);
}

/* ───────── Pages légales ───────── */
class LegalPage extends StatelessWidget {
  const LegalPage({super.key, required this.title});

  final String title;

  static const texts = {
    'Mentions légales': [
      'Éditeur : Menewa Naya — forme juridique, siège social et numéro d’enregistrement à compléter.',
      'Hébergeur : à compléter.',
      'Ces informations doivent être validées par l’organisation avant la mise en production.',
    ],
    'Politique de confidentialité': [
      'Nous collectons uniquement les données nécessaires : numéro de téléphone, nom, zone, historique des demandes.',
      'Les données personnelles des bénéficiaires ne sont visibles que par les personnes qui en ont besoin pour accomplir leur mission.',
      'Les actions critiques sont enregistrées dans un journal d’audit. Les éléments financiers sont archivés, jamais supprimés définitivement sans contrôle.',
      'Texte juridique définitif (durées de conservation, droits d’accès et de rectification, autorité de contrôle) à valider.',
    ],
    'Conditions d’utilisation': [
      'L’utilisation de la plateforme implique le respect des règles de la communauté et l’exactitude des informations fournies.',
      'Les tokens Mena sont des unités de récompense virtuelles sans valeur monétaire garantie ; leur transfert ou conversion est désactivé jusqu’à validation juridique.',
      'Conditions complètes à valider par un conseil juridique.',
    ],
  };

  @override
  Widget build(BuildContext context) {
    return _Page(hero: PageHero(title: title), children: [
      for (final t in texts[title] ?? const <String>[])
        Padding(padding: const EdgeInsets.only(bottom: 12), child: Text(t, style: const TextStyle(fontSize: 14, height: 1.5, color: Color(0xFF334155)))),
    ]);
  }
}

/* ───────── Contact ───────── */
class ContactPage extends ConsumerStatefulWidget {
  const ContactPage({super.key});

  @override
  ConsumerState<ContactPage> createState() => _ContactPageState();
}

class _ContactPageState extends ConsumerState<ContactPage> {
  final _name = TextEditingController();
  final _phone = TextEditingController();
  final _email = TextEditingController();
  final _subject = TextEditingController();
  final _message = TextEditingController();
  bool _sending = false;

  @override
  void initState() {
    super.initState();
    final u = ref.read(sessionProvider).user;
    if (u != null) {
      _name.text = u.name;
      _phone.text = u.phone;
    }
  }

  Future<void> _send() async {
    setState(() => _sending = true);
    final ok = await runAction(context, () => ref.read(apiProvider).post('/v1/contact', {
          'name': _name.text.trim(), 'phone': _phone.text.trim(), 'email': _email.text.trim(),
          'subject': _subject.text.trim(), 'message': _message.text.trim(),
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
    Widget info(IconData icon, String main, String hint) => PublicCard(
          child: Row(children: [
            Icon(icon, color: brand),
            const SizedBox(width: 12),
            Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(main), Text(hint, style: const TextStyle(fontSize: 12, color: Colors.black54))])),
          ]),
        );
    return _Page(
      hero: const PageHero(eyebrow: 'Contact', title: 'Écrivez-nous', text: 'Une question, un partenariat, un besoin ? Notre équipe vous répond.'),
      children: [
        PublicCard(child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
          TextField(controller: _name, decoration: const InputDecoration(labelText: 'Nom')),
          const SizedBox(height: 10),
          TextField(controller: _phone, keyboardType: TextInputType.phone, decoration: const InputDecoration(labelText: 'Téléphone')),
          const SizedBox(height: 10),
          TextField(controller: _email, keyboardType: TextInputType.emailAddress, decoration: const InputDecoration(labelText: 'E-mail')),
          const SizedBox(height: 10),
          TextField(controller: _subject, decoration: const InputDecoration(labelText: 'Sujet')),
          const SizedBox(height: 10),
          TextField(controller: _message, maxLines: 5, decoration: const InputDecoration(labelText: 'Message')),
          const SizedBox(height: 12),
          FilledButton(onPressed: _sending ? null : _send, child: Text(_sending ? 'Envoi…' : 'Envoyer le message')),
        ])),
        info(Icons.place_outlined, 'Lomé, Togo', 'Adresse à confirmer'),
        info(Icons.phone_outlined, '+228 90 00 00 00', 'Numéro à confirmer'),
        info(Icons.mail_outline, 'contact@menewa-naya.org', 'Adresse à confirmer'),
      ],
    );
  }
}

/* ───────── Partenaires ───────── */
class PartnersPage extends ConsumerWidget {
  const PartnersPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final api = ref.read(apiProvider);
    return AsyncView<List<Json>>(
      load: () async => asList(await api.get('/v1/public/partners')),
      builder: (context, partners, reload) => _Page(
        hero: const PageHero(eyebrow: 'Nos partenaires', title: 'Ensemble pour un plus grand impact',
            text: 'Associations, ONG, entreprises, institutions et mécènes partagent notre vision et publient dons, actions et campagnes sur la plateforme.'),
        children: [
          for (final p in partners)
            PublicCard(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(asStr(p['organization']) ?? asStr(p['name']) ?? '', style: h2),
              Text(asStr(p['zone']) ?? 'Togo', style: const TextStyle(color: Colors.black54)),
            ])),
          PublicCard(color: mint, child: Column(children: [
            const Text('Devenir partenaire', style: h2),
            const SizedBox(height: 6),
            const Text('Inscrivez votre organisation, publiez vos campagnes de financement participatif et suivez les contributions.', textAlign: TextAlign.center, style: body),
            const SizedBox(height: 12),
            SiteButton('S’inscrire comme partenaire', onPressed: () => openLogin(context, ref, register: true)),
          ])),
        ],
      ),
    );
  }
}

/* ───────── Social Kitchen ───────── */
class SocialKitchenPage extends ConsumerWidget {
  const SocialKitchenPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final api = ref.read(apiProvider);
    return AsyncView<(List<Json>, List<Json>)>(
      load: () async {
        final catalog = await api.get('/catalog') as Map;
        final camp = await api.get('/v1/public/listings').then(asList, onError: (_) => <Json>[]);
        return (asList(catalog['items']), camp);
      },
      builder: (context, data, reload) {
        final (items, camp) = data;
        return _Page(
          hero: const PageHero(eyebrow: 'Menewa Social Kitchen', title: 'Le don qui devient action',
              text: 'Choisissez un don prédéfini, définissez la quantité et la cible, ou créez votre propre action de bienfaisance. Tarifs provisoires, configurables par l’administration.'),
          children: [
            for (final i in items)
              PublicCard(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Row(children: [
                  CircleAvatar(radius: 24, backgroundColor: mint, child: Text(asStr(i['emoji']) ?? '🎁', style: const TextStyle(fontSize: 22))),
                  const SizedBox(width: 12),
                  Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                    Text(asStr(i['title']) ?? '', style: const TextStyle(fontWeight: FontWeight.bold, color: brandDark)),
                    Text(categories[asStr(i['category'])] ?? asStr(i['category']) ?? '', style: const TextStyle(fontSize: 12, color: Colors.black54)),
                  ])),
                ]),
                if (asStr(i['description']) != null) ...[const SizedBox(height: 8), Text(asStr(i['description'])!, style: body)],
                const SizedBox(height: 8),
                Text.rich(TextSpan(children: [
                  TextSpan(text: fcfa(asInt(i['unitPrice'])), style: const TextStyle(fontSize: 17, fontWeight: FontWeight.bold, color: brand)),
                  TextSpan(text: ' / ${i['unit']}', style: const TextStyle(fontSize: 12, color: Colors.black54)),
                ])),
              ])),
            if (camp.isNotEmpty) ...[
              const Padding(padding: EdgeInsets.only(top: 8, bottom: 10), child: Text('Campagnes et actions de nos partenaires', style: h2)),
              for (final c in camp)
                PublicCard(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                    decoration: BoxDecoration(color: Colors.amber.shade100, borderRadius: BorderRadius.circular(20)),
                    child: Text(listingTypes[asStr(c['type'])] ?? asStr(c['type']) ?? '', style: TextStyle(fontSize: 11, color: Colors.amber.shade900)),
                  ),
                  const SizedBox(height: 6),
                  Text(asStr(c['title']) ?? '', style: const TextStyle(fontWeight: FontWeight.bold, color: brandDark)),
                  Text(asStr(c['description']) ?? '', style: body),
                  if (asInt(c['goalAmount']) > 0) ...[
                    const SizedBox(height: 8),
                    LinearProgressIndicator(value: (asInt(c['raisedAmount']) / asInt(c['goalAmount'])).clamp(0.0, 1.0), color: brand, backgroundColor: Colors.grey.shade200),
                    const SizedBox(height: 4),
                    Text('${fcfa(asInt(c['raisedAmount']))} collectés sur ${fcfa(asInt(c['goalAmount']))}', style: const TextStyle(fontSize: 12, color: Colors.black54)),
                  ],
                ])),
            ],
            const SizedBox(height: 6),
            Center(child: SiteButton('Faire un don', onPressed: () => openLogin(context, ref, reason: 'Connectez-vous pour faire un don.', then: const PendingAction.tab('Catalogue')))),
          ],
        );
      },
    );
  }
}

/* ───────── Social Network ───────── */
class SocialNetworkPage extends ConsumerStatefulWidget {
  const SocialNetworkPage({super.key});

  @override
  ConsumerState<SocialNetworkPage> createState() => _SocialNetworkPageState();
}

class _SocialNetworkPageState extends ConsumerState<SocialNetworkPage> {
  final _q = TextEditingController();
  String? _cat;
  int _version = 0;

  @override
  Widget build(BuildContext context) {
    final api = ref.read(apiProvider);
    return AsyncView<List<Json>>(
      key: ValueKey(_version),
      load: () async => asList(await api.get('/v1/public/providers', query: {'q': _q.text.trim(), 'cat': _cat})),
      builder: (context, rows, reload) => _Page(
        hero: const PageHero(eyebrow: 'Menewa Social Network', title: 'Vos compétences, au service des autres',
            text: 'Trouvez un prestataire local et mettez-vous en relation. Connectez-vous pour demander un service et négocier le prix.'),
        children: [
          TextField(controller: _q, decoration: const InputDecoration(hintText: 'Rechercher (nom, zone, compétence)…'), onSubmitted: (_) => setState(() => _version++)),
          const SizedBox(height: 10),
          DropdownButtonFormField<String?>(
            initialValue: _cat,
            isExpanded: true,
            items: [
              const DropdownMenuItem(value: null, child: Text('Toutes les catégories')),
              ...providerCategories.entries.map((e) => DropdownMenuItem(value: e.key, child: Text(e.value))),
            ],
            onChanged: (v) => _cat = v,
          ),
          const SizedBox(height: 10),
          SiteButton('Rechercher', onPressed: () => setState(() => _version++)),
          const SizedBox(height: 16),
          for (final r in rows)
            PublicCard(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  Wrap(crossAxisAlignment: WrapCrossAlignment.center, spacing: 6, children: [
                    Text(asStr(r['name']) ?? 'Prestataire', style: const TextStyle(fontWeight: FontWeight.bold, color: brandDark)),
                    if (r['verified'] == true)
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 1),
                        decoration: BoxDecoration(color: Colors.green.shade100, borderRadius: BorderRadius.circular(20)),
                        child: Text('Vérifié', style: TextStyle(fontSize: 11, color: Colors.green.shade900)),
                      ),
                  ]),
                  Text('${providerCategories[asStr(r['category'])] ?? ''} · ${asStr(r['zone']) ?? '—'}', style: const TextStyle(fontSize: 12, color: Colors.black54)),
                ])),
                Column(crossAxisAlignment: CrossAxisAlignment.end, children: [
                  Text('★' * (r['rating'] is num ? (r['rating'] as num).round() : 0), style: const TextStyle(color: sun)),
                  Text('${r['reviews'] ?? 0} avis', style: const TextStyle(fontSize: 11, color: Colors.black54)),
                ]),
              ]),
              if (asStr(r['description']) != null) ...[const SizedBox(height: 6), Text(asStr(r['description'])!, style: body)],
              for (final s in asList(r['services'])) Text('• ${s['label']}', style: const TextStyle(fontSize: 12, color: Color(0xFF475569))),
            ])),
          if (rows.isEmpty) const Text('Aucun prestataire ne correspond à votre recherche.', style: TextStyle(color: Colors.black54)),
          const SizedBox(height: 10),
          Center(child: SiteButton('Demander un service', yellow: true,
              onPressed: () => openLogin(context, ref, reason: 'Connectez-vous pour demander un service.', then: const PendingAction.tab('Services')))),
        ],
      ),
    );
  }
}
