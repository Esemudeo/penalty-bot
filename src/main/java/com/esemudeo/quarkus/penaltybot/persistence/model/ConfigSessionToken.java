package com.esemudeo.quarkus.penaltybot.persistence.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "config_session_token")
public class ConfigSessionToken extends PanacheEntity {
    @Column(name = "guild_id", nullable = false)
    private Long guildId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "token", nullable = false, unique = true)
    private String token;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "used", nullable = false)
    @Setter
    private boolean used;
}
