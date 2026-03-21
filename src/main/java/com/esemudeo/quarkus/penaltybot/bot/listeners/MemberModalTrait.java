package com.esemudeo.quarkus.penaltybot.bot.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface MemberModalTrait {

	default Optional<Member> validateMember(ModalInteractionEvent event, String fieldId) {
		List<Member> members = Objects.requireNonNull(event.getValue(fieldId))
				.getAsMentions().getMembers();
		if (members.isEmpty()) {
			event.reply("No member selected.").setEphemeral(true).queue();
			return Optional.empty();
		}
		return Optional.of(members.getFirst());
	}
}
