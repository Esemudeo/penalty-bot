package com.esemudeo.quarkus.penaltybot.web.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import com.esemudeo.quarkus.penaltybot.JDAInstance;
import com.esemudeo.quarkus.penaltybot.persistence.model.ConfigSessionToken;
import com.esemudeo.quarkus.penaltybot.persistence.repository.ConfigSessionTokenRepository;
import com.esemudeo.quarkus.penaltybot.web.AuthSession;

import java.util.List;
import java.util.Map;

@Route("settings")
public class SettingsView extends VerticalLayout implements BeforeEnterObserver {

    @Inject
    JDAInstance jdaInstance;

    @Inject
    AuthSession authSession;

    @Inject
    ConfigSessionTokenRepository configSessionTokenRepository;

    private final H2 welcomeHeading = new H2();

    public SettingsView() {
        add(welcomeHeading);
        add(new Paragraph("Configuration options will be available here."));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authSession.isNotAuthenticated()) {
            if (!tryAuthenticateFromToken(event)) {
                return;
            }
        }

        try {
            Guild guild = jdaInstance.getJda().getGuildById(authSession.getGuildId());
            if (guild == null) {
                event.forwardTo(ErrorView.class);
                return;
            }

            Member member = guild.retrieveMemberById(authSession.getUserId()).complete();
            welcomeHeading.setText("Server Settings – " + member.getEffectiveName());
        } catch (Exception e) {
            event.forwardTo(ErrorView.class);
        }
    }

    /** Validates the one-time settings-access token from the URL, populates AuthSession, returns false on failure. */
    private boolean tryAuthenticateFromToken(BeforeEnterEvent event) {
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
        List<String> tokens = params.get("token");
        if (tokens == null || tokens.isEmpty()) {
            event.forwardTo(ErrorView.class);
            return false;
        }

        ConfigSessionToken token = configSessionTokenRepository.findValidByToken(tokens.getFirst()).orElse(null);
        if (token == null) {
            event.forwardTo(ErrorView.class);
            return false;
        }

        configSessionTokenRepository.markAsUsed(token.getToken());
        authSession.setUserId(token.getUserId());
        authSession.setGuildId(token.getGuildId());
        return true;
    }
}