package nrw.heilmann.quarkus.bot.listeners;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nrw.heilmann.quarkus.bot.commands.SlashCommand;
import nrw.heilmann.quarkus.bot.persistence.model.PenaltyType;
import nrw.heilmann.quarkus.bot.persistence.repository.CommandPermissionRepository;
import nrw.heilmann.quarkus.bot.persistence.repository.PenaltyTypeRepository;

import java.util.List;

@ApplicationScoped
public class GuildReadyListener extends ListenerAdapter {

	@Any
	Instance<SlashCommand> slashCommands;

	@Inject
	PenaltyTypeRepository penaltyTypeRepository;

	@Inject
	CommandPermissionRepository commandPermissionRepository;

	@Override
	public void onGuildReady(@Nonnull GuildReadyEvent event) {
		long guildId = event.getGuild().getIdLong();
		provideDefaultPenaltyTypes(guildId);
		provideDefaultCommandPermissions(guildId);
	}

	private void provideDefaultPenaltyTypes(long guildId) {
		List<PenaltyType> allPenaltyTypes = penaltyTypeRepository.findByGuild(guildId);
		if (allPenaltyTypes.isEmpty()) {
			penaltyTypeRepository.persistIfAbsent(guildId, "teamkill", "Teamkill", /* defaultType= */ true);
		}
	}

	private void provideDefaultCommandPermissions(long guildId) {
		for (String commandName : slashCommands.stream().map(SlashCommand::getName).toList()) {
			commandPermissionRepository.persistIfAbsent(guildId, commandName);
		}
	}
}
