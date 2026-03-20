package nrw.heilmann.quarkus.persistence.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "penalty_type")
public class PenaltyType extends PanacheEntity {
	@Column(name = "guild_id")
	private @Nonnull Long guildId;
	@Column(name = "technical_name")
	private @Nonnull String technicalName;
	@Column(name = "display_name")
	private @Nonnull String displayName;
	@Column(name = "is_default")
	private boolean defaultType;
	private Integer price;
}
