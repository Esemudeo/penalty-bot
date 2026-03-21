package com.esemudeo.quarkus.penaltybot.bot.listeners;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import com.esemudeo.quarkus.penaltybot.bot.commands.ReportPenaltyCommand;
import com.esemudeo.quarkus.penaltybot.persistence.model.Penalty;
import com.esemudeo.quarkus.penaltybot.persistence.repository.PenaltyRepository;

import java.time.Instant;
import java.util.Objects;

@ApplicationScoped
public class ReportPenaltyModalListener extends ModalListener implements MemberModalTrait {

	@Inject
	PenaltyRepository penaltyRepository;

	@Override
	protected String getModalIdPrefix() {
		return ReportPenaltyCommand.MODAL_ID_PREFIX;
	}

	@Override
	protected void handleModalInteraction(ModalInteractionEvent event, @Nonnull Guild guild) {
		Member affectedMember = validateMember(event, ReportPenaltyCommand.FIELD_MEMBER).orElse(null);
		if (affectedMember == null)
			return;

		String amountRaw = Objects.requireNonNull(event.getValue(ReportPenaltyCommand.FIELD_AMOUNT)).getAsString();
		int amount;
		try {
			amount = Integer.parseInt(amountRaw);
			if (amount == 0) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			event.reply("Invalid amount: " + amountRaw).setEphemeral(true).queue();
			return;
		}

		String penaltyTypeName = Objects.requireNonNull(event.getValue(ReportPenaltyCommand.FIELD_REASON)).getAsStringList().getFirst();

		Penalty penaltyDraft = Penalty.builder()
				.timestamp(Instant.now())
				.guildId(guild.getIdLong())
				.authorId(event.getUser().getIdLong())
				.affectedMemberId(affectedMember.getIdLong())
				.amount(amount)
				.build();

		Penalty penalty = penaltyRepository.save(penaltyDraft, penaltyTypeName).orElse(null);
		if (penalty == null) {
			event.reply("Unknown penalty type: " + penaltyTypeName).setEphemeral(true).queue();
			return;
		}

		event.reply("Reported %d x %s for %s.".formatted(amount, penalty.getPenaltyType().getDisplayName(), affectedMember.getEffectiveName()))
				.queue();
	}
}
