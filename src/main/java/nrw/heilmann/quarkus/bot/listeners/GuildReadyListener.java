package nrw.heilmann.quarkus.bot.listeners;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nrw.heilmann.quarkus.bot.persistence.PenaltyType;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

@ApplicationScoped
public class GuildReadyListener extends ListenerAdapter {

	@Override
	@Transactional
	public void onGuildReady(@NonNull GuildReadyEvent event) {
		createTeamkillPenaltyAsDefault(event);
	}

	private static void createTeamkillPenaltyAsDefault(@NonNull GuildReadyEvent event) {
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
