package nrw.heilmann.quarkus.bot.listeners;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nrw.heilmann.quarkus.bot.commands.SlashCommand;

@ApplicationScoped
public class SlashCommandListener extends ListenerAdapter {

	@Any
	Instance<SlashCommand> slashCommands;

	@Override
	public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
		slashCommands.stream()
				.filter(command -> command.getName().equals(event.getName()))
				.findFirst()
				.ifPresent(command -> command.handleCommand(event));

	}
}
