package nrw.heilmann.quarkus.bot.listeners;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nrw.heilmann.quarkus.bot.persistence.PenaltyType;

import java.util.Optional;

@ApplicationScoped
public class GuildReadyListener extends ListenerAdapter {

	@Override
	@Transactional
	public void onGuildReady(@Nonnull GuildReadyEvent event) {
		createTeamkillPenaltyAsDefault(event);
	}

	private static void createTeamkillPenaltyAsDefault(@Nonnull GuildReadyEvent event) {
		long guildId = event.getGuild().getIdLong();
		Optional<PenaltyType> teamkillPenaltyType =
				PenaltyType.<PenaltyType>find("guildId = ?1 and technicalName = ?2", guildId, "teamkill").stream().findAny();
		if (teamkillPenaltyType.isEmpty()) {
			PenaltyType.builder()
					.guildId(guildId)
					.technicalName("teamkill")
					.displayName("Teamkill")
					.build()
					.persist();
		}
	}
}
