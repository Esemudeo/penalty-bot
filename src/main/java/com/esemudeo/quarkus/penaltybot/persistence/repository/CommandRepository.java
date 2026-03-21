package com.esemudeo.quarkus.penaltybot.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import com.esemudeo.quarkus.penaltybot.persistence.model.Command;
import com.esemudeo.quarkus.penaltybot.persistence.model.CommandExplicitRole;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class CommandRepository {

    public Optional<Command> findByGuildAndCommand(long guildId, String commandName) {
        return Command.find("guildId = ?1 and commandName = ?2", guildId, commandName).firstResultOptional();
    }

    /** All command permission entries for a guild – used by the settings UI. */
    public List<Command> findAllByGuild(long guildId) {
        return Command.find("guildId = ?1", guildId).list();
    }

    public void persistIfAbsent(long guildId, String commandName) {
        if (findByGuildAndCommand(guildId, commandName).isEmpty()) {
            Command.builder()
                    .guildId(guildId)
                    .commandName(commandName)
                    .build()
                    .persist();
        }
    }

    /**
     * Sets the minimum role required to use a command.
     * Pass {@code null} to remove the requirement.
     */
    public void updateMinRole(long guildId, String commandName, Long minRoleId) {
        Command.update("minRoleId = ?1 where guildId = ?2 and commandName = ?3",
                minRoleId, guildId, commandName);
    }

    /**
     * Replaces the explicit role whitelist for a command.
     * Existing roles not in the new list are deleted (orphanRemoval).
     */
    public void setExplicitRoles(long guildId, String commandName, List<Long> roleIds) {
        Command command = findByGuildAndCommand(guildId, commandName).orElse(null);
        if (command == null) {
            return;
        }
        command.getExplicitRoles().clear();
        roleIds.forEach(roleId -> command.getExplicitRoles().add(
                CommandExplicitRole.builder()
                        .roleId(roleId)
                        .build()
        ));
    }
}