package com.esemudeo.quarkus.penaltybot.shared.command;

import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public interface UserContextMenuCommand extends GuildCommand {
	String getName();

	String getContextMenuName();

	default CommandData toContextMenuCommandData() {
		return Commands.user(getContextMenuName());
	}

	void handleContextMenuCommand(UserContextInteractionEvent event);
}