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

import java.util.List;
import java.util.Map;

@Route("settings")
public class SettingsView extends VerticalLayout implements BeforeEnterObserver {

    @Inject
    JDAInstance jdaInstance;

    private final H2 welcomeHeading = new H2();

    public SettingsView() {
        add(welcomeHeading);
        add(new Paragraph("Configuration options will be available here."));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
        List<String> userIds = params.get("userId");
        List<String> guildIds = params.get("guildId");

        if (userIds == null || userIds.isEmpty() || guildIds == null || guildIds.isEmpty()) {
            event.forwardTo(ErrorView.class);
            return;
        }

        try {
            long userId = Long.parseLong(userIds.getFirst());
            long guildId = Long.parseLong(guildIds.getFirst());

            Guild guild = jdaInstance.getJda().getGuildById(guildId);
            if (guild == null) {
                event.forwardTo(ErrorView.class);
                return;
            }

            Member member = guild.retrieveMemberById(userId).complete();
            welcomeHeading.setText("Server Settings – " + member.getEffectiveName());
        } catch (Exception e) {
            event.forwardTo(ErrorView.class);
        }
    }
}