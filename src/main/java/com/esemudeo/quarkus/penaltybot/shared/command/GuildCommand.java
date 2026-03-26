package com.esemudeo.quarkus.penaltybot.shared.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import com.esemudeo.quarkus.penaltybot.shared.exception.NotInGuildException;

import java.util.function.Consumer;

public interface GuildCommand {

	String getName();

	@FunctionalInterface
	interface GuildAction {
		void execute(Guild guild) throws Exception;
	}

	default void executeInGuild(GenericCommandInteractionEvent event, GuildAction action) {
		executeInGuild(event, action, null);
	}

	default void executeInGuild(GenericCommandInteractionEvent event, GuildAction action, Consumer<Exception> onError) {
		try {
			Guild guild = event.getGuild();
			if (guild == null) {
				throw new NotInGuildException();
			}
			action.execute(guild);
		} catch (NotInGuildException e) {
			event.reply("Command wasn't executed inside a server.").setEphemeral(true).queue();
		} catch (Exception e) {
			event.reply("An error occurred while processing the command.").setEphemeral(true).queue();
			if (onError != null) {
				onError.accept(e);
			}
		}
	}
}