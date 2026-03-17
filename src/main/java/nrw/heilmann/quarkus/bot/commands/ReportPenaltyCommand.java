package nrw.heilmann.quarkus.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;
import nrw.heilmann.quarkus.bot.exceptions.NotInGuildException;
import nrw.heilmann.quarkus.bot.persistence.PenaltyType;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ReportPenaltyCommand extends SlashCommand {

	private static final String ERROR_NOT_ON_GUILD = "Command wasn't executed inside a server.";
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
	@Transactional
	public void handleCommand(SlashCommandInteractionEvent event) {
		try {
			long guildId = resolveGuildId(event);

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

			List<SelectOption> allPenaltyTypes =
					PenaltyType.<PenaltyType>find("guildId = ?1", guildId).stream()
							.map(pt -> SelectOption.of(pt.getDisplayName(), pt.getTechnicalName()))
							.toList();
			StringSelectMenu reasonSelect = StringSelectMenu.create(FIELD_REASON)
					.setPlaceholder("Select a reason")
					.addOptions(allPenaltyTypes)
					.setDefaultValues("teamkill")
					.setRequired(true)
					.build();

			Modal modal = Modal.create(UUID.randomUUID().toString(), "Report Penalty")
					.addComponents(
							Label.of("Affected Member", memberSelect),
							Label.of("Amount", amountInput),
							Label.of("Reason", reasonSelect))
					.build();

			event.replyModal(modal).queue();

		} catch (NotInGuildException e) {
			replyAsNotInGuild(event);
		} catch (Exception e) {
			event.reply("An error occurred while processing the command.").setEphemeral(true).queue();
		}
	}


	private static long resolveGuildId(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		if (guild == null) {
			throw new NotInGuildException();
		}
		return guild.getIdLong();
	}

	private static void replyAsNotInGuild(SlashCommandInteractionEvent event) {
		event.reply(ERROR_NOT_ON_GUILD).setEphemeral(true).queue();
	}
}
