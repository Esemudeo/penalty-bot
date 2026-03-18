package nrw.heilmann.quarkus.bot.persistence.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "penalty")
public class Penalty extends PanacheEntity {
	@Column(name = "timestamp")
	private Instant timestamp;
	@Column(name = "guild_id")
	private Long guildId;
	@Column(name = "author_id")
	private Long authorId;
	@Column(name = "affected_member_id")
	private Long affectedMemberId;
	@Column(name = "amount")
	private Integer amount;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "penalty_type_pkid", nullable = false)
	private PenaltyType penaltyType;
}
