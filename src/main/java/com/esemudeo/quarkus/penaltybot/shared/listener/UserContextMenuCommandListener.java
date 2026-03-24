package com.esemudeo.quarkus.penaltybot.shared.listener;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.esemudeo.quarkus.penaltybot.shared.command.UserContextMenuCommand;

@ApplicationScoped
public class UserContextMenuCommandListener extends ListenerAdapter {

	@Any
	Instance<UserContextMenuCommand> contextMenuCommands;

	@Override
	public void onUserContextInteraction(@Nonnull UserContextInteractionEvent event) {
		contextMenuCommands.stream()
				.filter(command -> command.getContextMenuName().equals(event.getName()))
				.findFirst()
				.ifPresent(command -> command.handleContextMenuCommand(event));
	}
}