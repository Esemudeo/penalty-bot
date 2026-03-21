package com.esemudeo.quarkus.penaltybot.web.view;

import com.esemudeo.quarkus.penaltybot.web.AuthSession;
import com.esemudeo.quarkus.penaltybot.web.SettingsService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxBase;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.inject.Inject;

import java.util.List;

@Route("settings")
public class SettingsView extends VerticalLayout implements BeforeEnterObserver {

	private static final String DISCORD_DEFAULT_ROLE_COLOR = "#99AAB5";

	@Inject
	AuthSession authSession;

	@Inject
	SettingsService settingsService;

	private final H2 welcomeHeading = new H2();
	private final Paragraph welcomeParagraph = new Paragraph();
	private final MultiSelectComboBox<SettingsService.GuildRole> reportPenaltyExplicitRolesComboBox = new MultiSelectComboBox<>("Explicit roles");
	private final ComboBox<SettingsService.GuildRole> reportPenaltyMinRoleComboBox = new ComboBox<>("Minimum role");

	public SettingsView() {
		configureRolesComboBox(reportPenaltyExplicitRolesComboBox);
		configureRolesComboBox(reportPenaltyMinRoleComboBox);
		add(welcomeHeading, welcomeParagraph, reportPenaltyExplicitRolesComboBox, reportPenaltyMinRoleComboBox);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		if (authSession.isNotAuthenticated()) {
			if (!tryAuthenticateFromToken(event)) {
				return;
			}
		}

		try {
			welcomeHeading.setText("Penalty Bot Server Settings – %s".formatted(settingsService.getGuildName()));
			welcomeParagraph.setText("Welcome, %s, to the server settings for the penalty bot!".formatted(settingsService.getMemberDisplayName()));
			reportPenaltyExplicitRolesComboBox.setItems(settingsService.getGuildRoles());
			reportPenaltyMinRoleComboBox.setItems(settingsService.getGuildRoles());
		} catch (Exception e) {
			event.forwardTo(ErrorView.class);
		}
	}

	private <C extends ComboBoxBase<C, SettingsService.GuildRole, ?>> void configureRolesComboBox(C comboBox) {
		comboBox.setItemLabelGenerator(SettingsService.GuildRole::name);
		comboBox.setRenderer(new ComponentRenderer<>(role -> {
			Span dot = new Span();
			dot.getStyle()
					.set("display", "inline-block")
					.set("width", "10px").set("height", "10px")
					.set("border-radius", "50%")
					.set("background-color", role.hexColor() != null ? role.hexColor() : DISCORD_DEFAULT_ROLE_COLOR)
					.set("margin-right", "8px")
					.set("flex-shrink", "0");
			return new Span(dot, new Span(role.name()));
		}));
		comboBox.setWidthFull();
	}

	/** Validates the one-time settings-access token from the URL, populates AuthSession, returns false on failure. */
	private boolean tryAuthenticateFromToken(BeforeEnterEvent event) {
		List<String> tokens = event.getLocation().getQueryParameters().getParameters().get("token");
		if (tokens == null || tokens.isEmpty() || !settingsService.authenticateWithToken(tokens.getFirst())) {
			event.forwardTo(ErrorView.class);
			return false;
		}
		return true;
	}
}
