import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/models.dart';
import '../../core/pending.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';
import '../auth/login_screen.dart';
import 'public_pages.dart';

/// Photos de l'accueil téléversées par l'administration (mêmes emplacements que le site).
final publicPhotosProvider = FutureProvider<Map<String, Json>>((ref) async {
  final list = asList(await ref.read(apiProvider).get('/v1/public/home-photos'));
  return {for (final p in list) if (p['available'] == true) asStr(p['slot'])!: p};
});

/// Image d'un emplacement de l'accueil, ou rien si l'administration n'en a pas téléversé.
class SlotPhoto extends ConsumerWidget {
  const SlotPhoto(this.slot, {super.key, this.fit = BoxFit.cover, this.alignment = Alignment.center});

  final String slot;
  final BoxFit fit;
  final Alignment alignment;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final photo = ref.watch(publicPhotosProvider).valueOrNull?[slot];
    if (photo == null) return const SizedBox.shrink();
    final api = ref.read(apiProvider);
    return Image.network('${api.baseUrl}/v1/public/home-photos/$slot?v=${photo['version']}',
        headers: api.authHeaders, fit: fit, alignment: alignment, semanticLabel: asStr(photo['alt']),
        errorBuilder: (_, __, ___) => const SizedBox.shrink());
  }
}

/// Ouvre la connexion (ou l'inscription) ; l'action en attente est reprise une fois connecté.
Future<void> openLogin(BuildContext context, WidgetRef ref, {bool register = false, String? reason, PendingAction? then}) async {
  if (then != null) ref.read(pendingActionProvider.notifier).state = then;
  if (ref.read(sessionProvider).authenticated) {
    // Déjà connecté (site ouvert depuis l'espace) : retour à l'espace, qui reprend l'action.
    Navigator.of(context).popUntil((r) => r.isFirst);
    return;
  }
  await Navigator.push(context, MaterialPageRoute(builder: (_) => LoginScreen(initialRegister: register, reason: reason)));
}

/// Bandeau de titre des pages intérieures (équivalent de PageHero sur le site).
class PageHero extends StatelessWidget {
  const PageHero({super.key, this.eyebrow, required this.title, this.text});

  final String? eyebrow;
  final String title;
  final String? text;

  @override
  Widget build(BuildContext context) {
    return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
      Container(
        padding: const EdgeInsets.fromLTRB(20, 28, 20, 24),
        decoration: const BoxDecoration(gradient: LinearGradient(colors: [mint, Colors.white, cream])),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          if (eyebrow != null)
            Text(eyebrow!.toUpperCase(), style: const TextStyle(fontSize: 11, letterSpacing: 2.2, fontWeight: FontWeight.w600, color: brand)),
          const SizedBox(height: 6),
          Text(title, style: const TextStyle(fontSize: 27, fontWeight: FontWeight.w800, color: brandDark, height: 1.15)),
          if (text != null) ...[
            const SizedBox(height: 10),
            Text(text!, style: const TextStyle(fontSize: 15, color: Color(0xFF334155), height: 1.4)),
          ],
        ]),
      ),
      const PatternStrip(height: 6),
    ]);
  }
}

/// Bande décorative aux couleurs de Menewa (vert / jaune), comme le motif du site.
class PatternStrip extends StatelessWidget {
  const PatternStrip({super.key, this.height = 10});

  final double height;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: height,
      child: Row(children: List.generate(24, (i) => Expanded(child: ColoredBox(color: i.isEven ? brand : sun)))),
    );
  }
}

/// Carte blanche à bordure, utilisée par les pages du site.
class PublicCard extends StatelessWidget {
  const PublicCard({super.key, required this.child, this.accent, this.top = false, this.color = Colors.white});

  final Widget child;
  final Color? accent;
  final bool top;
  final Color color;

  @override
  Widget build(BuildContext context) {
    // Flutter n'arrondit qu'une bordure d'une seule couleur : le liseré de couleur est dessiné à l'intérieur.
    final mark = accent == null ? null : BorderSide(color: accent!, width: 4);
    return Container(
      margin: const EdgeInsets.only(bottom: 14),
      clipBehavior: Clip.antiAlias,
      decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(16), border: Border.all(color: Colors.grey.shade200)),
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.all(16),
        decoration: mark == null ? null : BoxDecoration(border: top ? Border(top: mark) : Border(left: mark)),
        child: child,
      ),
    );
  }
}

const h2 = TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: brandDark);
const body = TextStyle(fontSize: 14, color: Color(0xFF475569), height: 1.45);

/// Bouton vert ou jaune du site.
class SiteButton extends StatelessWidget {
  const SiteButton(this.label, {super.key, required this.onPressed, this.yellow = false});

  final String label;
  final VoidCallback? onPressed;
  final bool yellow;

  @override
  Widget build(BuildContext context) {
    return FilledButton(
      style: FilledButton.styleFrom(
        backgroundColor: yellow ? sun : brand,
        foregroundColor: yellow ? brandDark : Colors.white,
        padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 14),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        textStyle: const TextStyle(fontWeight: FontWeight.bold),
      ),
      onPressed: onPressed,
      child: Row(mainAxisSize: MainAxisSize.min, children: [Flexible(child: Text(label, textAlign: TextAlign.center)), const SizedBox(width: 6), const Icon(Icons.arrow_forward, size: 16)]),
    );
  }
}

/// Pied de page du site : navigation, liens légaux, mention Togo | Afrique | Diaspora.
class PublicFooter extends StatelessWidget {
  const PublicFooter({super.key, required this.onNavigate});

  /// Ouvre une page du site par son nom (« Accueil », « À propos »…).
  final void Function(String page) onNavigate;

  @override
  Widget build(BuildContext context) {
    Widget link(String label, [String? page]) => InkWell(
          onTap: () => onNavigate(page ?? label),
          child: Padding(padding: const EdgeInsets.symmetric(vertical: 4, horizontal: 6), child: Text(label, style: const TextStyle(color: Colors.white, fontSize: 12))),
        );
    return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
      Container(
        color: brand,
        padding: const EdgeInsets.fromLTRB(16, 22, 16, 14),
        child: Column(children: [
          const Text('Menewa Naya', style: TextStyle(color: Colors.white, fontSize: 22, fontWeight: FontWeight.w800)),
          const Text('Ensemble, on fait la différence.', style: TextStyle(color: Colors.white70, fontSize: 12)),
          const SizedBox(height: 14),
          Wrap(alignment: WrapAlignment.center, children: [
            link('Accueil'), link('Nos services', 'Social Kitchen'), link('À propos'), link('Partenaires'), link('Actualités'), link('Contact'),
          ]),
          const Divider(color: Colors.white24, height: 24),
          Text('© ${DateTime.now().year} Menewa Naya. Tous droits réservés.', style: const TextStyle(color: Colors.white, fontSize: 11)),
          const SizedBox(height: 6),
          Wrap(alignment: WrapAlignment.center, children: [
            link('Mentions légales'), link('Politique de confidentialité'), link('Conditions d’utilisation'), link('Aide'),
          ]),
          const SizedBox(height: 8),
          const Text('Togo | Afrique | Diaspora', style: TextStyle(color: Colors.white, fontSize: 11)),
        ]),
      ),
      const PatternStrip(height: 14),
    ]);
  }
}

/// Ouvre une page intérieure du site dans une nouvelle route (avec la barre du site).
void pushPublicPage(BuildContext context, String page) {
  final screen = publicPage(page);
  if (screen == null) return;
  Navigator.push(context, MaterialPageRoute(builder: (_) => Scaffold(appBar: AppBar(title: Text(page)), body: screen)));
}
