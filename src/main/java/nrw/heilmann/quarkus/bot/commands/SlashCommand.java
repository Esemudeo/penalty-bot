package nrw.heilmann.quarkus.bot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public abstract class SlashCommand {
	public abstract String getName();

	protected abstract String getDescription();

	protected abstract List<OptionData> getOptions();

	public final CommandData toCommandData() {
		return Commands.slash(getName(), getDescription()).addOptions(getOptions());
	}

	public abstract void handleCommand(SlashCommandInteractionEvent event);
}
