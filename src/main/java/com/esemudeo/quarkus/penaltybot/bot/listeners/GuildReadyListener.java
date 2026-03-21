package com.esemudeo.quarkus.penaltybot.bot.listeners;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.esemudeo.quarkus.penaltybot.bot.commands.SlashCommand;
import com.esemudeo.quarkus.penaltybot.persistence.repository.CommandRepository;
import com.esemudeo.quarkus.penaltybot.persistence.repository.PenaltyTypeRepository;

@ApplicationScoped
public class GuildReadyListener extends ListenerAdapter {

	@Any
	Instance<SlashCommand> slashCommands;

	@Inject
	PenaltyTypeRepository penaltyTypeRepository;

	@Inject
	CommandRepository commandPermissionRepository;

	@Override
	public void onGuildReady(@Nonnull GuildReadyEvent event) {
		long guildId = event.getGuild().getIdLong();
		provideDefaultPenaltyTypes(guildId);
		provideDefaultCommandPermissions(guildId);
	}

	private void provideDefaultPenaltyTypes(long guildId) {
		penaltyTypeRepository.createDefaultIfNoneExist(guildId);
	}

	private void provideDefaultCommandPermissions(long guildId) {
		for (String commandName : slashCommands.stream().map(SlashCommand::getName).toList()) {
			commandPermissionRepository.persistIfAbsent(guildId, commandName);
		}
	}
}
