package com.esemudeo.quarkus.penaltybot.configuration.global.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
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
@Builder(toBuilder = true)
@Entity
@Table(name = "global_guild_config")
public class GlobalGuildConfig extends PanacheEntity {
	@Column(name = "guild_id", nullable = false)
	private Long guildId;
	@Column(name = "paypal_me_username")
	private String paypalMeUsername;
	@Column(name = "notification_channel_id")
	private Long notificationChannelId;
}
