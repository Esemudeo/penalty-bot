package com.esemudeo.quarkus.penaltybot.configuration.auth.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import com.esemudeo.quarkus.penaltybot.configuration.auth.model.ConfigSessionToken;

import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class ConfigSessionTokenRepository {

	public Optional<ConfigSessionToken> findValidByToken(String token) {
		return ConfigSessionToken.find("token = ?1 and used = false and expiresAt > ?2", token, Instant.now())
				.firstResultOptional();
	}

	public void markAsUsed(String token) {
		ConfigSessionToken.update("used = true where token = ?1", token);
	}

	public void invalidateAllForUser(long userId) {
		ConfigSessionToken.delete("userId = ?1", userId);
	}

	public void save(ConfigSessionToken sessionToken) {
		sessionToken.persist();
	}
}
