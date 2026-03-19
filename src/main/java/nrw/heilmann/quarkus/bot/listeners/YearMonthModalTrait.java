package nrw.heilmann.quarkus.bot.listeners;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

public interface YearMonthModalTrait {

	default Optional<YearMonth> validateYearMonth(ModalInteractionEvent event, String fieldId) {
		String raw = Objects.requireNonNull(event.getValue(fieldId)).getAsStringList().getFirst();
		try {
			return Optional.of(YearMonth.parse(raw));
		} catch (DateTimeParseException e) {
			event.reply("Invalid month: " + raw).setEphemeral(true).queue();
			return Optional.empty();
		}
	}
}
