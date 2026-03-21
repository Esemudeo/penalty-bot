package com.esemudeo.quarkus.penaltybot.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.esemudeo.quarkus.penaltybot.persistence.model.Command;
import com.esemudeo.quarkus.penaltybot.persistence.model.GlobalGuildConfig;
import com.esemudeo.quarkus.penaltybot.persistence.model.PenaltyType;
import com.esemudeo.quarkus.penaltybot.persistence.repository.CommandRepository;
import com.esemudeo.quarkus.penaltybot.persistence.repository.GlobalGuildConfigRepository;
import com.esemudeo.quarkus.penaltybot.persistence.repository.PenaltyTypeRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service layer between the Vaadin settings UI and the repositories.
 * The guildId is always sourced from the authenticated session and never accepted from callers.
 * For single-entity operations the guildId is also included in the WHERE clause, so a
 * manipulated entity ID from a different guild silently affects nothing.
 */
@ApplicationScoped
public class SettingsService {

    @Inject
    AuthSession authSession;

    @Inject
    PenaltyTypeRepository penaltyTypeRepository;

    @Inject
    CommandRepository commandRepository;

    @Inject
    GlobalGuildConfigRepository globalGuildConfigRepository;

    // --- Penalty types ---

    public List<PenaltyType> getPenaltyTypes() {
        return penaltyTypeRepository.findAllByGuild(guildId());
    }

    public PenaltyType createPenaltyType(String displayName, boolean isDefault, Integer price) {
        return penaltyTypeRepository.create(guildId(), displayName, isDefault, price);
    }

    public void updatePenaltyType(long id, String displayName, boolean isDefault, Integer price) {
        penaltyTypeRepository.update(id, guildId(), displayName, isDefault, price);
    }

    public void deactivatePenaltyType(long id) {
        penaltyTypeRepository.deactivate(id, guildId());
    }

    // --- Command permissions ---

    public List<Command> getCommands() {
        return commandRepository.findAllByGuild(guildId());
    }

    public void updateMinRole(String commandName, Long minRoleId) {
        commandRepository.updateMinRole(guildId(), commandName, minRoleId);
    }

    public void setExplicitRoles(String commandName, List<Long> roleIds) {
        commandRepository.setExplicitRoles(guildId(), commandName, roleIds);
    }

    // --- Global guild config ---

    public Optional<GlobalGuildConfig> getGlobalConfig() {
        return globalGuildConfigRepository.findByGuild(guildId());
    }

    public void updatePaypalMeUsername(String username) {
        globalGuildConfigRepository.upsertPaypalMeUsername(guildId(), username);
    }

    // ---

    private long guildId() {
        if (!authSession.isAuthenticated()) {
            throw new SettingsAccessDeniedException("Not authenticated");
        }
        return authSession.getGuildId();
    }
}