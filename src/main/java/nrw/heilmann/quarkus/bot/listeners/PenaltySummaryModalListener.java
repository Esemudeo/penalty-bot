package nrw.heilmann.quarkus.bot.listeners;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import nrw.heilmann.quarkus.bot.commands.PenaltySummaryCommand;
import nrw.heilmann.quarkus.persistence.model.GlobalGuildConfig;
import nrw.heilmann.quarkus.persistence.repository.GlobalGuildConfigRepository;
import nrw.heilmann.quarkus.persistence.repository.PenaltyRepository;
import nrw.heilmann.quarkus.persistence.repository.PenaltyRepository.PenaltyTypeSummary;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class PenaltySummaryModalListener extends ModalListener implements YearMonthModalTrait {

	private static final int MAX_MESSAGE_LENGTH = 1900;

	@Inject
	PenaltyRepository penaltyRepository;
	@Inject
	GlobalGuildConfigRepository globalGuildConfigRepository;

	@Override
	protected String getModalIdPrefix() {
		return PenaltySummaryCommand.MODAL_ID_PREFIX;
	}

	@Override
	protected void handleModalInteraction(ModalInteractionEvent event, @Nonnull Guild validatedGuild) {
		YearMonth yearMonth = validateYearMonth(event, PenaltySummaryCommand.FIELD_MONTH).orElse(null);
		if (yearMonth == null) {
			return;
		}

		long guildId = validatedGuild.getIdLong();
		Map<Long, List<PenaltyTypeSummary>> summaryByUser = penaltyRepository.aggregateByMonthForAllUsers(guildId, yearMonth);

		if (summaryByUser.isEmpty()) {
			String monthLabel = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + yearMonth.getYear();
			event.reply("No penalties found for %s.".formatted(monthLabel)).setEphemeral(true).queue();
			return;
		}

		Optional<String> paypalUsername = globalGuildConfigRepository.findByGuild(guildId)
				.map(GlobalGuildConfig::getPaypalMeUsername);

		List<String> userBlocks = buildUserBlocks(summaryByUser, paypalUsername);
		List<String> messages = chunkIntoMessages(userBlocks);

		event.reply(messages.getFirst()).queue(hook ->
				messages.stream().skip(1).forEach(msg -> hook.sendMessage(msg).queue()));
	}

	private List<String> buildUserBlocks(Map<Long, List<PenaltyTypeSummary>> summaryByUser, Optional<String> paypalUsername) {
		List<String> blocks = new ArrayList<>();
		for (Map.Entry<Long, List<PenaltyTypeSummary>> entry : summaryByUser.entrySet()) {
			long userId = entry.getKey();
			List<PenaltyTypeSummary> summaries = entry.getValue();

			StringBuilder block = new StringBuilder();
			block.append("<@").append(userId).append(">\n");
			summaries.forEach(s -> block.append("- ").append(s.displayName()).append(": ").append(s.totalAmount()).append("x\n"));

			int totalCents = summaries.stream()
					.filter(s -> s.totalPriceCents() != null)
					.mapToInt(PenaltyTypeSummary::totalPriceCents)
					.sum();

			if (totalCents > 0) {
				String euroAmount = "%.2f".formatted(totalCents / 100.0);
				block.append("Total: €").append(euroAmount);
				paypalUsername.ifPresent(username ->
						block.append(" → https://paypal.me/").append(username).append("/").append(euroAmount).append("EUR"));
				block.append("\n");
			}

			blocks.add(block.toString());
		}
		return blocks;
	}

	private List<String> chunkIntoMessages(List<String> userBlocks) {
		List<String> messages = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		for (String block : userBlocks) {
			if (!current.isEmpty() && current.length() + block.length() >= MAX_MESSAGE_LENGTH) {
				messages.add(current.toString().strip());
				current = new StringBuilder();
			}
			current.append(block).append("\n");
		}
		if (!current.isEmpty()) {
			messages.add(current.toString().strip());
		}
		return messages;
	}
}
