package com.esemudeo.quarkus.penaltybot.penalty.listener;

import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class ModalListener extends ListenerAdapter {

	protected abstract String getModalIdPrefix();

	protected abstract void handleModalInteraction(ModalInteractionEvent event, @Nonnull Guild guild);

	@Override
	public final void onModalInteraction(@Nonnull ModalInteractionEvent event) {
		if (!event.getModalId().startsWith(getModalIdPrefix())) {
			return;
		}
		Guild guild = event.getGuild();
		if (guild == null) {
			event.reply("This command can only be used inside a server.").setEphemeral(true).queue();
			return;
		}
		handleModalInteraction(event, guild);
	}
}