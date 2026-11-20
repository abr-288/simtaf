// Généré par scripts/contract.mjs depuis docs/api/reference.json (GET /api/v1/reference) — ne pas modifier à la main.

export const REFERENCE = {
  "statuses": {
    "request": {
      "submitted": {
        "label": "Soumise",
        "tone": "blue"
      },
      "info_requested": {
        "label": "Infos demandées",
        "tone": "amber"
      },
      "accepted": {
        "label": "Acceptée",
        "tone": "green"
      },
      "rejected": {
        "label": "Refusée",
        "tone": "red"
      },
      "in_progress": {
        "label": "En cours",
        "tone": "indigo"
      },
      "completed": {
        "label": "Réalisée",
        "tone": "green"
      },
      "closed": {
        "label": "Clôturée",
        "tone": "slate"
      },
      "cancelled": {
        "label": "Annulée",
        "tone": "slate"
      }
    },
    "mission": {
      "proposed": {
        "label": "Proposée",
        "tone": "amber"
      },
      "accepted": {
        "label": "Acceptée",
        "tone": "blue"
      },
      "refused": {
        "label": "Refusée",
        "tone": "red"
      },
      "in_progress": {
        "label": "En cours",
        "tone": "indigo"
      },
      "completed": {
        "label": "Terminée",
        "tone": "green"
      },
      "cancelled": {
        "label": "Annulée",
        "tone": "slate"
      }
    },
    "proof": {
      "pending": {
        "label": "À contrôler",
        "tone": "amber"
      },
      "validated": {
        "label": "Validée",
        "tone": "green"
      },
      "rejected": {
        "label": "Rejetée",
        "tone": "red"
      }
    },
    "payment": {
      "planned": {
        "label": "Prévu",
        "tone": "slate"
      },
      "declared": {
        "label": "Déclaré",
        "tone": "amber"
      },
      "validated": {
        "label": "Validé",
        "tone": "blue"
      },
      "partial": {
        "label": "Partiel",
        "tone": "indigo"
      },
      "paid": {
        "label": "Payé",
        "tone": "green"
      },
      "cancelled": {
        "label": "Annulé",
        "tone": "red"
      }
    },
    "dispute": {
      "open": {
        "label": "Ouvert",
        "tone": "red"
      },
      "investigating": {
        "label": "En instruction",
        "tone": "amber"
      },
      "resolved": {
        "label": "Résolu",
        "tone": "green"
      },
      "closed": {
        "label": "Clos",
        "tone": "slate"
      }
    },
    "order": {
      "requested": {
        "label": "Demandé",
        "tone": "blue"
      },
      "negotiating": {
        "label": "Négociation",
        "tone": "amber"
      },
      "agreed": {
        "label": "Accord conclu",
        "tone": "green"
      },
      "in_progress": {
        "label": "En cours",
        "tone": "indigo"
      },
      "done": {
        "label": "Réalisé (à confirmer)",
        "tone": "amber"
      },
      "confirmed": {
        "label": "Confirmé",
        "tone": "green"
      },
      "declined": {
        "label": "Décliné",
        "tone": "red"
      },
      "cancelled": {
        "label": "Annulé",
        "tone": "slate"
      }
    },
    "listing": {
      "pending": {
        "label": "En modération",
        "tone": "amber"
      },
      "published": {
        "label": "Publiée",
        "tone": "green"
      },
      "rejected": {
        "label": "Refusée",
        "tone": "red"
      },
      "closed": {
        "label": "Close",
        "tone": "slate"
      }
    },
    "token": {
      "approved": {
        "label": "Validée",
        "tone": "green"
      },
      "pending": {
        "label": "En attente",
        "tone": "amber"
      },
      "rejected": {
        "label": "Rejetée",
        "tone": "red"
      }
    },
    "user": {
      "active": {
        "label": "Actif",
        "tone": "green"
      },
      "suspended": {
        "label": "Suspendu",
        "tone": "red"
      },
      "archived": {
        "label": "Archivé",
        "tone": "slate"
      }
    }
  },
  "labels": {
    "requestKind": {
      "catalogue": "Don du catalogue",
      "personnalise": "Don personnel",
      "action": "Action / visite",
      "recurrent": "Engagement récurrent"
    },
    "paymentMode": {
      "immediate": "Paiement immédiat",
      "prefinance": "Préfinancement",
      "echeance": "Paiement à chaque échéance"
    },
    "recurrence": {
      "none": "Ponctuel",
      "weekly": "Hebdomadaire",
      "monthly": "Mensuel",
      "quarterly": "Trimestriel"
    },
    "category": {
      "nourriture": "Nourriture",
      "betail": "Bétail",
      "volaille": "Volailles",
      "habits": "Habits",
      "fournitures": "Fournitures",
      "equipements": "Équipements",
      "action": "Actions & visites",
      "autre": "Autre"
    },
    "providerCategory": {
      "officiant": "Officiant religieux",
      "commerce": "Commerce / approvisionnement",
      "traiteur": "Service traiteur",
      "transport": "Chauffeur / transporteur",
      "photo": "Photo / vidéo",
      "autre": "Autre service"
    },
    "proofType": {
      "photo": "Photo",
      "video": "Vidéo",
      "receipt": "Reçu",
      "invoice": "Facture",
      "attestation": "Attestation"
    },
    "payMethod": {
      "mobile_money": "Mobile Money (T-Money, Flooz…)",
      "card": "Carte bancaire",
      "bank": "Virement bancaire",
      "cash": "Espèces",
      "exchange": "Échange de services"
    },
    "listingType": {
      "don": "Don",
      "action": "Action solidaire",
      "crowdfunding": "Financement participatif"
    },
    "annex": {
      "packaging": "Personnalisation du packaging",
      "officiant": "Intervention d'un officiant religieux",
      "photos": "Prises de photos",
      "video": "Reportage photo / vidéo"
    },
    "role": {
      "donor": "Donateur",
      "client": "Client",
      "provider": "Prestataire",
      "partner": "Partenaire",
      "beneficiary": "Bénéficiaire",
      "subadmin": "Administrateur secondaire",
      "admin": "Administrateur"
    },
    "permission": {
      "requests.manage": "Traiter les demandes (accepter / refuser / demander des infos)",
      "users.manage": "Gérer les utilisateurs (suspension, archivage)",
      "providers.manage": "Gérer les prestataires et leurs services",
      "beneficiaries.view": "Consulter / gérer les bénéficiaires (données personnelles)",
      "missions.manage": "Créer, attribuer et réattribuer les missions",
      "proofs.review": "Contrôler les preuves de remise",
      "payments.manage": "Suivre et valider les paiements",
      "finances.view": "Consulter coûts, marges et finances globales",
      "disputes.manage": "Traiter les litiges",
      "stats.view": "Consulter les statistiques",
      "stats.export": "Exporter les rapports",
      "tokens.manage": "Gérer les règles et opérations Token Mena",
      "catalog.manage": "Gérer le catalogue et les tarifs",
      "listings.moderate": "Modérer les publications partenaires et campagnes",
      "audit.view": "Consulter le journal d'audit",
      "roles.manage": "Gérer les rôles et permissions (administrateur principal)",
      "settings.manage": "Modifier la configuration de la plateforme"
    }
  },
  "selfRegisterRoles": [
    "donor",
    "client",
    "provider",
    "partner",
    "beneficiary"
  ]
} as const;
