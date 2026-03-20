package nrw.heilmann.quarkus.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;
import nrw.heilmann.quarkus.bot.exceptions.NotInGuildException;
import nrw.heilmann.quarkus.bot.permissions.RequiresCommandPermission;
import nrw.heilmann.quarkus.persistence.repository.PenaltyRepository;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RequiresCommandPermission
@ApplicationScoped
public class PenaltySummaryCommand extends SlashCommand {

	public static final String MODAL_ID_PREFIX = "penalty-summary:";
	public static final String FIELD_MONTH = "modal-month";
	private static final int SHOW_FOR_LAST_X_MONTHS = 6;

	@Inject
	PenaltyRepository penaltyRepository;

	@Override
	public String getName() {
		return "penalty-summary";
	}

	@Override
	protected String getDescription() {
		return "Show a penalty summary for all members in a specific month.";
	}

	@Override
	public void handleCommand(SlashCommandInteractionEvent event) {
		try {
			Guild guild = event.getGuild();
			if (guild == null) {
				throw new NotInGuildException();
			}

			List<YearMonth> availableMonths = penaltyRepository.findAvailableMonthsForGuild(guild.getIdLong(), SHOW_FOR_LAST_X_MONTHS);
			if (availableMonths.isEmpty()) {
				event.reply("No penalties found for the last %d months.".formatted(SHOW_FOR_LAST_X_MONTHS)).setEphemeral(true).queue();
				return;
			}

			List<SelectOption> monthOptions = availableMonths.stream()
					.map(ym -> SelectOption.of(
							ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + ym.getYear(),
							ym.toString()))
					.toList();

			StringSelectMenu monthSelect = StringSelectMenu.create(FIELD_MONTH)
					.setPlaceholder("Select a month")
					.addOptions(monthOptions)
					.setDefaultValues(monthOptions.getFirst().getValue())
					.setRequired(true)
					.build();

			Modal modal = Modal.create(MODAL_ID_PREFIX + UUID.randomUUID(), "Penalty Summary")
					.addComponents(Label.of("Month", monthSelect))
					.build();

			event.replyModal(modal).queue();
		} catch (NotInGuildException e) {
			event.reply("Command wasn't executed inside a server.").setEphemeral(true).queue();
		} catch (Exception e) {
			event.reply("An error occurred while processing the command.").setEphemeral(true).queue();
		}
	}
}
