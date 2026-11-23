import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../campaigns/campaigns_screen.dart';
import '../cart/cart_screen.dart';
import '../help/help_screen.dart';
import '../catalog/catalog_screen.dart';
import '../listings/my_listings_screen.dart';
import '../missions/missions_screen.dart';
import '../notifications/notifications_screen.dart';
import '../payments/my_payments_screen.dart';
import '../profile/profile_screen.dart';
import '../requests/custom_request_screen.dart';
import '../requests/requests_screen.dart';
import '../rewards/rewards_screen.dart';
import '../services/services_screen.dart';
import 'dashboard_screen.dart';

class _Tab {
  const _Tab(this.label, this.icon, this.page);

  final String label;
  final IconData icon;
  final Widget page;
}

/// Espace membre : onglets adaptés au rôle (affichage seulement, l'API applique les droits).
class HomeShell extends ConsumerStatefulWidget {
  const HomeShell({super.key, required this.user});

  final AppUser user;

  @override
  ConsumerState<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends ConsumerState<HomeShell> {
  int _index = 0;

  int _unread = 0;

  @override
  void initState() {
    super.initState();
    _refreshUnread();
  }

  Future<void> _refreshUnread() async {
    try {
      final res = await ref.read(apiProvider).get('/v1/notifications/unread-count');
      if (mounted) setState(() => _unread = asInt((res as Map)['count']));
    } catch (_) {
      // Compteur indicatif : une erreur réseau ne bloque pas l'écran.
    }
  }

  /// Onglets du bas selon le rôle (5 au plus) ; alertes et profil sont dans la barre du haut.
  List<_Tab> _tabs(AppUser u) {
    final home = _Tab('Accueil', Icons.home_outlined, DashboardScreen(user: u));
    final services = _Tab('Services', Icons.handshake_outlined, ServicesScreen(user: u));
    const campaigns = _Tab('Campagnes', Icons.campaign_outlined, CampaignsScreen());
    if (u.isRequester) {
      return [
        home,
        _Tab('Catalogue', Icons.card_giftcard, CatalogScreen(user: u)),
        const _Tab('Demandes', Icons.list_alt, RequestsScreen()),
        services,
        campaigns,
      ];
    }
    if (u.role == 'provider') return [home, const _Tab('Missions', Icons.handyman_outlined, MissionsScreen()), services];
    if (u.role == 'partner') return [home, const _Tab('Publications', Icons.post_add, MyListingsScreen()), campaigns];
    return [home];
  }

  Future<void> _open(String title, Widget page) async {
    await Navigator.push(context, MaterialPageRoute(builder: (_) => Scaffold(appBar: AppBar(title: Text(title)), body: page)));
    await _refreshUnread();
  }

  /// Menu latéral : écrans secondaires selon le rôle (l'API applique les droits).
  Widget _drawer(AppUser u) {
    final requester = u.role == 'donor' || u.role == 'client';
    void go(String title, Widget page) {
      Navigator.pop(context);
      _open(title, page);
    }

    return Drawer(
      child: SafeArea(
        child: ListView(children: [
          ListTile(
            title: Text(u.name.isEmpty ? u.phone : u.name, style: const TextStyle(fontWeight: FontWeight.bold)),
            subtitle: Text('${roleLabels[u.role] ?? u.role} · ${u.phone}'),
          ),
          const Divider(),
          if (requester) ...[
            ListTile(
              leading: const Icon(Icons.volunteer_activism_outlined),
              title: const Text('Don personnel / action'),
              onTap: () {
                Navigator.pop(context);
                Navigator.push(context, MaterialPageRoute(builder: (_) => CustomRequestScreen(user: u)));
              },
            ),
            ListTile(
              leading: const Icon(Icons.shopping_cart_outlined),
              title: const Text('Mon panier'),
              onTap: () {
                Navigator.pop(context);
                Navigator.push(context, MaterialPageRoute(builder: (_) => CartScreen(user: u)));
              },
            ),
          ],
          if (u.role != 'beneficiary')
            ListTile(
              leading: const Icon(Icons.account_balance_wallet_outlined),
              title: Text(u.role == 'provider' ? 'Mes rémunérations' : 'Mes paiements'),
              onTap: () => go(u.role == 'provider' ? 'Mes rémunérations' : 'Mes paiements', MyPaymentsScreen(user: u)),
            ),
          if (u.role != 'beneficiary')
            ListTile(leading: const Text('🪙', style: TextStyle(fontSize: 20)), title: const Text('Token Mena'), onTap: () => go('Token Mena', const RewardsScreen())),
          ListTile(leading: const Icon(Icons.notifications_outlined), title: const Text('Alertes'), onTap: () => go('Alertes', const NotificationsScreen())),
          ListTile(leading: const Icon(Icons.person_outline), title: const Text('Profil'), onTap: () => go('Profil', ProfileScreen(user: u))),
          ListTile(leading: const Icon(Icons.help_outline), title: const Text('Aide'), onTap: () => go('Aide', HelpScreen(user: u))),
        ]),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final u = widget.user;
    if (u.isAdmin) {
      return Scaffold(
        body: Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(mainAxisSize: MainAxisSize.min, children: [
              const Text('Les comptes administrateurs utilisent la console d’administration web.', textAlign: TextAlign.center),
              const SizedBox(height: 16),
              FilledButton(onPressed: () => ref.read(sessionProvider.notifier).logout(), child: const Text('Se déconnecter')),
            ]),
          ),
        ),
      );
    }
    final tabs = _tabs(u);
    final index = _index.clamp(0, tabs.length - 1);
    return Scaffold(
      appBar: AppBar(
        title: Text(tabs[index].label == 'Accueil' ? 'Menewa Naya' : tabs[index].label),
        actions: [
          IconButton(
            tooltip: 'Alertes',
            icon: Badge(isLabelVisible: _unread > 0, label: Text('$_unread'), child: const Icon(Icons.notifications_outlined)),
            onPressed: () => _open('Alertes', const NotificationsScreen()),
          ),
          IconButton(tooltip: 'Profil', icon: const Icon(Icons.person_outline), onPressed: () => _open('Profil', ProfileScreen(user: u))),
        ],
      ),
      drawer: _drawer(u),
      body: IndexedStack(index: index, children: tabs.map((t) => t.page).toList()),
      bottomNavigationBar: tabs.length < 2 ? null : NavigationBar(
        selectedIndex: index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: tabs.map((t) => NavigationDestination(icon: Icon(t.icon), label: t.label)).toList(),
      ),
    );
  }
}
