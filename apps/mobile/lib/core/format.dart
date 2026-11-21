import 'package:flutter/material.dart';

import 'reference.g.dart';

/// Formats et libellés repris de la plateforme web Menewa Naya.
String fcfa(num? amount) {
  final digits = (amount ?? 0).round().abs().toString();
  final buffer = StringBuffer();
  for (var i = 0; i < digits.length; i++) {
    if (i > 0 && (digits.length - i) % 3 == 0) buffer.write(' ');
    buffer.write(digits[i]);
  }
  return '${(amount ?? 0) < 0 ? '-' : ''}$buffer FCFA';
}

const _months = ['janv.', 'févr.', 'mars', 'avr.', 'mai', 'juin', 'juil.', 'août', 'sept.', 'oct.', 'nov.', 'déc.'];

String fdate(String? iso) {
  if (iso == null) return '—';
  final d = DateTime.tryParse(iso)?.toLocal();
  if (d == null) return '—';
  return '${d.day.toString().padLeft(2, '0')} ${_months[d.month - 1]} ${d.year}';
}

String fdatetime(String? iso) {
  if (iso == null) return '—';
  final d = DateTime.tryParse(iso)?.toLocal();
  if (d == null) return '—';
  return '${fdate(iso)} ${d.hour.toString().padLeft(2, '0')}:${d.minute.toString().padLeft(2, '0')}';
}

class StatusMeta {
  const StatusMeta(this.label, this.color);

  final String label;
  final Color color;
}

/// Couleur d'affichage d'une tonalité du référentiel (seul choix propre à l'application).
Color toneColor(String tone) => switch (tone) {
      'green' => const Color(0xFF047857),
      'blue' => const Color(0xFF0369A1),
      'amber' => const Color(0xFFB45309),
      'red' => const Color(0xFFB91C1C),
      'indigo' => const Color(0xFF4338CA),
      _ => const Color(0xFF475569),
    };

// Libellés et statuts : référentiel du backend (GET /api/v1/reference), généré dans reference.g.dart par `pnpm contract`.
Map<String, StatusMeta> _statuses(String group) => {
      for (final e in referenceStatuses[group]!.entries) e.key: StatusMeta(e.value.label, toneColor(e.value.tone)),
    };

final requestStatus = _statuses('request');
final missionStatus = _statuses('mission');
final proofStatus = _statuses('proof');
final paymentStatus = _statuses('payment');
final orderStatus = _statuses('order');
final listingStatus = _statuses('listing');
final disputeStatus = _statuses('dispute');

final categories = referenceLabels['category']!;
final kindLabels = referenceLabels['requestKind']!;
final proofTypes = referenceLabels['proofType']!;
final recurrences = referenceLabels['recurrence']!;
final paymentModes = referenceLabels['paymentMode']!;
final annexLabels = referenceLabels['annex']!;
final roleLabels = referenceLabels['role']!;
final providerCategories = referenceLabels['providerCategory']!;
final listingTypes = referenceLabels['listingType']!;

/// Moyens déclarables par un donateur (l'échange de services est réservé aux missions et services).
final payMethods = {
  for (final e in referenceLabels['payMethod']!.entries)
    if (e.key != 'exchange') e.key: e.value,
};
