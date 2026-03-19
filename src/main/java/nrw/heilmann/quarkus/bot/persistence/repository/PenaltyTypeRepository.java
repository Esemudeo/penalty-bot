package nrw.heilmann.quarkus.bot.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import nrw.heilmann.quarkus.bot.persistence.model.PenaltyType;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class PenaltyTypeRepository {

	public List<PenaltyType> findByGuild(long guildId) {
		return PenaltyType.find("guildId = ?1", guildId).list();
	}

	public Optional<PenaltyType> findByGuildAndName(long guildId, String technicalName) {
		return PenaltyType.find("guildId = ?1 and technicalName = ?2", guildId, technicalName).firstResultOptional();
	}

	public void persistIfAbsent(long guildId, String technicalName, String displayName, boolean defaultType) {
		if (findByGuildAndName(guildId, technicalName).isEmpty()) {
			PenaltyType.builder()
					.guildId(guildId)
					.technicalName(technicalName)
					.displayName(displayName)
					.defaultType(defaultType)
					.build()
					.persist();
		}
	}
}
