/**
 * Parcours de don de bout en bout : panier, demandes et échéances, bénéficiaires, missions des
 * prestataires, preuves de remise (stockage privé), paiements et échanges de services, litiges,
 * tableaux de bord, finances et statistiques. Regroupés tant que leurs invariants restent couplés
 * (clôture d'un don = preuves + missions + échéances) ; découpage possible ensuite via des événements.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Giving operations")
package com.menewa.giving;
