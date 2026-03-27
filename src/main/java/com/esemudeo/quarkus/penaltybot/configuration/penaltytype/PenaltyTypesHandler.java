package com.esemudeo.quarkus.penaltybot.configuration.penaltytype;

import com.esemudeo.quarkus.penaltybot.configuration.SettingsService;
import com.esemudeo.quarkus.penaltybot.configuration.penaltytype.model.PenaltyType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PenaltyTypesHandler {

	private static final int MAX_PENALTY_TYPES = 15;
	private static final int CENTS_PER_EURO = 100;
	private static final String EURO_FORMAT = "%d.%02d €";

	private final SettingsService settingsService;
	private List<PenaltyType> initialPenaltyTypes;
	private final List<PenaltyTypeEntry> workingCopy = new ArrayList<>();

	public PenaltyTypesHandler(List<PenaltyType> initialPenaltyTypes, SettingsService settingsService) {
		this.settingsService = settingsService;
		this.initialPenaltyTypes = initialPenaltyTypes;
		resetWorkingCopy();
	}

	public List<PenaltyTypeEntry> getWorkingCopy() {
		return workingCopy;
	}

	public boolean canAddMore() {
		return workingCopy.size() < MAX_PENALTY_TYPES;
	}

	public boolean canDeleteOrToggle() {
		return workingCopy.size() > 1;
	}

	public void addEntry(String displayName, boolean isDefault, Integer price) {
		if (isDefault) {
			clearAllDefaults(null);
		}
		workingCopy.add(PenaltyTypeEntry.builder()
				.displayName(displayName)
				.defaultType(isDefault)
				.price(price)
				.active(true)
				.build());
		enforceConstraints();
	}

	public void updateEntry(PenaltyTypeEntry entry, String displayName, boolean isDefault, Integer price) {
		if (isDefault) {
			clearAllDefaults(entry);
		}
		entry.setDisplayName(displayName);
		entry.setPrice(price);
		entry.setDefaultType(isDefault);
		enforceConstraints();
	}

	public void markForDeletion(PenaltyTypeEntry entry) {
		entry.setPendingDelete(true);
		workingCopy.remove(entry);
		enforceConstraints();
	}

	public void setActive(PenaltyTypeEntry entry, boolean active) {
		entry.setActive(active);
		enforceConstraints(entry);
	}

	public void setDefault(PenaltyTypeEntry entry, boolean isDefault) {
		if (isDefault) {
			clearAllDefaults(entry);
		}
		entry.setDefaultType(isDefault);
		enforceConstraints(entry);
	}

	public boolean isNameDuplicate(String name, PenaltyTypeEntry excludeEntry) {
		return workingCopy.stream()
				.filter(e -> e != excludeEntry)
				.anyMatch(e -> name.equalsIgnoreCase(e.getDisplayName()));
	}

	public boolean isDirty() {
		if (workingCopy.size() != initialPenaltyTypes.size()) {
			return true;
		}
		for (PenaltyTypeEntry entry : workingCopy) {
			if (entry.getEntityId() == null) {
				return true;
			}
			PenaltyType original = findOriginalById(entry.getEntityId());
			if (original == null || entryDiffersFromEntity(entry, original)) {
				return true;
			}
		}
		return false;
	}

	public void save() {
		List<Long> workingIds = workingCopy.stream()
				.map(PenaltyTypeEntry::getEntityId)
				.filter(Objects::nonNull)
				.toList();
		for (PenaltyType pt : initialPenaltyTypes) {
			if (!workingIds.contains(pt.id)) {
				settingsService.makePenaltyTypeUnusable(pt.id);
			}
		}

		for (PenaltyTypeEntry entry : workingCopy) {
			if (entry.getEntityId() == null) {
				settingsService.createPenaltyType(entry.getDisplayName(), entry.isDefaultType(), entry.getPrice());
			}
		}

		for (PenaltyTypeEntry entry : workingCopy) {
			if (entry.getEntityId() != null) {
				PenaltyType original = findOriginalById(entry.getEntityId());
				if (original != null && entryDiffersFromEntity(entry, original)) {
					settingsService.updatePenaltyType(
							entry.getEntityId(), entry.getDisplayName(), entry.isDefaultType(), entry.getPrice(), entry.isActive()
					);
				}
			}
		}

		initialPenaltyTypes = settingsService.getAllPenaltyTypes();
		resetWorkingCopy();
	}

	public void cancelChanges() {
		resetWorkingCopy();
	}

	public void resetWorkingCopy() {
		workingCopy.clear();
		for (PenaltyType pt : initialPenaltyTypes) {
			workingCopy.add(PenaltyTypeEntry.fromEntity(pt));
		}
	}

	private void clearAllDefaults(PenaltyTypeEntry exclude) {
		for (PenaltyTypeEntry entry : workingCopy) {
			if (entry != exclude) {
				entry.setDefaultType(false);
			}
		}
	}

	private void enforceConstraints(PenaltyTypeEntry excludeFromFallback) {
		if (workingCopy.isEmpty()) {
			return;
		}

		boolean hasActive = workingCopy.stream().anyMatch(PenaltyTypeEntry::isActive);
		if (!hasActive) {
			workingCopy.stream()
					.filter(entry -> entry != excludeFromFallback)
					.findFirst()
					.ifPresent(entry -> entry.setActive(true));
		}

		PenaltyTypeEntry currentDefault = workingCopy.stream()
				.filter(PenaltyTypeEntry::isDefaultType)
				.findFirst()
				.orElse(null);

		if (currentDefault != null && !currentDefault.isActive()) {
			currentDefault.setDefaultType(false);
			currentDefault = null;
		}

		if (currentDefault == null) {
			workingCopy.stream()
					.filter(PenaltyTypeEntry::isActive)
					.filter(entry -> entry != excludeFromFallback)
					.findFirst()
					.ifPresent(entry -> entry.setDefaultType(true));
		}
	}

	private void enforceConstraints() {
		enforceConstraints(null);
	}

	private PenaltyType findOriginalById(Long id) {
		return initialPenaltyTypes.stream()
				.filter(pt -> pt.id.equals(id))
				.findFirst()
				.orElse(null);
	}

	private boolean entryDiffersFromEntity(PenaltyTypeEntry entry, PenaltyType entity) {
		return !Objects.equals(entry.getDisplayName(), entity.getDisplayName())
				|| entry.isDefaultType() != entity.isDefaultType()
				|| !Objects.equals(entry.getPrice(), entity.getPrice())
				|| entry.isActive() != entity.isActive();
	}

	public static String formatCentsAsEuro(int cents) {
		return EURO_FORMAT.formatted(cents / CENTS_PER_EURO, Math.abs(cents % CENTS_PER_EURO));
	}

	@Getter
	@Builder
	public static class PenaltyTypeEntry {
		private final Long entityId;
		@Setter private String displayName;
		@Setter private boolean defaultType;
		@Setter private Integer price;
		@Setter private boolean active;
		@Setter private boolean pendingDelete;

		static PenaltyTypeEntry fromEntity(PenaltyType pt) {
			return PenaltyTypeEntry.builder()
					.entityId(pt.id)
					.displayName(pt.getDisplayName())
					.defaultType(pt.isDefaultType())
					.price(pt.getPrice())
					.active(pt.isActive())
					.pendingDelete(false)
					.build();
		}
	}
}
