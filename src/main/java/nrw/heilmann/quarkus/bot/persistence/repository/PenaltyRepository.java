package nrw.heilmann.quarkus.bot.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import nrw.heilmann.quarkus.bot.persistence.model.Penalty;
import nrw.heilmann.quarkus.bot.persistence.model.PenaltyType;
import org.hibernate.Hibernate;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

	public Map<String, Integer> aggregateByMonth(long guildId, YearMonth yearMonth, long userId) {
		Instant startOfMonthInclusive = yearMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
		Instant endOfMonthExclusive = yearMonth.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
		List<Penalty> allPenaltiesOfMonth =
				Penalty.list("guildId = ?1 and affectedMemberId = ?2 and timestamp >= ?3 and timestamp < ?4", guildId, userId, startOfMonthInclusive,
						endOfMonthExclusive);
		return allPenaltiesOfMonth
				.stream()
				.map(p -> new PenaltyAmountByType(p.getPenaltyType().getDisplayName(), p.getAmount()))
				.collect(Collectors.groupingBy(PenaltyAmountByType::displayName, Collectors.summingInt(PenaltyAmountByType::amount)));
	}

	public List<YearMonth> findAvailableMonthsForGuild(long guildId, int limit) {
		return Penalty.<Penalty>find("guildId = ?1 ORDER BY timestamp DESC", guildId)
				.stream()
				.map(p -> YearMonth.from(p.getTimestamp().atZone(ZoneOffset.UTC)))
				.distinct()
				.limit(limit)
				.toList();
	}

	record PenaltyAmountByType(String displayName, int amount) {}
}
