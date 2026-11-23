import 'dart:async';
import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:ota_update/ota_update.dart';

import '../../core/config/app_config.dart';
import '../../core/widgets.dart';

/// Dernière APK publiée par deploy/dev/build-apk.sh (fichier menewa-test.json à côté de l'APK).
class AppRelease {
  const AppRelease({required this.build, required this.apk});

  final int build;
  final String apk;

  static AppRelease? fromJson(Object? json) {
    if (json is! Map) return null;
    final build = json['build'];
    final apk = json['apk'];
    if (build is! num || apk is! String || !apk.startsWith('https://')) return null;
    return AppRelease(build: build.toInt(), apk: apk);
  }

  bool isNewerThan(int current) => current > 0 && build > current;
}

/// Lit la fiche de la dernière version ; null si indisponible (hors ligne, développement local…).
Future<AppRelease?> fetchLatestRelease({http.Client? client}) async {
  if (AppConfig.updateUrl.isEmpty || defaultTargetPlatform != TargetPlatform.android) return null;
  final c = client ?? http.Client();
  try {
    final res = await c.get(Uri.parse(AppConfig.updateUrl), headers: {'Cache-Control': 'no-cache'}).timeout(const Duration(seconds: 10));
    if (res.statusCode != 200) return null;
    return AppRelease.fromJson(jsonDecode(res.body));
  } catch (_) {
    return null; // Vérification silencieuse : l'application reste utilisable.
  } finally {
    if (client == null) c.close();
  }
}

/// Vérifie une fois au démarrage s'il existe une version plus récente et propose de l'installer.
class UpdateGate extends StatefulWidget {
  const UpdateGate({super.key, required this.child});

  final Widget child;

  @override
  State<UpdateGate> createState() => _UpdateGateState();
}

class _UpdateGateState extends State<UpdateGate> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => checkForUpdate(context, silent: true));
  }

  @override
  Widget build(BuildContext context) => widget.child;
}

/// Cherche une mise à jour. `silent` : ne rien afficher si l'application est déjà à jour (vérification automatique).
Future<void> checkForUpdate(BuildContext context, {bool silent = false}) async {
  final release = await fetchLatestRelease();
  if (!context.mounted) return;
  if (release == null || !release.isNewerThan(AppConfig.appBuild)) {
    if (!silent) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: Text(release == null ? 'Impossible de vérifier les mises à jour pour le moment.' : 'L’application est à jour.')));
    }
    return;
  }
  final accept = await showDialog<bool>(
    context: context,
    builder: (ctx) => AlertDialog(
      title: const Text('Nouvelle version disponible'),
      content: const Text('Une nouvelle version de Menewa Naya est disponible. Installez-la maintenant pour profiter des dernières améliorations.\n\n'
          'Android vous demandera de confirmer l’installation (la première fois, autorisez l’installation depuis Menewa Naya).'),
      actions: [
        TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Plus tard')),
        FilledButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('Mettre à jour')),
      ],
    ),
  );
  if (accept == true && context.mounted) await _install(context, release);
}

Future<void> _install(BuildContext context, AppRelease release) {
  return showDialog<void>(context: context, barrierDismissible: false, builder: (_) => _InstallDialog(release: release));
}

class _InstallDialog extends StatefulWidget {
  const _InstallDialog({required this.release});

  final AppRelease release;

  @override
  State<_InstallDialog> createState() => _InstallDialogState();
}

class _InstallDialogState extends State<_InstallDialog> {
  StreamSubscription<OtaEvent>? _sub;
  double? _progress;
  String _status = 'Téléchargement…';
  bool _done = false;

  @override
  void initState() {
    super.initState();
    try {
      _sub = OtaUpdate().execute(widget.release.apk, destinationFilename: 'menewa-naya.apk').listen(_onEvent, onError: (Object e) => _fail('$e'));
    } catch (e) {
      _fail('$e');
    }
  }

  void _onEvent(OtaEvent e) {
    if (!mounted) return;
    setState(() {
      switch (e.status) {
        case OtaStatus.DOWNLOADING:
          final p = double.tryParse(e.value ?? '');
          _progress = p == null ? null : p / 100;
          _status = 'Téléchargement… ${p?.round() ?? ''} %';
        case OtaStatus.INSTALLING:
        case OtaStatus.INSTALLATION_DONE:
          _progress = 1;
          _status = 'Téléchargement terminé : confirmez l’installation dans la fenêtre d’Android.';
          _done = true;
        case OtaStatus.PERMISSION_NOT_GRANTED_ERROR:
          _status = 'Autorisez l’installation d’applications depuis Menewa Naya (réglages Android), puis réessayez.';
          _done = true;
        case OtaStatus.ALREADY_RUNNING_ERROR:
          _status = 'Une mise à jour est déjà en cours.';
          _done = true;
        default:
          _status = 'La mise à jour a échoué${e.value == null ? '' : ' (${e.value})'}. Vous pouvez réessayer plus tard.';
          _done = true;
      }
    });
  }

  void _fail(String message) {
    if (!mounted) return;
    setState(() {
      _status = 'La mise à jour a échoué ($message). Vous pouvez réessayer plus tard.';
      _done = true;
    });
  }

  @override
  void dispose() {
    _sub?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Mise à jour'),
      content: Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.stretch, children: [
        LinearProgressIndicator(value: _progress, color: brand, backgroundColor: mint),
        const SizedBox(height: 12),
        Text(_status),
      ]),
      actions: [
        if (_done) TextButton(onPressed: () => Navigator.pop(context), child: const Text('Fermer'))
        else TextButton(onPressed: () async {
          await OtaUpdate().cancel();
          if (context.mounted) Navigator.pop(context);
        }, child: const Text('Annuler')),
      ],
    );
  }
}

/// Ligne « Version … · Rechercher une mise à jour » pour les menus.
class VersionTile extends StatelessWidget {
  const VersionTile({super.key});

  @override
  Widget build(BuildContext context) {
    final build = AppConfig.appBuild;
    return ListTile(
      leading: const Icon(Icons.system_update_outlined),
      title: const Text('Rechercher une mise à jour'),
      subtitle: Text(build > 0 ? 'Version installée : $build' : 'Version de développement'),
      onTap: () => checkForUpdate(context),
    );
  }
}
