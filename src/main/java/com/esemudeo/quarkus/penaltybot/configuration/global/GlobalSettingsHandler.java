package com.esemudeo.quarkus.penaltybot.configuration.global;

import com.esemudeo.quarkus.penaltybot.configuration.SettingsService;
import com.esemudeo.quarkus.penaltybot.configuration.global.model.GlobalGuildConfig;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GlobalSettingsHandler {

	private final SettingsService settingsService;
	private Optional<GlobalGuildConfig> initialConfig;

	private List<SettingsService.GuildTextChannel> cachedChannels;

	public GlobalSettingsHandler(Optional<GlobalGuildConfig> initialConfig, SettingsService settingsService) {
		this.settingsService = settingsService;
		this.initialConfig = initialConfig;
	}

	public String getInitialPaypalUsername() {
		return initialConfig.map(GlobalGuildConfig::getPaypalMeUsername).orElse(null);
	}

	public Long getInitialNotificationChannelId() {
		return initialConfig.map(GlobalGuildConfig::getNotificationChannelId).orElse(null);
	}

	public boolean isDirty(String currentPaypal, Long currentChannelId) {
		String normalizedPaypal = (currentPaypal != null && currentPaypal.isBlank()) ? null : currentPaypal;
		return !Objects.equals(normalizedPaypal, getInitialPaypalUsername())
				|| !Objects.equals(currentChannelId, getInitialNotificationChannelId());
	}

	public void save(String paypalUsername, Long notificationChannelId) {
		String normalized = (paypalUsername != null && paypalUsername.isBlank()) ? null : paypalUsername;
		settingsService.updatePaypalMeUsername(normalized);
		settingsService.updateNotificationChannelId(notificationChannelId);
		initialConfig = settingsService.getGlobalConfig();
	}

	public void clearChannelsCache() {
		cachedChannels = null;
	}

	public List<SettingsService.GuildTextChannel> ensureChannelsLoaded() {
		if (cachedChannels == null) {
			cachedChannels = settingsService.getGuildTextChannels();
		}
		return cachedChannels;
	}

	public Optional<SettingsService.GuildTextChannel> getGuildTextChannelById(long id) {
		return settingsService.getGuildTextChannelById(id);
	}
}
