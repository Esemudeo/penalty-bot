package com.esemudeo.quarkus.penaltybot.configuration;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;

public abstract class SettingsCard extends Div {

	protected static final int NOTIFICATION_DURATION_MS = 3000;

	protected SettingsCard(String title) {
		getStyle()
				.set("background-color", "var(--lumo-base-color)")
				.set("border-radius", "var(--lumo-border-radius-l)")
				.set("box-shadow", "var(--lumo-box-shadow-xs)")
				.set("padding", "var(--lumo-space-m)")
				.set("margin-bottom", "var(--lumo-space-s)")
				.set("box-sizing", "border-box");

		H3 sectionTitle = new H3(title);
		sectionTitle.getStyle().set("margin", "0 0 var(--lumo-space-s) 0");
		add(sectionTitle);
	}

	public abstract void applyInitialState();
}