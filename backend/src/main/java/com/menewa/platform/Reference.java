package com.menewa.platform;

import com.menewa.identity.IdentityPermissions;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Référentiel unique des codes métier et de leurs libellés (statuts, catégories, rôles, permissions…).
 * Les clients web et mobile en sont générés (`pnpm contract`) : aucun libellé n'est plus recopié à la main.
 */
@RestController
public class Reference {
    public record Status(String label, String tone) {}

    public record Catalog(Map<String, Map<String, Status>> statuses, Map<String, Map<String, String>> labels,
            List<String> selfRegisterRoles) {}

    private static Map<String, Status> statuses(String... triplets) {
        Map<String, Status> out = new LinkedHashMap<>();
        for (int i = 0; i < triplets.length; i += 3) out.put(triplets[i], new Status(triplets[i + 1], triplets[i + 2]));
        return out;
    }

    private static Map<String, String> labels(String... pairs) {
        Map<String, String> out = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) out.put(pairs[i], pairs[i + 1]);
        return out;
    }

    static final Map<String, String> PERMISSIONS = labels(
            "requests.manage", "Traiter les demandes (accepter / refuser / demander des infos)",
            "users.manage", "Gérer les utilisateurs (suspension, archivage)",
            "providers.manage", "Gérer les prestataires et leurs services",
            "beneficiaries.view", "Consulter / gérer les bénéficiaires (données personnelles)",
            "missions.manage", "Créer, attribuer et réattribuer les missions",
            "proofs.review", "Contrôler les preuves de remise",
            "payments.manage", "Suivre et valider les paiements",
            "finances.view", "Consulter coûts, marges et finances globales",
            "disputes.manage", "Traiter les litiges",
            "stats.view", "Consulter les statistiques",
            "stats.export", "Exporter les rapports",
            "tokens.manage", "Gérer les règles et opérations Token Mena",
            "catalog.manage", "Gérer le catalogue et les tarifs",
            "listings.moderate", "Modérer les publications partenaires et campagnes",
            "audit.view", "Consulter le journal d'audit",
            "roles.manage", "Gérer les rôles et permissions (administrateur principal)",
            "settings.manage", "Modifier la configuration de la plateforme");

    static {
        if (!PERMISSIONS.keySet().equals(IdentityPermissions.ALL)) throw new IllegalStateException("Libellés de permissions désynchronisés");
    }

    static final Catalog CATALOG;
    static {
        Map<String, Map<String, Status>> st = new LinkedHashMap<>();
        st.put("request", statuses("submitted", "Soumise", "blue", "info_requested", "Infos demandées", "amber", "accepted", "Acceptée", "green",
                "rejected", "Refusée", "red", "in_progress", "En cours", "indigo", "completed", "Réalisée", "green", "closed", "Clôturée", "slate",
                "cancelled", "Annulée", "slate"));
        st.put("mission", statuses("proposed", "Proposée", "amber", "accepted", "Acceptée", "blue", "refused", "Refusée", "red",
                "in_progress", "En cours", "indigo", "completed", "Terminée", "green", "cancelled", "Annulée", "slate"));
        st.put("proof", statuses("pending", "À contrôler", "amber", "validated", "Validée", "green", "rejected", "Rejetée", "red"));
        st.put("payment", statuses("planned", "Prévu", "slate", "declared", "Déclaré", "amber", "validated", "Validé", "blue",
                "partial", "Partiel", "indigo", "paid", "Payé", "green", "cancelled", "Annulé", "red"));
        st.put("dispute", statuses("open", "Ouvert", "red", "investigating", "En instruction", "amber", "resolved", "Résolu", "green", "closed", "Clos", "slate"));
        st.put("order", statuses("requested", "Demandé", "blue", "negotiating", "Négociation", "amber", "agreed", "Accord conclu", "green",
                "in_progress", "En cours", "indigo", "done", "Réalisé (à confirmer)", "amber", "confirmed", "Confirmé", "green",
                "declined", "Décliné", "red", "cancelled", "Annulé", "slate"));
        st.put("listing", statuses("pending", "En modération", "amber", "published", "Publiée", "green", "rejected", "Refusée", "red", "closed", "Close", "slate"));
        st.put("token", statuses("approved", "Validée", "green", "pending", "En attente", "amber", "rejected", "Rejetée", "red"));
        st.put("user", statuses("active", "Actif", "green", "suspended", "Suspendu", "red", "archived", "Archivé", "slate"));

        Map<String, Map<String, String>> lb = new LinkedHashMap<>();
        lb.put("requestKind", labels("catalogue", "Don du catalogue", "personnalise", "Don personnel", "action", "Action / visite",
                "recurrent", "Engagement récurrent"));
        lb.put("paymentMode", labels("immediate", "Paiement immédiat", "prefinance", "Préfinancement", "echeance", "Paiement à chaque échéance"));
        lb.put("recurrence", labels("none", "Ponctuel", "weekly", "Hebdomadaire", "monthly", "Mensuel", "quarterly", "Trimestriel"));
        lb.put("category", labels("nourriture", "Nourriture", "betail", "Bétail", "volaille", "Volailles", "habits", "Habits",
                "fournitures", "Fournitures", "equipements", "Équipements", "action", "Actions & visites", "autre", "Autre"));
        lb.put("providerCategory", labels("officiant", "Officiant religieux", "commerce", "Commerce / approvisionnement",
                "traiteur", "Service traiteur", "transport", "Chauffeur / transporteur", "photo", "Photo / vidéo", "autre", "Autre service"));
        lb.put("proofType", labels("photo", "Photo", "video", "Vidéo", "receipt", "Reçu", "invoice", "Facture", "attestation", "Attestation"));
        lb.put("payMethod", labels("mobile_money", "Mobile Money (T-Money, Flooz…)", "card", "Carte bancaire", "bank", "Virement bancaire",
                "cash", "Espèces", "exchange", "Échange de services"));
        lb.put("listingType", labels("don", "Don", "action", "Action solidaire", "crowdfunding", "Financement participatif"));
        lb.put("annex", labels("packaging", "Personnalisation du packaging", "officiant", "Intervention d'un officiant religieux",
                "photos", "Prises de photos", "video", "Reportage photo / vidéo"));
        lb.put("role", labels("donor", "Donateur", "client", "Client", "provider", "Prestataire", "partner", "Partenaire",
                "beneficiary", "Bénéficiaire", "subadmin", "Administrateur secondaire", "admin", "Administrateur"));
        lb.put("permission", PERMISSIONS);
        CATALOG = new Catalog(st, lb, List.of("donor", "client", "provider", "partner", "beneficiary"));
    }

    /** Libellé d'un code (ou le code lui-même s'il est inconnu), pour les messages générés côté serveur. */
    public static String label(String group, String code) {
        var g = CATALOG.labels().get(group);
        return g == null ? code : g.getOrDefault(code, code);
    }

    @GetMapping("/api/v1/reference")
    Catalog reference() { return CATALOG; }
}
