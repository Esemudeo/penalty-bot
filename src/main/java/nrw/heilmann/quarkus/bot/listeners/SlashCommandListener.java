package nrw.heilmann.quarkus.bot.listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandListener extends ListenerAdapter {
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if ("ping".equals(event.getName())) {
			event.reply(event.getOption("message").getAsString() + " yourself!").queue();
		}
	}
}
