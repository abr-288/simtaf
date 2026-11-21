package com.menewa.identity;

import java.util.Map;

/**
 * Événements publiés par le module identity. Les autres modules (audit, récompenses, prestataires)
 * s'y abonnent : identity ne dépend ainsi d'aucun module métier.
 */
public final class IdentityEvents {
    private IdentityEvents() {}

    /** Nouveau compte créé après vérification de l'OTP d'inscription. */
    public record UserRegistered(Long userId, String role) {}

    /** Connexion réussie. */
    public record UserLoggedIn(Long userId, String role) {}

    /** Déconnexion volontaire (session supprimée). */
    public record UserLoggedOut(Long userId, String role) {}

    /** Rôle modifié par l'administration (les permissions sont dans `details`). */
    public record UserRoleChanged(Long userId, String fromRole, String toRole) {}

    /** Action d'administration à inscrire au journal d'audit. */
    public record IdentityAudited(Long actorId, String actorRole, String action, String entity, String entityId,
            Map<String, Object> details) {}
}
