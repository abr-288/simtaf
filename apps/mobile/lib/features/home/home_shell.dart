import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/models.dart';
import '../../core/session.dart';
import '../catalog/catalog_screen.dart';
import '../missions/missions_screen.dart';
import '../notifications/notifications_screen.dart';
import '../profile/profile_screen.dart';
import '../requests/requests_screen.dart';
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

  List<_Tab> _tabs(AppUser u) {
    final home = _Tab('Accueil', Icons.home_outlined, DashboardScreen(user: u));
    const notifications = _Tab('Alertes', Icons.notifications_outlined, NotificationsScreen());
    final profile = _Tab('Profil', Icons.person_outline, ProfileScreen(user: u));
    if (u.isRequester) {
      return [
        home,
        _Tab('Catalogue', Icons.card_giftcard, CatalogScreen(user: u)),
        const _Tab('Demandes', Icons.list_alt, RequestsScreen()),
        notifications,
        profile,
      ];
    }
    if (u.role == 'provider') return [home, const _Tab('Missions', Icons.handyman_outlined, MissionsScreen()), notifications, profile];
    return [home, notifications, profile];
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
      appBar: AppBar(title: Text(tabs[index].label == 'Accueil' ? 'Menewa Naya' : tabs[index].label)),
      body: IndexedStack(index: index, children: tabs.map((t) => t.page).toList()),
      bottomNavigationBar: NavigationBar(
        selectedIndex: index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: tabs.map((t) => NavigationDestination(icon: Icon(t.icon), label: t.label)).toList(),
      ),
    );
  }
}
