import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'api/api_client.dart';
import 'format.dart';
import 'session.dart';

const brand = Color(0xFF0B5A3C);
const brandDark = Color(0xFF07412C);
const sun = Color(0xFFF6B53B);
const mint = Color(0xFFEAF4EE);
const cream = Color(0xFFFFF7E3);

/// Charge des données asynchrones avec indicateur, message d'erreur, réessai et tirer-pour-rafraîchir.
class AsyncView<T> extends StatefulWidget {
  const AsyncView({super.key, required this.load, required this.builder});

  final Future<T> Function() load;
  final Widget Function(BuildContext context, T data, Future<void> Function() reload) builder;

  @override
  State<AsyncView<T>> createState() => _AsyncViewState<T>();
}

class _AsyncViewState<T> extends State<AsyncView<T>> {
  late Future<T> _future;

  @override
  void initState() {
    super.initState();
    _future = widget.load();
  }

  Future<void> _reload() async {
    final next = widget.load();
    setState(() => _future = next);
    try {
      await next;
    } catch (_) {
      // L'erreur est affichée par le FutureBuilder.
    }
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<T>(
      future: _future,
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done && !snapshot.hasData) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.hasError) {
          return ErrorView(message: snapshot.error.toString(), onRetry: _reload);
        }
        return RefreshIndicator(onRefresh: _reload, child: widget.builder(context, snapshot.data as T, _reload));
      },
    );
  }
}

class ErrorView extends StatelessWidget {
  const ErrorView({super.key, required this.message, required this.onRetry});

  final String message;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          const Icon(Icons.cloud_off, size: 48, color: Colors.grey),
          const SizedBox(height: 12),
          Text(message, textAlign: TextAlign.center),
          const SizedBox(height: 12),
          FilledButton(onPressed: onRetry, child: const Text('Réessayer')),
        ]),
      ),
    );
  }
}

class EmptyState extends StatelessWidget {
  const EmptyState(this.text, {super.key});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 8),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(color: Colors.grey.shade50, borderRadius: BorderRadius.circular(14), border: Border.all(color: Colors.grey.shade300)),
      child: Text(text, textAlign: TextAlign.center, style: TextStyle(color: Colors.grey.shade600)),
    );
  }
}

class StatusChip extends StatelessWidget {
  const StatusChip(this.meta, this.value, {super.key});

  final Map<String, StatusMeta> meta;
  final String value;

  @override
  Widget build(BuildContext context) {
    final m = meta[value] ?? StatusMeta(value, Colors.blueGrey);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 3),
      decoration: BoxDecoration(color: m.color.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(20)),
      child: Text(m.label, style: TextStyle(color: m.color, fontWeight: FontWeight.w600, fontSize: 12)),
    );
  }
}

class SectionCard extends StatelessWidget {
  const SectionCard({super.key, required this.title, required this.child, this.trailing});

  final String title;
  final Widget child;
  final Widget? trailing;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 14),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(children: [
            Expanded(child: Text(title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: brandDark))),
            if (trailing != null) trailing!,
          ]),
          const SizedBox(height: 10),
          child,
        ]),
      ),
    );
  }
}

class StatTile extends StatelessWidget {
  const StatTile({super.key, required this.label, required this.value, this.color = brand});

  final String label;
  final String value;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(label.toUpperCase(), style: TextStyle(fontSize: 11, color: Colors.grey.shade600, fontWeight: FontWeight.w600)),
          const SizedBox(height: 4),
          Text(value, style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: color)),
        ]),
      ),
    );
  }
}

/// Image protégée (preuve) chargée avec le jeton de session.
class ProtectedImage extends ConsumerWidget {
  const ProtectedImage({super.key, required this.kind, required this.id, this.height = 160});

  final String kind;
  final int id;
  final double height;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final api = ref.read(apiProvider);
    return ClipRRect(
      borderRadius: BorderRadius.circular(10),
      child: Image.network(api.fileUrl(kind, id), headers: api.authHeaders, height: height, width: double.infinity, fit: BoxFit.cover,
          errorBuilder: (_, __, ___) => Container(height: height, color: mint, child: const Center(child: Icon(Icons.image_not_supported)))),
    );
  }
}

/// Exécute une action API, affiche le message de succès ou d'erreur et retourne true en cas de succès.
Future<bool> runAction(BuildContext context, Future<dynamic> Function() action) async {
  final messenger = ScaffoldMessenger.of(context);
  try {
    final result = await action();
    final message = result is Map && result['ok'] is String ? result['ok'] as String : 'Enregistré.';
    messenger.showSnackBar(SnackBar(content: Text(message)));
    return true;
  } on ApiException catch (e) {
    messenger.showSnackBar(SnackBar(content: Text(e.message), backgroundColor: Colors.red.shade700));
    return false;
  }
}

/// Demande une saisie de texte (motif, réponse…) ; retourne null si annulé.
Future<String?> promptText(BuildContext context, {required String title, String? hint, int minLength = 0, int maxLines = 3}) {
  final controller = TextEditingController();
  return showDialog<String>(
    context: context,
    builder: (ctx) => AlertDialog(
      title: Text(title),
      content: TextField(controller: controller, maxLines: maxLines, decoration: InputDecoration(hintText: hint), autofocus: true),
      actions: [
        TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Annuler')),
        FilledButton(
          onPressed: () {
            final text = controller.text.trim();
            if (text.length >= minLength) Navigator.pop(ctx, text);
          },
          child: const Text('Valider'),
        ),
      ],
    ),
  );
}

/// Carte de section dont le contenu est une colonne d'éléments alignés à gauche.
Widget section(String title, List<Widget> children, {Widget? trailing}) => SectionCard(
      title: title,
      trailing: trailing,
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: children),
    );
