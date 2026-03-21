package com.esemudeo.quarkus.penaltybot.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import com.esemudeo.quarkus.penaltybot.JDAInstance;
import com.esemudeo.quarkus.penaltybot.persistence.model.Command;
import com.esemudeo.quarkus.penaltybot.persistence.model.GlobalGuildConfig;
import com.esemudeo.quarkus.penaltybot.persistence.model.PenaltyType;
import com.esemudeo.quarkus.penaltybot.persistence.repository.CommandRepository;
import com.esemudeo.quarkus.penaltybot.persistence.repository.GlobalGuildConfigRepository;
import com.esemudeo.quarkus.penaltybot.persistence.repository.PenaltyTypeRepository;
import net.dv8tion.jda.api.entities.Guild;

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

    @Inject
    JDAInstance jdaInstance;

    // --- Guild roles ---

    public record GuildRole(long id, String name) {}

    public List<GuildRole> getGuildRoles() {
        Guild guild = jdaInstance.getJda().getGuildById(guildId());
        if (guild == null) {
            return List.of();
        }
        // getRoles() returns roles sorted highest to lowest by position
        return guild.getRoles().stream()
                .map(r -> new GuildRole(r.getIdLong(), r.getName()))
                .toList();
    }

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

    public void makePenaltyTypeUnusable(long id) {
        long guildId = guildId();
        try {
            penaltyTypeRepository.delete(id, guildId);
        } catch (PersistenceException e) {
            penaltyTypeRepository.deactivate(id, guildId);
        }
    }

    // --- Command permissions ---

    public List<Command> getCommands() {
        return commandRepository.findAllByGuild(guildId());
    }

    public void updateCommandMinRole(String commandName, Long minRoleId) {
        commandRepository.updateMinRole(guildId(), commandName, minRoleId);
    }

    public void replaceCommandExplicitRoles(String commandName, List<Long> roleIds) {
        commandRepository.setExplicitRoles(guildId(), commandName, roleIds);
    }

    // --- Global guild config ---

    public Optional<GlobalGuildConfig> getGlobalConfig() {
        return globalGuildConfigRepository.findByGuild(guildId());
    }

    public void updatePaypalMeUsername(String paypalUsername) {
        globalGuildConfigRepository.upsertPaypalMeUsername(guildId(), paypalUsername);
    }

    // ---

    private long guildId() {
        if (authSession.isNotAuthenticated()) {
            throw new SettingsAccessDeniedException("Not authenticated");
        }
        return authSession.getGuildId();
    }
}
