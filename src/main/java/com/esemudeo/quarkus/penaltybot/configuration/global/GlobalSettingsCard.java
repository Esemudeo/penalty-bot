package com.esemudeo.quarkus.penaltybot.configuration.global;

import com.esemudeo.quarkus.penaltybot.configuration.SettingsCard;
import com.esemudeo.quarkus.penaltybot.configuration.SettingsService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.List;

public class GlobalSettingsCard extends SettingsCard {

	private static final String OPENED_PROPERTY = "opened";

	private final GlobalSettingsHandler handler;

	private TextField paypalField;
	private ComboBox<SettingsService.GuildTextChannel> notificationChannelComboBox;
	private Button saveButton;

	public GlobalSettingsCard(GlobalSettingsHandler handler) {
		super("Common Settings");
		this.handler = handler;
		buildContent();
	}

	private void buildContent() {
		paypalField = new TextField("PayPal.me Username");
		paypalField.setValueChangeMode(ValueChangeMode.EAGER);
		paypalField.setWidthFull();

		Span paypalPreview = new Span();
		paypalPreview.getStyle()
				.set("color", "var(--lumo-secondary-text-color)")
				.set("font-size", "var(--lumo-font-size-s)");
		paypalField.addValueChangeListener(e -> {
			String username = e.getValue();
			if (username != null && !username.isBlank()) {
				paypalPreview.setText("https://paypal.me/%s".formatted(username.trim()));
			} else {
				paypalPreview.setText("");
			}
		});

		notificationChannelComboBox = new ComboBox<>("Notification Channel");
		notificationChannelComboBox.setItemLabelGenerator(SettingsService.GuildTextChannel::name);
		notificationChannelComboBox.setClearButtonVisible(true);
		notificationChannelComboBox.setWidthFull();
		notificationChannelComboBox.getElement().addPropertyChangeListener(OPENED_PROPERTY, e -> {
			boolean dropdownWasOpened = Boolean.TRUE.equals(e.getValue());
			if (dropdownWasOpened) {
				onChannelsDropdownOpened();
			}
		});

		saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setEnabled(false);

		paypalField.addValueChangeListener(e -> updateDirtyState());
		notificationChannelComboBox.addValueChangeListener(e -> updateDirtyState());

		saveButton.addClickListener(e -> {
			Long channelId = notificationChannelComboBox.getValue() != null
					? notificationChannelComboBox.getValue().id()
					: null;
			handler.save(paypalField.getValue(), channelId);
			saveButton.setEnabled(false);
			Notification.show("Common settings saved.", 3000, Notification.Position.BOTTOM_START)
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		});

		FormLayout form = new FormLayout();
		form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
		form.add(paypalField);
		form.add(paypalPreview);
		form.add(notificationChannelComboBox);

		Div buttonWrapper = new Div(saveButton);
		buttonWrapper.getStyle().set("margin-top", "var(--lumo-space-m)");

		add(form, buttonWrapper);
	}

	private void updateDirtyState() {
		Long currentChannelId = notificationChannelComboBox.getValue() != null
				? notificationChannelComboBox.getValue().id()
				: null;
		saveButton.setEnabled(handler.isDirty(paypalField.getValue(), currentChannelId));
	}

	@Override
	public void applyInitialState() {
		handler.clearChannelsCache();

		String initialPaypal = handler.getInitialPaypalUsername();
		paypalField.setValue(initialPaypal != null ? initialPaypal : "");

		Long initialChannelId = handler.getInitialNotificationChannelId();
		if (initialChannelId != null) {
			handler.getGuildTextChannelById(initialChannelId).ifPresent(channel -> {
				notificationChannelComboBox.setItems(List.of(channel));
				notificationChannelComboBox.setValue(channel);
			});
		} else {
			notificationChannelComboBox.setItems(List.of());
			notificationChannelComboBox.clear();
		}
		saveButton.setEnabled(false);
	}

	private void onChannelsDropdownOpened() {
		List<SettingsService.GuildTextChannel> channels = handler.ensureChannelsLoaded();
		SettingsService.GuildTextChannel currentValue = notificationChannelComboBox.getValue();
		notificationChannelComboBox.setItems(channels);
		if (currentValue != null) {
			channels.stream()
					.filter(c -> c.id() == currentValue.id())
					.findFirst()
					.ifPresent(notificationChannelComboBox::setValue);
		}
	}
}
