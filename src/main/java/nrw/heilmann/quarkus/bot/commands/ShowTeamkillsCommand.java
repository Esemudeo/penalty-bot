package nrw.heilmann.quarkus.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Random;

@ApplicationScoped
public class ShowTeamkillsCommand extends SlashCommand {

	private final static String OPTION_MEMBER = "member";

	@Override
	public String getName() {
		return "tk-show";
	}

	@Override
	protected String getDescription() {
		return "Show teamkills for a member.";
	}

	@Override
	protected List<OptionData> getOptions() {
		return List.of(
				new OptionData(OptionType.MENTIONABLE, "member",
						"The member you want to show the teamkills reported for. Leave empty for yourself."));
	}

	@Override
	public void handleCommand(SlashCommandInteractionEvent event) {
		OptionMapping memberOption = event.getOption(OPTION_MEMBER);
		Member affectedMember = memberOption != null ? memberOption.getAsMember() : event.getMember();
		if (affectedMember != null) {
			event.reply("Reported " + new Random().nextInt(10) + " teamkills for " + affectedMember.getEffectiveName() + " so far.").queue();
		} else {
			event.reply("User is not part of this guild.").setEphemeral(true).queue();
		}
	}
}
