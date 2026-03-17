package nrw.heilmann.quarkus.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

@ApplicationScoped
public class ReportPenaltyCommand extends SlashCommand {

	private static final String MODAL_ID = "penalty-modal";
	private static final String FIELD_MEMBER = "modal-member";
	private static final String FIELD_AMOUNT = "modal-amount";
	private static final String FIELD_REASON = "modal-reason";

	@Override
	public String getName() {
		return "penalty";
	}

	@Override
	protected String getDescription() {
		return "Report a penalty for a member.";
	}

	@Override
	public void handleCommand(SlashCommandInteractionEvent event) {
		EntitySelectMenu memberSelect = EntitySelectMenu.create(FIELD_MEMBER, EntitySelectMenu.SelectTarget.USER)
				.setPlaceholder("Select a member")
				.setDefaultValues(EntitySelectMenu.DefaultValue.user(event.getUser().getIdLong()))
				.setRequired(true)
				.build();

		TextInput amountInput = TextInput.create(FIELD_AMOUNT, TextInputStyle.SHORT)
				.setPlaceholder("1")
				.setValue("1")
				.setRequired(true)
				.build();

		StringSelectMenu reasonSelect = StringSelectMenu.create(FIELD_REASON)
				.setPlaceholder("Select a reason")
				.addOption("Teamkill", "teamkill")
				.addOption("Friendly Fire", "friendly-fire")
				.addOption("AFK", "afk")
				.setDefaultValues("teamkill")
				.setRequired(true)
				.build();

		Modal modal = Modal.create(MODAL_ID, "Report Penalty")
				.addComponents(
						Label.of("Affected Member", memberSelect),
						Label.of("Amount", amountInput),
						Label.of("Reason", reasonSelect))
				.build();

		event.replyModal(modal).queue();
	}
}
