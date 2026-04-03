package com.esemudeo.quarkus.penaltybot.penalty.listener;

import com.esemudeo.quarkus.penaltybot.permission.PermissionService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface MemberModalTrait {

	String PENALTY_COMMAND_NAME = "penalty";

	default Optional<Member> validateMember(ModalInteractionEvent event, String fieldId) {
		List<Member> members = Objects.requireNonNull(event.getValue(fieldId))
				.getAsMentions().getMembers();
		if (members.isEmpty()) {
			event.reply("No member selected.").setEphemeral(true).queue();
			return Optional.empty();
		}
		return Optional.of(members.getFirst());
	}

	default boolean hasNoPenaltyReportPermission(ModalInteractionEvent event, Guild guild, Member member,
	                                             PermissionService permissionService) {
		if (!permissionService.isAllowedForCommand(guild, member, PENALTY_COMMAND_NAME)) {
			event.reply("The selected member does not have the required role to be reported.")
					.setEphemeral(true).queue();
			return true;
		}
		return false;
	}
}
