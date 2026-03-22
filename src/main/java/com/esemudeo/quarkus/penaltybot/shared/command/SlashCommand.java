package com.esemudeo.quarkus.penaltybot.shared.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public abstract class SlashCommand {
	public abstract String getName();

	protected abstract String getDescription();

	public final CommandData toCommandData() {
		return Commands.slash(getName(), getDescription());
	}

	public abstract void handleCommand(SlashCommandInteractionEvent event);
}