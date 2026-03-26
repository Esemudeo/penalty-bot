package com.esemudeo.quarkus.penaltybot.shared.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public interface SlashCommand extends GuildCommand {
	String getHelpDescription();

	default CommandData toSlashCommandData() {
		return Commands.slash(getName(), getHelpDescription());
	}

	void handleSlashCommand(SlashCommandInteractionEvent event);
}