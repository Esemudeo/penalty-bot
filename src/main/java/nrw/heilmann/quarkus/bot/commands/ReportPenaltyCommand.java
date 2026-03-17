package nrw.heilmann.quarkus.bot.commands;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import nrw.heilmann.quarkus.bot.exceptions.NotInGuildException;
import nrw.heilmann.quarkus.bot.persistence.Penalty;
import nrw.heilmann.quarkus.bot.persistence.PenaltyType;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ReportPenaltyCommand extends SlashCommand {

	private static final String ERROR_NOT_ON_GUILD = "Command wasn't executed inside a server.";
	private static final int DEFAULT_PENALTY_AMOUNT = 1;
	private static final String OPTION_AMOUNT = "amount";
	private static final String OPTION_MEMBER = "member";

	@Override
	public String getName() {
		return "penalty";
	}

	@Override
	protected String getDescription() {
		return "Report a penalty for a member.";
	}

	@Override
	protected List<OptionData> getOptions() {
		return List.of(
				new OptionData(OptionType.MENTIONABLE, OPTION_MEMBER, "The member you want to report the penalty for. Leave empty for yourself."),
				new OptionData(OptionType.INTEGER, OPTION_AMOUNT, "The amount of penalties you want to report. Leave empty for default of '1'."));
	}

	@Override
	@Transactional
	public void handleCommand(SlashCommandInteractionEvent event) {
		try {
			Member author = resolveAuthor(event);
			Member affectedMember = resolveAffectedMember(event, author);
			long guildId = resolveGuildId(event);

			OptionMapping amountOption = event.getOption(OPTION_AMOUNT);
			int amount = amountOption != null ? amountOption.getAsInt() : DEFAULT_PENALTY_AMOUNT;

			Optional<PenaltyType> penaltyType =
					PenaltyType.<PenaltyType>find("guildId = ?1 and technicalName = ?2", guildId, "teamkill").stream().findFirst();
			if (penaltyType.isEmpty()) {
				event.reply("Teamkill penalty not found for this server. Please contact the administrator.").setEphemeral(true).queue();
				return;
			}

			Penalty penaltyToSave = Penalty.builder()
					.timestamp(event.getTimeCreated().toInstant())
					.guildId(guildId)
					.authorId(author.getIdLong())
					.affectedMemberId(affectedMember.getIdLong())
					.amount(amount)
					.penaltyType(penaltyType.get())
					.build();

			penaltyToSave.persist();
			String penaltyPhrase = formatPenaltyPhrase(amount);
			event.reply(
					"%s %s of type '%s' for %s!".formatted(author.getEffectiveName(), penaltyPhrase, penaltyToSave.getPenaltyType().getDisplayName(),
							affectedMember.getEffectiveName())).queue();
		} catch (NotInGuildException e) {
			replyAsNotInGuild(event);
		} catch (Exception e) {
			event.reply("An error occurred while processing the command.").setEphemeral(true).queue();
		}
	}

	private static @Nonnull Member resolveAuthor(SlashCommandInteractionEvent event) {
		Member author = event.getMember();
		if (author == null) {
			throw new NotInGuildException();
		}
		return author;
	}

	private static @Nonnull Member resolveAffectedMember(SlashCommandInteractionEvent event, @Nonnull Member author) {
		OptionMapping memberOption = event.getOption(OPTION_MEMBER);
		if (memberOption == null) {
			return author;
		}

		Member member = memberOption.getAsMember();
		if (member == null) {
			throw new NotInGuildException();
		}

		return member;
	}

	private static long resolveGuildId(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		if (guild == null) {
			throw new NotInGuildException();
		}
		return guild.getIdLong();
	}

	private String formatPenaltyPhrase(int amount) {
		String action = amount > 0 ? "reported" : "removed";
		int absolutePenalties = Math.abs(amount);
		if (absolutePenalties == 1) {
			return "%s a penalty".formatted(action);
		}
		return "%s %d penalties".formatted(action, absolutePenalties);
	}

	private static void replyAsNotInGuild(SlashCommandInteractionEvent event) {
		event.reply(ERROR_NOT_ON_GUILD).setEphemeral(true).queue();
	}
}
