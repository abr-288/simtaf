import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';

/// Photos de l'accueil choisies par l'administration (Admin → Paramètres → Photos de l'accueil).
final homePhotosProvider = FutureProvider<List<Json>>((ref) async => asList(await ref.read(apiProvider).get('/v1/public/home-photos')));

/// Bandeau photo de l'accueil ; n'affiche rien tant qu'aucune photo n'est téléversée.
class HomeBanner extends ConsumerWidget {
  const HomeBanner({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final photos = ref.watch(homePhotosProvider).valueOrNull ?? const [];
    final hero = photos.where((p) => p['slot'] == 'hero' && p['available'] == true).firstOrNull;
    if (hero == null) return const SizedBox.shrink();
    final api = ref.read(apiProvider);
    return Padding(
      padding: const EdgeInsets.only(bottom: 14),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(16),
        child: Stack(children: [
          Image.network('${api.baseUrl}/v1/public/home-photos/hero?v=${hero['version']}',
              headers: api.authHeaders, height: 170, width: double.infinity, fit: BoxFit.cover,
              semanticLabel: asStr(hero['alt']),
              errorBuilder: (_, __, ___) => const SizedBox.shrink()),
          Positioned.fill(
            child: DecoratedBox(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.bottomCenter,
                  end: Alignment.center,
                  colors: [brandDark.withValues(alpha: 0.8), Colors.transparent],
                ),
              ),
            ),
          ),
          const Positioned(
            left: 14,
            right: 14,
            bottom: 12,
            child: Text('Ensemble, on fait la différence.', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 17)),
          ),
        ]),
      ),
    );
  }
}
