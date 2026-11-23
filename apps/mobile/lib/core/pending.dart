import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Action demandée depuis le site public avant connexion, reprise dès que l'espace membre s'ouvre
/// (comme le panier en attente du simulateur sur le web).
class PendingAction {
  const PendingAction.tab(this.tab)
      : itemId = null,
        quantity = 0,
        monthly = false;

  const PendingAction.cart({required int this.itemId, required this.quantity, required this.monthly}) : tab = 'Catalogue';

  /// Onglet à ouvrir (« Catalogue », « Services »…).
  final String tab;

  /// Don du simulateur à ajouter au panier.
  final int? itemId;
  final int quantity;
  final bool monthly;

  bool get isCart => itemId != null;
}

final pendingActionProvider = StateProvider<PendingAction?>((ref) => null);
