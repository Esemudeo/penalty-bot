package com.esemudeo.quarkus.penaltybot.persistence.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "penalty_type")
public class PenaltyType extends PanacheEntity {
	@Column(name = "guild_id", nullable = false)
	private @Nonnull Long guildId;
	@Column(name = "technical_name", nullable = false)
	private @Nonnull String technicalName;
	@Column(name = "display_name", nullable = false)
	private @Nonnull String displayName;
	@Setter
	@Column(name = "is_default", nullable = false)
	private boolean defaultType;
	@Setter
	@Column(name = "price")
	private Integer price;
	@Setter
	@Column(name = "active", nullable = false)
	private boolean active;
}
