import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/format.dart';
import '../../core/models.dart';
import '../../core/session.dart';
import '../../core/widgets.dart';
import '../cart/cart_screen.dart';

/// Catalogue des dons : quantité et cible, puis ajout au panier (prix revérifiés par l'API).
class CatalogScreen extends ConsumerStatefulWidget {
  const CatalogScreen({super.key, required this.user});

  final AppUser user;

  @override
  ConsumerState<CatalogScreen> createState() => _CatalogScreenState();
}

class _CatalogScreenState extends ConsumerState<CatalogScreen> {
  String? _category;

  Future<(List<CatalogItem>, List<String>)> _load() async {
    final api = ref.read(apiProvider);
    final catalog = await api.get('/catalog') as Map;
    final targets = asList(await api.get('/v1/catalog/targets')).map((t) => asStr(t['label']) ?? '').where((l) => l.isNotEmpty).toList();
    return (asList(catalog['items']).map(CatalogItem.fromJson).toList(), targets);
  }

  Future<void> _add(CatalogItem item, List<String> targets) async {
    final qty = TextEditingController(text: '1');
    final custom = TextEditingController();
    String? target;
    final ok = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setSheet) => Padding(
          padding: EdgeInsets.fromLTRB(20, 20, 20, MediaQuery.of(ctx).viewInsets.bottom + 20),
          child: Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.stretch, children: [
            Text('${item.emoji} ${item.title}', style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            Text('${fcfa(item.unitPrice)} / ${item.unit}', style: const TextStyle(color: brand)),
            const SizedBox(height: 12),
            TextField(controller: qty, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Quantité')),
            const SizedBox(height: 12),
            DropdownButtonFormField<String>(
              initialValue: target,
              decoration: const InputDecoration(labelText: 'Cible (facultatif)'),
              items: [
                ...targets.map((t) => DropdownMenuItem(value: t, child: Text(t))),
                if (widget.user.role == 'donor') const DropdownMenuItem(value: '__custom__', child: Text('Autre cible (personnalisée)…')),
              ],
              onChanged: (v) => setSheet(() => target = v),
            ),
            if (target == '__custom__') ...[
              const SizedBox(height: 12),
              TextField(controller: custom, decoration: const InputDecoration(labelText: 'Précisez la cible')),
            ],
            const SizedBox(height: 16),
            FilledButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('Ajouter au panier')),
          ]),
        ),
      ),
    );
    if (ok != true || !mounted) return;
    final chosen = target == '__custom__' ? custom.text.trim() : target;
    await runAction(context, () => ref.read(apiProvider).post('/v1/cart', {
          'catalogItemId': item.id,
          'quantity': (int.tryParse(qty.text) ?? 1).clamp(1, 500),
          if (chosen != null && chosen.isNotEmpty) 'target': chosen,
        }));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => CartScreen(user: widget.user))),
        icon: const Icon(Icons.shopping_cart_outlined),
        label: const Text('Panier'),
      ),
      body: AsyncView<(List<CatalogItem>, List<String>)>(
        load: _load,
        builder: (context, data, reload) {
          final (items, targets) = data;
          final shown = _category == null ? items : items.where((i) => i.category == _category).toList();
          return ListView(padding: const EdgeInsets.fromLTRB(16, 8, 16, 90), children: [
            SizedBox(
              height: 44,
              child: ListView(scrollDirection: Axis.horizontal, children: [
                Padding(padding: const EdgeInsets.only(right: 6), child: ChoiceChip(label: const Text('Tous'), selected: _category == null, onSelected: (_) => setState(() => _category = null))),
                ...categories.entries.where((e) => e.key != 'autre').map((e) => Padding(
                      padding: const EdgeInsets.only(right: 6),
                      child: ChoiceChip(label: Text(e.value), selected: _category == e.key, onSelected: (_) => setState(() => _category = e.key)),
                    )),
              ]),
            ),
            const SizedBox(height: 8),
            if (shown.isEmpty) const EmptyState('Aucun article dans cette catégorie.'),
            ...shown.map((i) => Card(
                  margin: const EdgeInsets.only(bottom: 10),
                  child: ListTile(
                    contentPadding: const EdgeInsets.all(12),
                    leading: CircleAvatar(radius: 26, backgroundColor: mint, child: Text(i.emoji, style: const TextStyle(fontSize: 24))),
                    title: Text(i.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                    subtitle: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                      if (i.description != null) Text(i.description!, maxLines: 2, overflow: TextOverflow.ellipsis),
                      const SizedBox(height: 4),
                      Text('${fcfa(i.unitPrice)} / ${i.unit}', style: const TextStyle(color: brand, fontWeight: FontWeight.bold)),
                    ]),
                    trailing: IconButton.filled(icon: const Icon(Icons.add_shopping_cart), tooltip: 'Ajouter au panier', onPressed: () => _add(i, targets)),
                  ),
                )),
          ]);
        },
      ),
    );
  }
}
