package nrw.heilmann.quarkus.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
import nrw.heilmann.quarkus.bot.permissions.RequiresCommandPermission;
import nrw.heilmann.quarkus.persistence.model.PenaltyType;
import nrw.heilmann.quarkus.persistence.repository.PenaltyTypeRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiresCommandPermission
@ApplicationScoped
public class ReportPenaltyCommand extends SlashCommand {

	public static final String MODAL_ID_PREFIX = "report-penalty:";
	public static final String FIELD_MEMBER = "modal-member";
	public static final String FIELD_AMOUNT = "modal-amount";
	public static final String FIELD_REASON = "modal-reason";

	private static final String ERROR_NOT_ON_GUILD = "Command wasn't executed inside a server.";

	@Inject
	PenaltyTypeRepository penaltyTypeRepository;

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

			List<PenaltyType> penaltyTypes = penaltyTypeRepository.findByGuild(guildId);
			List<SelectOption> allPenaltyTypes = penaltyTypes.stream()
					.map(pt -> SelectOption.of(pt.getDisplayName(), pt.getTechnicalName()))
					.toList();
			Optional<PenaltyType> defaultPenaltyType = penaltyTypes.stream()
					.filter(PenaltyType::isDefaultType)
					.findFirst();

			StringSelectMenu.Builder reasonSelectBuilder = StringSelectMenu.create(FIELD_REASON)
					.addOptions(allPenaltyTypes)
					.setRequired(true);

			if (defaultPenaltyType.isPresent()) {
				reasonSelectBuilder.setDefaultValues(defaultPenaltyType.get().getTechnicalName());
			} else {
				reasonSelectBuilder.setPlaceholder("No default value configured - please select manually");
			}

			StringSelectMenu reasonSelect = reasonSelectBuilder.build();

			Modal modal = Modal.create(MODAL_ID_PREFIX + UUID.randomUUID(), "Report Penalty")
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
