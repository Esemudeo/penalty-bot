package com.esemudeo.quarkus.penaltybot.penalty.command;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;
import com.esemudeo.quarkus.penaltybot.shared.command.SlashCommand;
import com.esemudeo.quarkus.penaltybot.shared.command.UserContextMenuCommand;
import com.esemudeo.quarkus.penaltybot.permission.RequiresCommandPermission;
import com.esemudeo.quarkus.penaltybot.penalty.repository.PenaltyRepository;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RequiresCommandPermission
@ApplicationScoped
public class ShowPenaltiesCommand implements SlashCommand, UserContextMenuCommand {

	public static final String MODAL_ID_PREFIX = "show-penalties:";
	public static final String FIELD_MEMBER = "modal-member";
	public static final String FIELD_MONTH = "modal-month";
	private static final int SHOW_FOR_LAST_X_MONTHS = 6;

	@Inject
	PenaltyRepository penaltyRepository;

	@Override
	public String getName() {
		return "penalty-show";
	}

	@Override
	public String getHelpDescription() {
		return "Show penalties for a member and a specific month.";
	}

	@Override
	public String getContextMenuName() {
		return "Show penalties for month";
	}

	@Override
	public void handleSlashCommand(SlashCommandInteractionEvent event) {
		executeInGuild(event, guild -> openModal(guild, event.getUser().getIdLong(), event));
	}

	@Override
	public void handleContextMenuCommand(UserContextInteractionEvent event) {
		executeInGuild(event, guild -> openModal(guild, event.getTarget().getIdLong(), event));
	}

	private void openModal(Guild guild, long preselectedUserId, GenericCommandInteractionEvent event) {
		List<YearMonth> availableMonths = penaltyRepository.findAvailableMonthsForGuild(guild.getIdLong(), SHOW_FOR_LAST_X_MONTHS);
		if (availableMonths.isEmpty()) {
			event.reply("No penalties found for the last %d months.".formatted(SHOW_FOR_LAST_X_MONTHS)).setEphemeral(true).queue();
			return;
		}

		EntitySelectMenu memberSelect = EntitySelectMenu.create(FIELD_MEMBER, EntitySelectMenu.SelectTarget.USER)
				.setPlaceholder("Select a member")
				.setDefaultValues(EntitySelectMenu.DefaultValue.user(preselectedUserId))
				.setRequired(true)
				.build();

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

		Modal modal = Modal.create(MODAL_ID_PREFIX + UUID.randomUUID(), "Show Penalties")
				.addComponents(
						Label.of("Member", memberSelect),
						Label.of("Month", monthSelect))
				.build();

		event.replyModal(modal).queue();
	}
}