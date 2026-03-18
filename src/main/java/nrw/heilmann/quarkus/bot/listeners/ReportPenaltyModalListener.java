package nrw.heilmann.quarkus.bot.listeners;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nrw.heilmann.quarkus.bot.commands.ReportPenaltyCommand;
import nrw.heilmann.quarkus.bot.persistence.model.Penalty;
import nrw.heilmann.quarkus.bot.persistence.repository.PenaltyRepository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ReportPenaltyModalListener extends ListenerAdapter {

	@Inject
	PenaltyRepository penaltyRepository;

	@Override
	public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
		if (!event.getModalId().startsWith(ReportPenaltyCommand.MODAL_ID_PREFIX)) {
			return;
		}

		Guild guild = event.getGuild();
		if (guild == null) {
			event.reply("This command can only be used inside a server.").setEphemeral(true).queue();
			return;
		}

		List<User> selectedUsers = Objects.requireNonNull(event.getValue(ReportPenaltyCommand.FIELD_MEMBER)).getAsMentions().getUsers();
		if (selectedUsers.isEmpty()) {
			event.reply("No member selected.").setEphemeral(true).queue();
			return;
		}

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
		User affectedMember = selectedUsers.getFirst();

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
