package nrw.heilmann.quarkus.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import nrw.heilmann.quarkus.bot.permissions.RequiresCommandPermission;
import nrw.heilmann.quarkus.bot.persistence.repository.PenaltyRepository;

import java.time.YearMonth;
import java.util.Map;
import java.util.Objects;

@RequiresCommandPermission
@ApplicationScoped
public class ShowPenaltiesCommand extends SlashCommand {

	private final static String OPTION_MEMBER = "member";

	@Inject
	PenaltyRepository penaltyRepository;

	@Override
	public String getName() {
		return "penalty-show";
	}

	@Override
	protected String getDescription() {
		return "Show penalties for a member.";
	}

	@Override
	public void handleCommand(SlashCommandInteractionEvent event) {
		OptionMapping memberOption = event.getOption(OPTION_MEMBER);
		Member affectedMember = memberOption != null ? memberOption.getAsMember() : event.getMember();
		if (affectedMember != null) {
			Map<String, Integer> stringIntegerMap =
					penaltyRepository.aggregateByMonth(Objects.requireNonNull(event.getGuild()).getIdLong(), YearMonth.now(),
							affectedMember.getIdLong());
			event.reply("Penalties for " + affectedMember.getEffectiveName() + ":\n" +
							stringIntegerMap.entrySet().stream()
									.map(entry -> entry.getKey() + ": " + entry.getValue())
									.reduce((a, b) -> a + "\n" + b)
									.orElse("No penalties found."))
					.queue();
		} else {
			event.reply("User is not part of this guild.").setEphemeral(true).queue();
		}
	}
}
