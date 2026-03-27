package com.esemudeo.quarkus.penaltybot.configuration.penaltytype;

import com.esemudeo.quarkus.penaltybot.configuration.SettingsCard;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

public class PenaltyTypesCard extends SettingsCard {

	private static final String DIALOG_WIDTH = "min(500px, 90vw)";

	private final PenaltyTypesHandler handler;

	private Grid<PenaltyTypesHandler.PenaltyTypeEntry> grid;
	private Button saveButton;
	private Button cancelButton;
	private Button addButton;

	public PenaltyTypesCard(PenaltyTypesHandler handler) {
		super("Penalty Types");
		this.handler = handler;
		buildContent();
	}

	private void buildContent() {
		grid = new Grid<>();
		grid.setItems(handler.getWorkingCopy());
		grid.setAllRowsVisible(true);
		grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);

		grid.addColumn(PenaltyTypesHandler.PenaltyTypeEntry::getDisplayName)
				.setHeader("Name").setFlexGrow(2).setAutoWidth(true);
		grid.addColumn(entry -> entry.getPrice() != null ? PenaltyTypesHandler.formatCentsAsEuro(entry.getPrice()) : "")
				.setHeader("Price").setFlexGrow(1).setAutoWidth(true);

		grid.addComponentColumn(this::buildDefaultToggle)
				.setHeader("Default").setFlexGrow(0).setAutoWidth(true);
		grid.addComponentColumn(this::buildActiveToggle)
				.setHeader("Active").setFlexGrow(0).setAutoWidth(true);
		grid.addComponentColumn(this::buildActionButtons)
				.setFlexGrow(0).setAutoWidth(true);

		grid.setWidthFull();

		addButton = new Button("Add Penalty Type");
		addButton.addClickListener(e -> openDialog(null));

		saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setEnabled(false);
		saveButton.addClickListener(e -> {
			handler.save();
			refreshGrid();
			Notification.show("Penalty types saved.", NOTIFICATION_DURATION_MS, Notification.Position.BOTTOM_START)
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		});

		cancelButton = new Button("Cancel");
		cancelButton.setVisible(false);
		cancelButton.addClickListener(e -> {
			handler.cancelChanges();
			refreshGrid();
		});

		FlexLayout buttons = new FlexLayout(addButton, saveButton, cancelButton);
		buttons.setFlexWrap(FlexLayout.FlexWrap.WRAP);
		buttons.getStyle()
				.set("gap", "var(--lumo-space-s)")
				.set("margin-top", "var(--lumo-space-m)");

		add(grid, buttons);
		updateButtonStates();
	}

	private Checkbox buildDefaultToggle(PenaltyTypesHandler.PenaltyTypeEntry entry) {
		Checkbox toggle = new Checkbox();
		toggle.setValue(entry.isDefaultType());
		long activeCount = handler.getWorkingCopy().stream()
				.filter(PenaltyTypesHandler.PenaltyTypeEntry::isActive).count();
		boolean canToggle = handler.canDeleteOrToggle() && entry.isActive() && activeCount > 1;
		toggle.setEnabled(canToggle);
		toggle.addValueChangeListener(e -> {
			handler.setDefault(entry, e.getValue());
			grid.getDataProvider().refreshAll();
			updateButtonStates();
		});
		return toggle;
	}

	private ToggleButton buildActiveToggle(PenaltyTypesHandler.PenaltyTypeEntry entry) {
		ToggleButton toggle = new ToggleButton();
		toggle.setValue(entry.isActive());
		toggle.setEnabled(handler.canDeleteOrToggle());
		toggle.addValueChangeListener(e -> {
			handler.setActive(entry, e.getValue());
			grid.getDataProvider().refreshAll();
			updateButtonStates();
		});
		return toggle;
	}

	private HorizontalLayout buildActionButtons(PenaltyTypesHandler.PenaltyTypeEntry entry) {
		Button editButton = new Button(new Icon(VaadinIcon.EDIT));
		editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
		editButton.getStyle()
				.set("border-radius", "var(--lumo-border-radius-l)")
				.set("background-color", "var(--lumo-primary-color-10pct)")
				.set("color", "var(--lumo-primary-text-color)");
		editButton.addClickListener(e -> openDialog(entry));

		Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
		deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
		deleteButton.getStyle()
				.set("border-radius", "var(--lumo-border-radius-l)")
				.set("background-color", "var(--lumo-error-color-10pct)")
				.set("color", "var(--lumo-error-text-color)");
		deleteButton.setEnabled(handler.canDeleteOrToggle());
		deleteButton.addClickListener(e -> confirmDelete(entry));

		HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
		actions.setSpacing(false);
		actions.getStyle().set("gap", "var(--lumo-space-xs)");
		return actions;
	}

	private void openDialog(PenaltyTypesHandler.PenaltyTypeEntry existing) {
		Dialog dialog = new Dialog();
		dialog.setHeaderTitle(existing != null ? "Edit Penalty Type" : "Add Penalty Type");
		dialog.setWidth(DIALOG_WIDTH);

		TextField nameField = new TextField("Name");
		nameField.setWidthFull();

		IntegerField priceField = new IntegerField("Price (in cents)");
		priceField.setValueChangeMode(ValueChangeMode.EAGER);
		priceField.setWidthFull();
		priceField.setClearButtonVisible(true);

		Span pricePreview = new Span();
		pricePreview.getStyle()
				.set("color", "var(--lumo-secondary-text-color)")
				.set("font-size", "var(--lumo-font-size-s)");
		priceField.addValueChangeListener(e -> {
			Integer cents = e.getValue();
			pricePreview.setText(cents != null ? PenaltyTypesHandler.formatCentsAsEuro(cents) : "");
		});

		Checkbox defaultToggle = new Checkbox("Default type");

		if (existing != null) {
			nameField.setValue(existing.getDisplayName() != null ? existing.getDisplayName() : "");
			priceField.setValue(existing.getPrice());
			defaultToggle.setValue(existing.isDefaultType());
		}

		FormLayout form = new FormLayout();
		form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
		form.add(nameField, priceField, pricePreview, defaultToggle);

		Button saveBtn = new Button("Save");
		saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveBtn.addClickListener(e -> {
			String name = nameField.getValue();
			if (name == null || name.isBlank()) {
				nameField.setInvalid(true);
				nameField.setErrorMessage("Name is required");
				return;
			}
			if (handler.isNameDuplicate(name, existing)) {
				nameField.setInvalid(true);
				nameField.setErrorMessage("A penalty type with this name already exists");
				return;
			}

			if (existing != null) {
				handler.updateEntry(existing, name, defaultToggle.getValue(), priceField.getValue());
			} else {
				handler.addEntry(name, defaultToggle.getValue(), priceField.getValue());
			}

			grid.getDataProvider().refreshAll();
			updateButtonStates();
			dialog.close();
		});

		Button cancelBtn = new Button("Cancel", e -> dialog.close());

		dialog.getFooter().add(cancelBtn, saveBtn);
		dialog.add(form);
		dialog.open();
	}

	private void confirmDelete(PenaltyTypesHandler.PenaltyTypeEntry entry) {
		ConfirmDialog dialog = new ConfirmDialog();
		dialog.setHeader("Delete Penalty Type");
		dialog.setText("Are you sure you want to delete \"%s\"?".formatted(entry.getDisplayName()));
		dialog.setCancelable(true);
		dialog.setConfirmText("Delete");
		dialog.setConfirmButtonTheme("error primary");
		dialog.addConfirmListener(e -> {
			handler.markForDeletion(entry);
			grid.getDataProvider().refreshAll();
			updateButtonStates();
		});
		dialog.open();
	}

	private void refreshGrid() {
		grid.getDataProvider().refreshAll();
		updateButtonStates();
	}

	private void updateButtonStates() {
		boolean dirty = handler.isDirty();
		saveButton.setEnabled(dirty);
		cancelButton.setVisible(dirty);
		addButton.setEnabled(handler.canAddMore());
	}

	@Override
	public void applyInitialState() {
		handler.resetWorkingCopy();
		if (grid != null) {
			grid.getDataProvider().refreshAll();
		}
		updateButtonStates();
	}
}
