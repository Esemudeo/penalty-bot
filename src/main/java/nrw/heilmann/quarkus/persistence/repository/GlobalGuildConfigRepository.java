package nrw.heilmann.quarkus.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import nrw.heilmann.quarkus.persistence.model.GlobalGuildConfig;

import java.util.Optional;

@ApplicationScoped
@Transactional
public class GlobalGuildConfigRepository {

	public Optional<GlobalGuildConfig> findByGuild(long guildId) {
		return GlobalGuildConfig.find("guildId = ?1", guildId).firstResultOptional();
	}

	public void upsertPaypalMeUsername(long guildId, String username) {
		int updated = GlobalGuildConfig.update("paypalMeUsername = ?1 where guildId = ?2", username, guildId);
		if (updated == 0) {
			GlobalGuildConfig.builder()
					.guildId(guildId)
					.paypalMeUsername(username)
					.build()
					.persist();
		}
	}
}
