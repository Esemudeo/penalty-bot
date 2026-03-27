package com.esemudeo.quarkus.penaltybot.configuration;

import com.esemudeo.quarkus.penaltybot.configuration.commandpermission.CommandPermissionsCard;
import com.esemudeo.quarkus.penaltybot.configuration.commandpermission.CommandPermissionsHandler;
import com.esemudeo.quarkus.penaltybot.configuration.commandpermission.model.CommandPermission;
import com.esemudeo.quarkus.penaltybot.configuration.global.GlobalSettingsCard;
import com.esemudeo.quarkus.penaltybot.configuration.global.GlobalSettingsHandler;
import com.esemudeo.quarkus.penaltybot.configuration.global.model.GlobalGuildConfig;
import com.esemudeo.quarkus.penaltybot.configuration.penaltytype.PenaltyTypesCard;
import com.esemudeo.quarkus.penaltybot.configuration.penaltytype.PenaltyTypesHandler;
import com.esemudeo.quarkus.penaltybot.configuration.penaltytype.model.PenaltyType;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@Route("settings")
@PreserveOnRefresh
public class SettingsView extends VerticalLayout implements BeforeEnterObserver {

	private static final String CONTENT_MAX_WIDTH = "1200px";
	private static final String COLUMN_FLEX_BASIS = "1 1 400px";

	@Inject
	AuthSession authSession;

	@Inject
	SettingsService settingsService;

	private CommandPermissionsCard commandPermissionsCard;
	private PenaltyTypesCard penaltyTypesCard;
	private GlobalSettingsCard globalSettingsCard;

	private boolean initialized;

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		if (authSession.isNotAuthenticated()) {
			if (!tryAuthenticateFromToken(event)) {
				return;
			}
		}

		try {
			if (!initialized) {
				buildSections();
				initialized = true;
			}
			applyInitialState();
		} catch (Exception e) {
			event.forwardTo(ErrorView.class);
		}
	}

	private void buildSections() {
		removeAll();
		setPadding(false);
		setSpacing(false);
		setAlignItems(FlexComponent.Alignment.CENTER);
		getStyle()
				.set("background-color", "var(--lumo-shade-5pct)")
				.set("min-height", "100vh")
				.set("width", "100%")
				.set("box-sizing", "border-box")
				.set("overflow-x", "hidden");

		Div content = new Div();
		content.getStyle()
				.set("max-width", CONTENT_MAX_WIDTH)
				.set("width", "100%")
				.set("padding", "var(--lumo-space-m)")
				.set("box-sizing", "border-box");

		content.add(buildHeader());

		// Load data
		List<CommandPermission> commandPermissions = settingsService.getCommands();
		Optional<GlobalGuildConfig> globalConfig = settingsService.getGlobalConfig();
		List<PenaltyType> penaltyTypes = settingsService.getAllPenaltyTypes();

		// Create handlers (logic)
		CommandPermissionsHandler cpHandler = new CommandPermissionsHandler(commandPermissions, settingsService);
		PenaltyTypesHandler ptHandler = new PenaltyTypesHandler(penaltyTypes, settingsService);
		GlobalSettingsHandler gsHandler = new GlobalSettingsHandler(globalConfig, settingsService);

		// Create cards (UI)
		commandPermissionsCard = new CommandPermissionsCard(cpHandler);
		penaltyTypesCard = new PenaltyTypesCard(ptHandler);
		globalSettingsCard = new GlobalSettingsCard(gsHandler);

		// Two-column layout
		Div columnsRow = new Div();
		columnsRow.getStyle()
				.set("display", "flex")
				.set("gap", "var(--lumo-space-s)")
				.set("align-items", "flex-start")
				.set("flex-wrap", "wrap");

		Div leftColumn = new Div();
		leftColumn.getStyle()
				.set("flex", COLUMN_FLEX_BASIS)
				.set("min-width", "0");
		leftColumn.add(commandPermissionsCard);

		Div rightColumn = new Div();
		rightColumn.getStyle()
				.set("flex", COLUMN_FLEX_BASIS)
				.set("min-width", "0");
		rightColumn.add(penaltyTypesCard);
		rightColumn.add(globalSettingsCard);

		columnsRow.add(leftColumn, rightColumn);
		content.add(columnsRow);

		add(content);
	}

	private Div buildHeader() {
		Div header = new Div();
		header.getStyle().set("margin-bottom", "var(--lumo-space-s)");

		H2 heading = new H2("Penalty Bot Server Settings");
		heading.getStyle()
				.set("margin", "0")
				.set("font-size", "var(--lumo-font-size-xl)");

		Span guildName = new Span(settingsService.getGuildName());
		guildName.getStyle()
				.set("font-size", "var(--lumo-font-size-m)")
				.set("color", "var(--lumo-secondary-text-color)");

		Paragraph welcome = new Paragraph("Welcome, %s!".formatted(settingsService.getMemberDisplayName()));
		welcome.getStyle()
				.set("margin", "var(--lumo-space-xs) 0 0 0")
				.set("color", "var(--lumo-secondary-text-color)")
				.set("font-size", "var(--lumo-font-size-s)");

		header.add(heading, guildName, welcome);
		return header;
	}

	private void applyInitialState() {
		commandPermissionsCard.applyInitialState();
		penaltyTypesCard.applyInitialState();
		globalSettingsCard.applyInitialState();
	}

	private boolean tryAuthenticateFromToken(BeforeEnterEvent event) {
		List<String> tokens = event.getLocation().getQueryParameters().getParameters().get("token");
		if (tokens == null || tokens.isEmpty() || !settingsService.authenticateWithToken(tokens.getFirst())) {
			event.forwardTo(ErrorView.class);
			return false;
		}
		return true;
	}
}
