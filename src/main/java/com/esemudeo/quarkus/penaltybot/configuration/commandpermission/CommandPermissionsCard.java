package com.esemudeo.quarkus.penaltybot.configuration.commandpermission;

import com.esemudeo.quarkus.penaltybot.configuration.SettingsCard;
import com.esemudeo.quarkus.penaltybot.configuration.SettingsService;
import com.esemudeo.quarkus.penaltybot.configuration.commandpermission.model.CommandPermission;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxBase;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandPermissionsCard extends SettingsCard {

	private static final String DISCORD_DEFAULT_ROLE_COLOR = "#99AAB5";
	private static final String OPENED_PROPERTY = "opened";
	private static final String TWO_COLUMN_BREAKPOINT = "500px";

	private final CommandPermissionsHandler handler;

	private final Map<String, ComboBox<SettingsService.GuildRole>> minRoleComboBoxes = new HashMap<>();
	private final Map<String, MultiSelectComboBox<SettingsService.GuildRole>> explicitRoleComboBoxes = new HashMap<>();
	private final List<ComboBox<SettingsService.GuildRole>> allSingleRoleComboBoxes = new ArrayList<>();
	private final List<MultiSelectComboBox<SettingsService.GuildRole>> allMultiRoleComboBoxes = new ArrayList<>();

	private Button saveButton;

	public CommandPermissionsCard(CommandPermissionsHandler handler) {
		super("Command Permissions");
		this.handler = handler;
		buildContent();
	}

	private void buildContent() {
		for (CommandPermission cp : handler.getCommandPermissions()) {
			buildCommandPermissionRow(cp);
		}

		saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setEnabled(false);
		saveButton.addClickListener(e -> {
			handler.save();
			saveButton.setEnabled(false);
			Notification.show("Command permissions saved.", NOTIFICATION_DURATION_MS, Notification.Position.BOTTOM_START)
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		});

		Div buttonWrapper = new Div(saveButton);
		buttonWrapper.getStyle().set("margin-top", "var(--lumo-space-m)");
		add(buttonWrapper);
	}

	private void buildCommandPermissionRow(CommandPermission cp) {
		String name = cp.getCommandName();

		ComboBox<SettingsService.GuildRole> minRoleCombo = new ComboBox<>("Minimum role");
		configureRolesComboBox(minRoleCombo);
		minRoleCombo.setClearButtonVisible(true);
		minRoleComboBoxes.put(name, minRoleCombo);
		allSingleRoleComboBoxes.add(minRoleCombo);

		MultiSelectComboBox<SettingsService.GuildRole> explicitRolesCombo = new MultiSelectComboBox<>("Explicit roles");
		configureRolesComboBox(explicitRolesCombo);
		explicitRoleComboBoxes.put(name, explicitRolesCombo);
		allMultiRoleComboBoxes.add(explicitRolesCombo);

		minRoleCombo.addValueChangeListener(e -> {
			Long roleId = e.getValue() != null ? e.getValue().id() : null;
			handler.updateCurrentMinRole(name, roleId);
			saveButton.setEnabled(handler.isDirty());
		});
		explicitRolesCombo.addValueChangeListener(e -> {
			Set<Long> roleIds = e.getValue().stream()
					.map(SettingsService.GuildRole::id)
					.collect(Collectors.toSet());
			handler.updateCurrentExplicitRoles(name, roleIds);
			saveButton.setEnabled(handler.isDirty());
		});

		Div commandBlock = new Div();
		commandBlock.getStyle()
				.set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
				.set("border", "1px solid var(--lumo-contrast-10pct)")
				.set("border-radius", "var(--lumo-border-radius-m)")
				.set("margin-bottom", "var(--lumo-space-xs)");

		Span commandLabel = new Span("/" + name);
		commandLabel.getStyle()
				.set("font-weight", "600")
				.set("font-size", "var(--lumo-font-size-s)")
				.set("color", "var(--lumo-primary-text-color)");

		FormLayout fields = new FormLayout();
		fields.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep(TWO_COLUMN_BREAKPOINT, 2));
		fields.add(minRoleCombo, explicitRolesCombo);

		commandBlock.add(commandLabel, fields);
		add(commandBlock);
	}

	@Override
	public void applyInitialState() {
		handler.clearRolesCache();

		for (CommandPermissionsHandler.CommandPermissionState state : handler.getStates()) {
			String name = state.getCommandName();
			ComboBox<SettingsService.GuildRole> minRoleCombo = minRoleComboBoxes.get(name);
			MultiSelectComboBox<SettingsService.GuildRole> explicitRolesCombo = explicitRoleComboBoxes.get(name);

			if (state.getInitialMinRoleId() != null) {
				handler.getGuildRoleById(state.getInitialMinRoleId()).ifPresent(role -> {
					minRoleCombo.setItems(List.of(role));
					minRoleCombo.setValue(role);
				});
			} else {
				minRoleCombo.setItems(List.of());
				minRoleCombo.clear();
			}

			if (!state.getInitialExplicitRoleIds().isEmpty()) {
				List<SettingsService.GuildRole> roles = handler.getGuildRolesByIds(state.getInitialExplicitRoleIds());
				explicitRolesCombo.setItems(roles);
				explicitRolesCombo.setValue(new HashSet<>(roles));
			} else {
				explicitRolesCombo.setItems(List.of());
				explicitRolesCombo.clear();
			}
		}
		saveButton.setEnabled(false);
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
		comboBox.getElement().addPropertyChangeListener(OPENED_PROPERTY, e -> {
			boolean dropdownWasOpened = Boolean.TRUE.equals(e.getValue());
			if (dropdownWasOpened) {
				onRolesDropdownOpened();
			}
		});
	}

	private void onRolesDropdownOpened() {
		List<SettingsService.GuildRole> roles = handler.ensureRolesLoaded();

		for (ComboBox<SettingsService.GuildRole> comboBox : allSingleRoleComboBoxes) {
			SettingsService.GuildRole currentValue = comboBox.getValue();
			comboBox.setItems(roles);
			if (currentValue != null) {
				roles.stream()
						.filter(r -> r.id() == currentValue.id())
						.findFirst()
						.ifPresent(comboBox::setValue);
			}
		}

		for (MultiSelectComboBox<SettingsService.GuildRole> comboBox : allMultiRoleComboBoxes) {
			Set<SettingsService.GuildRole> currentValue = comboBox.getValue();
			Set<Long> prevIds = currentValue.stream()
					.map(SettingsService.GuildRole::id)
					.collect(Collectors.toSet());
			comboBox.setItems(roles);
			Set<SettingsService.GuildRole> restored = roles.stream()
					.filter(r -> prevIds.contains(r.id()))
					.collect(Collectors.toSet());
			comboBox.setValue(restored);
		}
	}
}
