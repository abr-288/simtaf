import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/session.dart';
import '../../core/widgets.dart';
import 'public_home_screen.dart';
import 'public_pages.dart';
import 'public_widgets.dart';

/// Site public dans l'application : premier écran sans connexion (même contenu que le site web).
/// Ouvert depuis l'espace membre (`fromSpace`), il propose « Mon espace » au lieu de la connexion.
class PublicShell extends ConsumerStatefulWidget {
  const PublicShell({super.key, this.fromSpace = false});

  final bool fromSpace;

  @override
  ConsumerState<PublicShell> createState() => _PublicShellState();
}

class _PublicShellState extends ConsumerState<PublicShell> {
  static const _tabs = ['Accueil', 'Social Kitchen', 'Social Network', 'Partenaires', 'Plus'];
  int _index = 0;

  void _navigate(String page) {
    final tab = _tabs.indexOf(page);
    if (tab >= 0) {
      setState(() => _index = tab);
    } else {
      pushPublicPage(context, page);
    }
  }

  @override
  Widget build(BuildContext context) {
    final loggedIn = ref.watch(sessionProvider).authenticated;
    final pages = [
      PublicHomeScreen(onNavigate: _navigate),
      const SocialKitchenPage(),
      const SocialNetworkPage(),
      const PartnersPage(),
      _MorePage(onNavigate: _navigate),
    ];
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.white,
        surfaceTintColor: Colors.white,
        titleSpacing: widget.fromSpace ? 0 : 16,
        title: GestureDetector(
          onTap: () => setState(() => _index = 0),
          child: const Text.rich(TextSpan(children: [
            TextSpan(text: 'Menewa ', style: TextStyle(fontWeight: FontWeight.w800, color: brand)),
            TextSpan(text: 'Naya', style: TextStyle(fontWeight: FontWeight.w800, color: sun)),
          ])),
        ),
        actions: [
          if (loggedIn)
            Padding(
              padding: const EdgeInsets.only(right: 8),
              child: FilledButton.icon(
                style: FilledButton.styleFrom(backgroundColor: sun, foregroundColor: brandDark),
                icon: const Icon(Icons.person_outline, size: 18),
                label: const Text('Mon espace'),
                onPressed: () => Navigator.of(context).popUntil((r) => r.isFirst),
              ),
            )
          else ...[
            TextButton(onPressed: () => openLogin(context, ref), child: const Text('Se connecter')),
            Padding(
              padding: const EdgeInsets.only(right: 8),
              child: FilledButton(
                style: FilledButton.styleFrom(backgroundColor: brand, padding: const EdgeInsets.symmetric(horizontal: 14)),
                onPressed: () => openLogin(context, ref, register: true),
                child: const Text('S’inscrire'),
              ),
            ),
          ],
        ],
      ),
      body: IndexedStack(index: _index, children: pages),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        labelBehavior: NavigationDestinationLabelBehavior.alwaysShow,
        destinations: const [
          NavigationDestination(icon: Icon(Icons.home_outlined), label: 'Accueil'),
          NavigationDestination(icon: Icon(Icons.volunteer_activism_outlined), label: 'Kitchen'),
          NavigationDestination(icon: Icon(Icons.groups_outlined), label: 'Network'),
          NavigationDestination(icon: Icon(Icons.handshake_outlined), label: 'Partenaires'),
          NavigationDestination(icon: Icon(Icons.menu), label: 'Plus'),
        ],
      ),
    );
  }
}

/// Le reste du menu du site (Nos services, À propos, Actualités, Contact) et les pages du pied de page.
class _MorePage extends ConsumerWidget {
  const _MorePage({required this.onNavigate});

  final void Function(String page) onNavigate;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final loggedIn = ref.watch(sessionProvider).authenticated;
    Widget item(IconData icon, String page, [String? sub]) => ListTile(
          leading: Icon(icon, color: brand),
          title: Text(page),
          subtitle: sub == null ? null : Text(sub),
          trailing: const Icon(Icons.chevron_right),
          onTap: () => onNavigate(page),
        );
    return ListView(children: [
      const Padding(padding: EdgeInsets.fromLTRB(16, 16, 16, 4), child: Text('Nos services', style: TextStyle(fontWeight: FontWeight.bold, color: brandDark))),
      item(Icons.volunteer_activism_outlined, 'Social Kitchen', 'Faire un don, financer un besoin'),
      item(Icons.groups_outlined, 'Social Network', 'Compétences et services de proximité'),
      item(Icons.route_outlined, 'Comment ça marche', 'Les étapes pour agir ensemble'),
      const Divider(),
      item(Icons.info_outline, 'À propos'),
      item(Icons.handshake_outlined, 'Partenaires'),
      item(Icons.newspaper_outlined, 'Actualités'),
      item(Icons.mail_outline, 'Contact'),
      item(Icons.help_outline, 'Aide'),
      const Divider(),
      item(Icons.gavel_outlined, 'Mentions légales'),
      item(Icons.privacy_tip_outlined, 'Politique de confidentialité'),
      item(Icons.description_outlined, 'Conditions d’utilisation'),
      if (!loggedIn)
        Padding(
          padding: const EdgeInsets.all(16),
          child: Row(children: [
            Expanded(child: SiteButton('Se connecter', yellow: true, onPressed: () => openLogin(context, ref))),
            const SizedBox(width: 10),
            Expanded(child: SiteButton('S’inscrire', onPressed: () => openLogin(context, ref, register: true))),
          ]),
        ),
      const PublicFooterNote(),
    ]);
  }
}

class PublicFooterNote extends StatelessWidget {
  const PublicFooterNote({super.key});

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.all(16),
        child: Text('© ${DateTime.now().year} Menewa Naya. Tous droits réservés.\nTogo | Afrique | Diaspora',
            textAlign: TextAlign.center, style: const TextStyle(fontSize: 11, color: Colors.black54)),
      );
}
