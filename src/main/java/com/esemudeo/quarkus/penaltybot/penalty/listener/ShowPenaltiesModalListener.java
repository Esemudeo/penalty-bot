package com.esemudeo.quarkus.penaltybot.penalty.listener;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import com.esemudeo.quarkus.penaltybot.penalty.command.ShowPenaltiesCommand;
import com.esemudeo.quarkus.penaltybot.penalty.repository.PenaltyRepository;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class ShowPenaltiesModalListener extends ModalListener implements MemberModalTrait, YearMonthModalTrait {

	@Inject
	PenaltyRepository penaltyRepository;

	@Override
	protected String getModalIdPrefix() {
		return ShowPenaltiesCommand.MODAL_ID_PREFIX;
	}

	@Override
	protected void handleModalInteraction(ModalInteractionEvent event, @Nonnull Guild validatedGuild) {
		Member affectedMember = validateMember(event, ShowPenaltiesCommand.FIELD_MEMBER).orElse(null);
		if (affectedMember == null) {
			return;
		}

		YearMonth yearMonth = validateYearMonth(event, ShowPenaltiesCommand.FIELD_MONTH).orElse(null);
		if (yearMonth == null) {
			return;
		}

		Map<String, Integer> penalties = penaltyRepository.aggregateByMonth(validatedGuild.getIdLong(), yearMonth, affectedMember.getIdLong());
		String monthLabel = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + yearMonth.getYear();
		String penaltySummary = penalties.isEmpty()
				? "No penalties found."
				: penalties.entrySet().stream()
						.map(entry -> entry.getKey() + ": " + entry.getValue())
						.reduce((a, b) -> a + "\n" + b)
						.orElse("No penalties found.");
		event.reply("Penalties for %s in %s:\n%s".formatted(affectedMember.getEffectiveName(), monthLabel, penaltySummary))
				.queue();
	}
}
