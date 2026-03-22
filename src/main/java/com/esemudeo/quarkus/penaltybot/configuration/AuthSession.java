package com.esemudeo.quarkus.penaltybot.configuration;

import com.vaadin.flow.server.VaadinSession;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Provides access to the authenticated Discord identity for the current Vaadin session.
 * Backed by {@link VaadinSession} – only callable from Vaadin UI threads.
 * Populated by {@link com.esemudeo.quarkus.penaltybot.configuration.SettingsView} after the
 * one-time settings-access token has been validated.
 */
@ApplicationScoped
public class AuthSession {

    private static final String KEY_USER_ID = "auth.userId";
    private static final String KEY_GUILD_ID = "auth.guildId";

    public boolean isNotAuthenticated() {
        return getUserId() == null || getGuildId() == null;
    }

    public Long getUserId() {
        return (Long) VaadinSession.getCurrent().getAttribute(KEY_USER_ID);
    }

    public Long getGuildId() {
        return (Long) VaadinSession.getCurrent().getAttribute(KEY_GUILD_ID);
    }

    public void setUserId(long userId) {
        VaadinSession.getCurrent().setAttribute(KEY_USER_ID, userId);
    }

    public void setGuildId(long guildId) {
        VaadinSession.getCurrent().setAttribute(KEY_GUILD_ID, guildId);
    }
}
