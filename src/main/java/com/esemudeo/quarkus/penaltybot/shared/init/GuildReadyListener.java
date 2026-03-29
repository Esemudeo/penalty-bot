package com.esemudeo.quarkus.penaltybot.shared.init;

import com.esemudeo.quarkus.penaltybot.shared.command.GuildCommand;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.esemudeo.quarkus.penaltybot.configuration.commandpermission.repository.CommandRepository;
import com.esemudeo.quarkus.penaltybot.configuration.penaltytype.repository.PenaltyTypeRepository;

@ApplicationScoped
public class GuildReadyListener extends ListenerAdapter {

	@Any
	Instance<GuildCommand> guildCommands;

	@Inject
	PenaltyTypeRepository penaltyTypeRepository;

	@Inject
	CommandRepository commandPermissionRepository;

	@Override
	public void onGuildReady(@Nonnull GuildReadyEvent event) {
		initializeGuildDefaults(event.getGuild().getIdLong());
	}

	@Override
	public void onGuildJoin(@Nonnull GuildJoinEvent event) {
		initializeGuildDefaults(event.getGuild().getIdLong());
	}

	private void initializeGuildDefaults(long guildId) {
		provideDefaultPenaltyTypes(guildId);
		provideDefaultCommandPermissions(guildId);
	}

	private void provideDefaultPenaltyTypes(long guildId) {
		penaltyTypeRepository.createDefaultIfNoneExist(guildId);
	}

	private void provideDefaultCommandPermissions(long guildId) {
		for (String commandName : guildCommands.stream().map(GuildCommand::getName).toList()) {
			commandPermissionRepository.persistIfAbsent(guildId, commandName);
		}
	}
}