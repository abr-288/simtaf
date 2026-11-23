import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/pending.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';
import 'public_widgets.dart';

/// Accueil du site public, section par section et mot pour mot comme la page d'accueil du web.
class PublicHomeScreen extends ConsumerWidget {
  const PublicHomeScreen({super.key, required this.onNavigate});

  /// Ouvre une page du site (« Social Kitchen », « À propos »…).
  final void Function(String page) onNavigate;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final api = ref.read(apiProvider);
    return AsyncView<(Json, List<Json>)>(
      load: () async {
        final settings = await api.get('/v1/settings/public').then((v) => Json.from(v as Map), onError: (_) => <String, dynamic>{});
        final items = await api.get('/catalog').then((v) => asList((v as Map)['items']), onError: (_) => <Json>[]);
        ref.invalidate(publicPhotosProvider);
        return (settings, items);
      },
      builder: (context, data, reload) {
        final (settings, items) = data;
        final stats = settings['public.stats'] is Map ? Json.from(settings['public.stats'] as Map) : <String, dynamic>{};
        String stat(String k) => asStr(stats[k]) ?? '—';
        return ListView(children: [
          _Hero(onAbout: () => onNavigate('À propos')),
          Padding(
            padding: const EdgeInsets.fromLTRB(14, 16, 14, 0),
            child: Column(children: [
              _Universe(
                slot: 'kitchen', colors: const [Color(0xFFE3F1E8), Color(0xFFF3FAF5)], iconBg: brand, iconColor: sun, icon: Icons.volunteer_activism,
                title: 'Social Kitchen', tagline: 'Le don qui devient action.',
                text: 'Faites un don, financez un besoin, contribuez à des projets solidaires et suivez l’impact de votre engagement.',
                button: SiteButton('Faire un don', onPressed: () => onNavigate('Social Kitchen')),
              ),
              _Universe(
                slot: 'network', colors: const [Color(0xFFFFF3D6), Color(0xFFFFFAF0)], iconBg: sun, iconColor: brandDark, icon: Icons.groups,
                title: 'Social Network', tagline: 'Vos compétences,\nau service des autres.',
                text: 'Proposez vos compétences, trouvez une personne ou une organisation pour répondre à un besoin.',
                button: SiteButton('Rejoindre le réseau', yellow: true, onPressed: () => onNavigate('Social Network')),
              ),
              // Chiffres
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(color: const Color(0xFFE9F3ED), borderRadius: BorderRadius.circular(16)),
                child: Column(children: [
                  GridView.count(
                    crossAxisCount: 2, shrinkWrap: true, physics: const NeverScrollableScrollPhysics(), childAspectRatio: 2.4,
                    children: [
                      _Stat(Icons.groups_outlined, stat('beneficiaries'), 'Bénéficiaires soutenus'),
                      _Stat(Icons.handshake_outlined, stat('partners'), 'Partenaires engagés'),
                      _Stat(Icons.favorite_border, stat('gifts'), 'Dons et contributions'),
                      _Stat(Icons.hub_outlined, stat('members'), 'Membres sur le réseau'),
                    ],
                  ),
                  const _Hand('Des actions concrètes\npour un impact durable.', color: brandDark),
                ]),
              ),
              const SizedBox(height: 22),
              // Partenaires
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                const Text('Nos partenaires', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: brandDark)),
                const Text('Ensemble pour un plus grand impact', style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: brandDark)),
                const SizedBox(height: 8),
                const Text('Nous collaborons avec des associations, ONG, entreprises, institutions et mécènes qui partagent notre vision.', style: body),
                const SizedBox(height: 12),
                SiteButton('Découvrir nos partenaires', onPressed: () => onNavigate('Partenaires')),
                const SizedBox(height: 16),
                const Wrap(alignment: WrapAlignment.spaceAround, runSpacing: 14, children: [
                  _Partner(Icons.eco_outlined, 'ONG\nSOLIDAIRE'),
                  _Partner(Icons.public, 'FONDATION\nAFRIQUE'),
                  _Partner(Icons.apartment, 'Entreprises\nengagées'),
                  _Partner(Icons.account_balance_outlined, 'Gouvernement\ndu Togo'),
                  _Partner(Icons.groups_outlined, 'Diaspora\nsolidaire'),
                ]),
              ]),
              const SizedBox(height: 22),
              ImpactSimulator(items: items),
              const SizedBox(height: 22),
              _HowItWorks(onNavigate: onNavigate),
            ]),
          ),
          const _Testimonials(),
          PublicFooter(onNavigate: onNavigate),
        ]);
      },
    );
  }
}

/// Texte « manuscrit » du site (police Caveat sur le web), avec soulignement jaune.
class _Hand extends StatelessWidget {
  const _Hand(this.text, {this.color = Colors.white, this.size = 22});

  final String text;
  final Color color;
  final double size;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 8),
      child: Column(children: [
        Text(text, textAlign: TextAlign.center,
            style: TextStyle(fontSize: size, fontStyle: FontStyle.italic, fontWeight: FontWeight.w600, color: color, height: 1.1)),
        Container(margin: const EdgeInsets.only(top: 4), height: 3, width: 130, decoration: BoxDecoration(color: sun, borderRadius: BorderRadius.circular(2))),
      ]),
    );
  }
}

class _Hero extends StatelessWidget {
  const _Hero({required this.onAbout});

  final VoidCallback onAbout;

  @override
  Widget build(BuildContext context) {
    return Stack(children: [
      const Positioned.fill(child: SlotPhoto('hero', alignment: Alignment(0.2, -0.5))),
      Positioned.fill(
        child: DecoratedBox(
          decoration: BoxDecoration(gradient: LinearGradient(colors: [const Color(0xFFFFFDF8), const Color(0xFFFFFDF8).withValues(alpha: 0.85), const Color(0xFFFFFDF8).withValues(alpha: 0.15)])),
        ),
      ),
      Padding(
        padding: const EdgeInsets.fromLTRB(20, 30, 20, 34),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Text('SOLIDARITÉ  •  PARTAGE  •  IMPACT', style: TextStyle(fontSize: 10, letterSpacing: 2, fontWeight: FontWeight.w600, color: brandDark)),
          const SizedBox(height: 16),
          const Text('Ensemble,\non fait la différence.', style: TextStyle(fontSize: 36, fontWeight: FontWeight.w800, color: Color(0xFF1C3A2C), height: 1.05)),
          const SizedBox(height: 14),
          const Text.rich(TextSpan(style: TextStyle(fontSize: 14, height: 1.45, color: Color(0xFF1F2937)), children: [
            TextSpan(text: 'Menewa est une '),
            TextSpan(text: 'plateforme solidaire', style: TextStyle(fontWeight: FontWeight.bold)),
            TextSpan(text: ' qui connecte les personnes, les compétences et les ressources pour répondre aux besoins concrets des communautés du Togo, d’Afrique et de la diaspora.'),
          ])),
          const SizedBox(height: 18),
          SiteButton('Découvrir Menewa', yellow: true, onPressed: onAbout),
          const _Hand('Plus de solidarité, plus d’opportunités, plus d’impact.', color: brand, size: 18),
        ]),
      ),
    ]);
  }
}

class _Universe extends StatelessWidget {
  const _Universe({required this.slot, required this.colors, required this.iconBg, required this.iconColor, required this.icon,
      required this.title, required this.tagline, required this.text, required this.button});

  final String slot;
  final List<Color> colors;
  final Color iconBg, iconColor;
  final IconData icon;
  final String title, tagline, text;
  final Widget button;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 14),
      clipBehavior: Clip.antiAlias,
      decoration: BoxDecoration(borderRadius: BorderRadius.circular(16), gradient: LinearGradient(colors: colors)),
      child: Stack(children: [
        Positioned(top: 0, bottom: 0, right: 0, width: 130, child: SlotPhoto(slot)),
        Padding(
          padding: const EdgeInsets.all(18),
          child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
            CircleAvatar(radius: 30, backgroundColor: iconBg, child: Icon(icon, color: iconColor, size: 30)),
            const SizedBox(width: 14),
            Expanded(
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text.rich(TextSpan(style: const TextStyle(fontSize: 23, fontWeight: FontWeight.bold, color: brandDark, height: 1.1), children: [
                  const TextSpan(text: 'Menewa\n'),
                  TextSpan(text: title, style: const TextStyle(color: brand)),
                ])),
                const SizedBox(height: 8),
                Text(tagline, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.bold, fontStyle: FontStyle.italic, color: brand)),
                const SizedBox(height: 8),
                Text(text, style: const TextStyle(fontSize: 13, height: 1.4)),
                const SizedBox(height: 12),
                button,
              ]),
            ),
          ]),
        ),
      ]),
    );
  }
}

class _Stat extends StatelessWidget {
  const _Stat(this.icon, this.value, this.label);

  final IconData icon;
  final String value, label;

  @override
  Widget build(BuildContext context) {
    return Row(children: [
      Icon(icon, size: 32, color: brand),
      const SizedBox(width: 8),
      Expanded(child: Column(mainAxisAlignment: MainAxisAlignment.center, crossAxisAlignment: CrossAxisAlignment.start, children: [
        Text(value, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: brandDark)),
        Text(label, style: const TextStyle(fontSize: 11, color: Color(0xFF475569))),
      ])),
    ]);
  }
}

class _Partner extends StatelessWidget {
  const _Partner(this.icon, this.label);

  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 100,
      child: Column(children: [
        Icon(icon, size: 34, color: Colors.blueGrey.shade400),
        const SizedBox(height: 6),
        Text(label, textAlign: TextAlign.center, style: TextStyle(fontSize: 11, fontWeight: FontWeight.w500, color: Colors.blueGrey.shade500, height: 1.2)),
      ]),
    );
  }
}

class _HowItWorks extends StatelessWidget {
  const _HowItWorks({required this.onNavigate});

  final void Function(String page) onNavigate;

  @override
  Widget build(BuildContext context) {
    Widget column(bool green, IconData icon, String title, String sub, List<(IconData, String)> steps, String cta, String page) {
      return Padding(
        padding: const EdgeInsets.only(bottom: 26),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(children: [
            CircleAvatar(radius: 30, backgroundColor: green ? brand : sun, child: Icon(icon, color: green ? sun : brandDark, size: 28)),
            const SizedBox(width: 14),
            Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(title, style: const TextStyle(fontSize: 19, fontWeight: FontWeight.bold, color: brandDark)),
              Text(sub, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w500, color: brandDark)),
            ])),
          ]),
          const SizedBox(height: 16),
          Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
            for (var i = 0; i < steps.length; i++)
              Expanded(
                child: Column(children: [
                  Stack(clipBehavior: Clip.none, children: [
                    CircleAvatar(radius: 26, backgroundColor: green ? const Color(0xFFE3F1E8) : const Color(0xFFFFF0CF),
                        child: Icon(steps[i].$1, color: green ? brand : const Color(0xFFC98A12), size: 24)),
                    Positioned(left: -10, top: 0, child: CircleAvatar(radius: 11, backgroundColor: green ? brand : sun,
                        child: Text('${i + 1}', style: const TextStyle(fontSize: 11, color: Colors.white, fontWeight: FontWeight.bold)))),
                  ]),
                  const SizedBox(height: 8),
                  Text(steps[i].$2, textAlign: TextAlign.center, style: const TextStyle(fontSize: 11.5, color: Color(0xFF334155), height: 1.25)),
                ]),
              ),
          ]),
          const SizedBox(height: 16),
          SiteButton(cta, yellow: !green, onPressed: () => onNavigate(page)),
        ]),
      );
    }

    return Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      const Text('COMMENT ÇA MARCHE ?', style: TextStyle(fontSize: 10, letterSpacing: 1.2, fontWeight: FontWeight.w600, color: brandDark)),
      const Text('3 étapes pour agir ensemble', style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: brandDark)),
      TextButton(onPressed: () => onNavigate('Comment ça marche'), child: const Text('Voir tous les services →')),
      const SizedBox(height: 8),
      column(true, Icons.volunteer_activism, 'Menewa Social Kitchen', 'Donner, financer, suivre', const [
        (Icons.favorite_border, 'Choisissez un besoin\nou faites un don'),
        (Icons.credit_card, 'Sélectionnez votre\nmode de contribution'),
        (Icons.trending_up, 'Suivez l’impact\nde votre action'),
      ], 'Accéder à la Social Kitchen', 'Social Kitchen'),
      column(false, Icons.groups, 'Menewa Social Network', 'Proposer, chercher, collaborer', const [
        (Icons.person_outline, 'Créez votre profil\n(compétence ou besoin)'),
        (Icons.search, 'Trouvez la bonne\npersonne ou ressource'),
        (Icons.hub_outlined, 'Échangez, collaborez,\nfaites grandir l’impact'),
      ], 'Accéder au Social Network', 'Social Network'),
    ]);
  }
}

class _Testimonials extends ConsumerWidget {
  const _Testimonials();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final hasWoman = ref.watch(publicPhotosProvider).valueOrNull?['woman'] != null;
    Widget quote(String text, String who, String where) => Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Text('“', style: TextStyle(fontSize: 46, height: 1, color: sun, fontFamily: 'serif')),
          const SizedBox(width: 8),
          Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(text, style: const TextStyle(fontSize: 13, height: 1.45, color: Colors.white)),
            const SizedBox(height: 8),
            Text(who, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: Colors.white)),
            Text(where, style: const TextStyle(fontSize: 11.5, color: Colors.white70)),
          ])),
        ]);
    return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
      Container(
        color: brand,
        padding: const EdgeInsets.all(20),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Text('Ils nous font confiance', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white)),
          const SizedBox(height: 10),
          quote('Grâce à Menewa, nous avons pu financer la cantine de notre école. C’est un vrai soulagement pour les enfants et pour toute la communauté.',
              'Koffi A.', 'Association Éducation pour Tous – Lomé'),
          const Divider(color: Colors.white24, height: 30),
          Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Container(
              width: 56, height: 56,
              clipBehavior: Clip.antiAlias,
              decoration: BoxDecoration(shape: BoxShape.circle, color: brandDark, border: Border.all(color: sun, width: 2)),
              child: hasWoman ? const SlotPhoto('woman') : const Center(child: Text('A', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: sun))),
            ),
            const SizedBox(width: 10),
            Expanded(child: quote('J’ai trouvé des missions qui correspondent à mes compétences. Menewa m’a permis de mettre mon savoir-faire au service des autres.',
                'Aminata B.', 'Membre du réseau – Diaspora')),
          ]),
        ]),
      ),
      Container(color: sun, padding: const EdgeInsets.symmetric(vertical: 22), child: const _Hand('La solidarité\nn’a pas de frontières.', color: brandDark, size: 24)),
    ]);
  }
}

/* ───────── Simulateur d'impact ───────── */

const _min = 1000, _max = 500000, _step = 1000, _months = 12;
const _presets = [5000, 10000, 25000, 50000, 100000, 250000];

/// Simulateur d'impact (mêmes règles que le site) : dons finançables pour un montant donné, ponctuel ou mensuel.
class ImpactSimulator extends ConsumerStatefulWidget {
  const ImpactSimulator({super.key, required this.items});

  final List<Json> items;

  @override
  ConsumerState<ImpactSimulator> createState() => _ImpactSimulatorState();
}

class _ImpactSimulatorState extends ConsumerState<ImpactSimulator> {
  int _amount = 50000;
  bool _monthly = false;
  int? _selected;
  final _other = TextEditingController();

  String _fmt(int n) => fcfa(n).replaceAll(' FCFA', '');

  @override
  Widget build(BuildContext context) {
    final total = _amount * (_monthly ? _months : 1);
    final rows = widget.items.where((i) => asInt(i['unitPrice']) > 0).map((it) {
      final price = asInt(it['unitPrice']);
      return (it: it, units: total ~/ price, monthlyUnits: _amount ~/ price, partial: (total / price).clamp(0.0, 1.0).toDouble(), missing: price > total ? price - total : 0);
    }).toList();
    final maxUnits = rows.fold<int>(1, (m, r) => r.units > m ? r.units : m);
    final sel = rows.where((r) => asInt(r.it['id']) == _selected && r.units > 0).firstOrNull;
    final selQty = sel == null ? 0 : (_monthly ? sel.monthlyUnits : sel.units);

    void finance() {
      if (sel == null) {
        // Don libre : catalogue de l'espace membre.
        openLogin(context, ref, reason: 'Connectez-vous pour faire un don.', then: const PendingAction.tab('Catalogue'));
        return;
      }
      final qty = selQty > 500 ? 500 : selQty;
      openLogin(context, ref,
          reason: 'Connectez-vous pour financer le don choisi dans le simulateur : il sera ajouté à votre panier. Pas encore de compte ? Inscrivez-vous, le don vous attendra aussi.',
          then: PendingAction.cart(itemId: asInt(sel.it['id']), quantity: qty, monthly: _monthly));
    }

    final qtyShown = selQty > 500 ? 500 : selQty;
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(color: const Color(0xFFE9F3ED), borderRadius: BorderRadius.circular(24)),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        const Text('CONCRÈTEMENT', style: TextStyle(fontSize: 10, letterSpacing: 2, fontWeight: FontWeight.w600, color: brandDark)),
        const SizedBox(height: 6),
        const Text.rich(TextSpan(style: TextStyle(fontSize: 26, fontWeight: FontWeight.w800, color: brandDark, height: 1.1), children: [
          TextSpan(text: 'Déplacez le curseur, '),
          TextSpan(text: 'voyez ce que ça change.', style: TextStyle(color: brand, backgroundColor: Color(0x8CF6B53B))),
        ])),
        const SizedBox(height: 14),
        Wrap(crossAxisAlignment: WrapCrossAlignment.end, spacing: 8, children: [
          Text(_fmt(_amount), style: const TextStyle(fontSize: 46, fontWeight: FontWeight.w800, color: brandDark, height: 1)),
          Padding(
            padding: const EdgeInsets.only(bottom: 6),
            child: Text.rich(TextSpan(children: [
              const TextSpan(text: 'FCFA ', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: brand)),
              TextSpan(text: _monthly ? 'par mois' : 'en une fois', style: const TextStyle(fontSize: 13, color: Colors.black54)),
            ])),
          ),
        ]),
        if (_monthly) Text('= ${_fmt(total)} FCFA sur $_months mois', style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: brand)),
        Slider(
          value: _amount.clamp(_min, _max).toDouble(),
          min: _min.toDouble(), max: _max.toDouble(), divisions: (_max - _min) ~/ _step,
          activeColor: sun, inactiveColor: const Color(0xFFC5D3CA),
          label: '${_fmt(_amount)} FCFA',
          onChanged: (v) => setState(() {
            _amount = v.round();
            _other.text = '';
          }),
        ),
        Wrap(spacing: 6, runSpacing: 6, children: [
          for (final p in _presets)
            ChoiceChip(
              label: Text('${_fmt(p)} FCFA', style: TextStyle(fontSize: 12, color: _amount == p ? Colors.white : brand, fontWeight: FontWeight.w600)),
              selected: _amount == p, selectedColor: brand, backgroundColor: Colors.white, showCheckmark: false,
              onSelected: (_) => setState(() => _amount = p),
            ),
        ]),
        const SizedBox(height: 10),
        TextField(
          controller: _other,
          keyboardType: TextInputType.number,
          decoration: const InputDecoration(labelText: 'Autre montant', suffixText: 'FCFA', filled: true, fillColor: Colors.white),
          onChanged: (v) {
            final n = int.tryParse(v.replaceAll(RegExp(r'\D'), '')) ?? 0;
            setState(() => _amount = n < _min ? _min : (n > 100000000 ? 100000000 : n));
          },
        ),
        const Divider(height: 24),
        Row(children: [
          Switch(value: _monthly, activeThumbColor: Colors.white, activeTrackColor: brand, onChanged: (v) => setState(() => _monthly = v)),
          const SizedBox(width: 8),
          Expanded(
            child: GestureDetector(
              onTap: () => setState(() => _monthly = !_monthly),
              child: const Text.rich(TextSpan(children: [
                TextSpan(text: 'Don mensuel ', style: TextStyle(fontWeight: FontWeight.w500)),
                TextSpan(text: '(×$_months)', style: TextStyle(fontSize: 12, color: Colors.black54)),
              ])),
            ),
          ),
        ]),
        const Text('Aucune estimation fiscale configurée.', style: TextStyle(fontSize: 12, color: Colors.black54)),
        if (_monthly)
          Container(
            margin: const EdgeInsets.only(top: 10),
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(color: const Color(0xFFFFF3D6), borderRadius: BorderRadius.circular(8)),
            child: Text('Don récurrent : ${_fmt(_amount)} FCFA chaque mois, soit ${_fmt(total)} FCFA sur $_months mois. Les chiffres ci-dessous sont des projections sur $_months mois.',
                style: const TextStyle(fontSize: 12, color: Color(0xFF6B4A07))),
          ),
        const SizedBox(height: 14),
        SizedBox(
          width: double.infinity,
          child: SiteButton(
            sel != null
                ? 'Financer ${_fmt(qtyShown)} × ${sel.it['title']}${_monthly ? ' / mois' : ''}'
                : 'Faire un don de ${_fmt(_amount)} FCFA${_monthly ? ' / mois' : ''}',
            onPressed: sel != null && selQty < 1 ? null : finance,
          ),
        ),
        const SizedBox(height: 8),
        Text(
          '${sel != null ? (selQty < 1 ? 'Ce don coûte ${_fmt(asInt(sel.it['unitPrice']))} FCFA : augmentez le montant mensuel pour le financer chaque mois. ' : 'Le don choisi sera ajouté à votre panier${_monthly ? ' (fréquence mensuelle sur 12 mois proposée)' : ''}, à valider ensuite. ') : 'Touchez un don ci-dessous pour le financer en particulier. '}'
          'Simulation indicative : aucun paiement n’est effectué ici, et chaque résultat est indépendant des autres.',
          style: const TextStyle(fontSize: 11, color: Colors.black54, height: 1.4),
        ),
        const SizedBox(height: 14),
        if (rows.isEmpty)
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(16), border: Border.all(color: Colors.grey.shade300)),
            child: const Text('Aucun don n’est disponible pour le moment.', style: TextStyle(color: Colors.black54)),
          ),
        for (final r in rows)
          _SimCard(
            item: r.it, units: r.units, monthlyUnits: r.monthlyUnits, ratio: r.units / maxUnits, partial: r.partial, missing: r.missing,
            monthly: _monthly, selected: sel != null && identical(sel.it, r.it), fmt: _fmt,
            onTap: () => setState(() => _selected = _selected == asInt(r.it['id']) ? null : asInt(r.it['id'])),
          ),
      ]),
    );
  }
}

class _SimCard extends StatelessWidget {
  const _SimCard({required this.item, required this.units, required this.monthlyUnits, required this.ratio, required this.partial,
      required this.missing, required this.monthly, required this.selected, required this.fmt, required this.onTap});

  final Json item;
  final int units, monthlyUnits, missing;
  final double ratio, partial;
  final bool monthly, selected;
  final String Function(int) fmt;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final can = units > 0;
    final unit = asStr(item['unit']) ?? '';
    final caption = can
        ? (monthly ? (monthlyUnits > 0 ? 'sur 12 mois · ${fmt(monthlyUnits)} / mois' : 'sur 12 mois (cumul)') : '$unit${units > 1 ? 's' : ''} finançable${units > 1 ? 's' : ''}')
        : 'encore ${fmt(missing)} FCFA';
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Material(
        color: selected ? const Color(0xFFEAF6EE) : Colors.white,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: BorderSide(color: selected ? brand : (can ? brand.withValues(alpha: 0.25) : Colors.grey.shade300), width: selected ? 2 : 1),
        ),
        child: InkWell(
          borderRadius: BorderRadius.circular(16),
          onTap: can ? onTap : null,
          child: Opacity(
            opacity: can ? 1 : 0.7,
            child: Padding(
              padding: const EdgeInsets.all(14),
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  ConstrainedBox(
                    constraints: const BoxConstraints(minWidth: 52),
                    child: Text(fmt(units), style: TextStyle(fontSize: 28, fontWeight: FontWeight.w800, color: can ? brand : Colors.grey, height: 1)),
                  ),
                  const SizedBox(width: 8),
                  Container(
                    width: 40, height: 40, alignment: Alignment.center,
                    decoration: BoxDecoration(color: mint, borderRadius: BorderRadius.circular(12)),
                    child: Text(asStr(item['emoji']) ?? '🎁', style: const TextStyle(fontSize: 20)),
                  ),
                  const SizedBox(width: 10),
                  Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                    Text(asStr(item['title']) ?? '', style: const TextStyle(fontSize: 15, fontWeight: FontWeight.bold, color: brandDark)),
                    if (asStr(item['description']) != null)
                      Text(asStr(item['description'])!, maxLines: 2, overflow: TextOverflow.ellipsis, style: const TextStyle(fontSize: 12, color: Colors.black54)),
                    Text('${fmt(asInt(item['unitPrice']))} FCFA / $unit', style: const TextStyle(fontSize: 11, color: Colors.black54)),
                  ])),
                  if (selected) const Icon(Icons.check_circle, color: brand, size: 20),
                ]),
                const SizedBox(height: 10),
                Row(children: [
                  Expanded(
                    child: ClipRRect(
                      borderRadius: BorderRadius.circular(6),
                      child: LinearProgressIndicator(value: can ? ratio : partial, minHeight: 7, backgroundColor: mint, color: can ? sun : Colors.grey.shade400),
                    ),
                  ),
                  const SizedBox(width: 10),
                  Text(caption, style: const TextStyle(fontSize: 11, color: Colors.black54)),
                ]),
              ]),
            ),
          ),
        ),
      ),
    );
  }
}
