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
import nrw.heilmann.quarkus.bot.persistence.Teamkill;

import java.util.List;

@ApplicationScoped
public class ReportTeamkillsCommand extends SlashCommand {

	private static final String ERROR_NOT_ON_GUILD = "Command wasn't executed inside a server.";
	private static final int DEFAULT_TK_AMOUNT = 1;
	private static final String OPTION_AMOUNT = "amount";
	private static final String OPTION_MEMBER = "member";

	@Override
	public String getName() {
		return "tk";
	}

	@Override
	protected String getDescription() {
		return "Report teamkills for a member.";
	}

	@Override
	protected List<OptionData> getOptions() {
		return List.of(
				new OptionData(OptionType.MENTIONABLE, OPTION_MEMBER, "The member you want to report the teamkills for. Leave empty for yourself."),
				new OptionData(OptionType.INTEGER, OPTION_AMOUNT, "The amount of teamkills you want to report. Leave empty for default of '1'."));
	}

	@Override
	@Transactional
	public void handleCommand(SlashCommandInteractionEvent event) {
		try {
			Member author = resolveAuthor(event);
			Member affectedMember = resolveAffectedMember(event, author);
			long guildId = resolveGuildId(event);

			OptionMapping amountOption = event.getOption(OPTION_AMOUNT);
			int amount = amountOption != null ? amountOption.getAsInt() : DEFAULT_TK_AMOUNT;

			Teamkill teamkillsToSave = Teamkill.builder()
					.timestamp(event.getTimeCreated().toInstant())
					.authorId(author.getIdLong())
					.affectedMemberId(affectedMember.getIdLong())
					.amount(amount)
					.guildId(guildId)
					.build();

			teamkillsToSave.persist();
			String teamkillPhrase = formatTeamkillPhrase(amount);
			event.reply("%s %s for %s!".formatted(author.getEffectiveName(), teamkillPhrase, affectedMember.getEffectiveName())).queue();
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

	private String formatTeamkillPhrase(int amount) {
		if (amount == 0) {
			return "reported no teamkill";
		}
		String action = amount > 0 ? "reported" : "removed";
		int absoluteTeamkills = Math.abs(amount);
		if (absoluteTeamkills == 1) {
			return "%s a teamkill".formatted(action);
		}
		return "%s %d teamkills".formatted(action, absoluteTeamkills);
	}

	private static void replyAsNotInGuild(SlashCommandInteractionEvent event) {
		event.reply(ERROR_NOT_ON_GUILD).setEphemeral(true).queue();
	}
}
