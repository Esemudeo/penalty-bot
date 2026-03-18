package nrw.heilmann.quarkus.bot.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import nrw.heilmann.quarkus.bot.persistence.model.Penalty;
import nrw.heilmann.quarkus.bot.persistence.model.PenaltyType;
import org.hibernate.Hibernate;

import java.util.Optional;

@ApplicationScoped
@Transactional
public class PenaltyRepository {

	public Optional<Penalty> save(Penalty penalty, String penaltyTypeName) {
		Optional<PenaltyType> penaltyType = PenaltyType
				.find("guildId = ?1 and technicalName = ?2", penalty.getGuildId(), penaltyTypeName)
				.firstResultOptional();
		if (penaltyType.isEmpty()) {
			return Optional.empty();
		}
		Penalty managed = penalty.toBuilder().penaltyType(penaltyType.get()).build();
		managed.persist();
		Hibernate.initialize(managed.getPenaltyType());
		return Optional.of(managed);
	}
}
