package com.esemudeo.quarkus.penaltybot.penalty.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import com.esemudeo.quarkus.penaltybot.penalty.model.Penalty;
import com.esemudeo.quarkus.penaltybot.configuration.penaltytype.model.PenaltyType;
import com.esemudeo.quarkus.penaltybot.configuration.penaltytype.repository.PenaltyTypeRepository;

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

	@Inject
	PenaltyTypeRepository penaltyTypeRepository;

	public Optional<Penalty> save(Penalty penalty, String penaltyTypeName) {
		Optional<PenaltyType> penaltyType = penaltyTypeRepository.findByGuildAndTechnicalName(penalty.getGuildId(), penaltyTypeName);
		if (penaltyType.isEmpty()) {
			return Optional.empty();
		}
		Penalty penaltyToPersist = penalty.toBuilder().penaltyType(penaltyType.get()).build();
		penaltyToPersist.persist();
		return Optional.of(penaltyToPersist);
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

	public Map<Long, List<PenaltyTypeSummary>> aggregateByMonthForAllUsers(long guildId, YearMonth yearMonth) {
		Instant startOfMonthInclusive = yearMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
		Instant endOfMonthExclusive = yearMonth.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
		List<Penalty> allPenaltiesOfMonth =
				Penalty.list("guildId = ?1 and timestamp >= ?2 and timestamp < ?3", guildId, startOfMonthInclusive, endOfMonthExclusive);
		return allPenaltiesOfMonth
				.stream()
				.collect(Collectors.groupingBy(
						Penalty::getAffectedMemberId,
						Collectors.collectingAndThen(
								Collectors.groupingBy(Penalty::getPenaltyType),
								typeMap -> typeMap.entrySet().stream()
										.map(e -> {
											int total = e.getValue().stream().mapToInt(Penalty::getAmount).sum();
											Integer price = e.getKey().getPrice() != null ? total * e.getKey().getPrice() : null;
											return new PenaltyTypeSummary(e.getKey().getDisplayName(), total, price);
										})
										.filter(s -> s.totalAmount() > 0)
										.toList())));
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

	public record PenaltyTypeSummary(String displayName, int totalAmount, Integer totalPriceCents) {}
}
