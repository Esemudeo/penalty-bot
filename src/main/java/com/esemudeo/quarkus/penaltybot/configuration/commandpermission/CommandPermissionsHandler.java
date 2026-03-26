package com.esemudeo.quarkus.penaltybot.configuration.commandpermission;

import com.esemudeo.quarkus.penaltybot.configuration.SettingsService;
import com.esemudeo.quarkus.penaltybot.configuration.commandpermission.model.CommandExplicitRole;
import com.esemudeo.quarkus.penaltybot.configuration.commandpermission.model.CommandPermission;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandPermissionsHandler {

	private final SettingsService settingsService;
	private final List<CommandPermission> commandPermissions;
	private final List<CommandPermissionState> states = new ArrayList<>();

	private List<SettingsService.GuildRole> cachedRoles;

	public CommandPermissionsHandler(List<CommandPermission> commandPermissions, SettingsService settingsService) {
		this.settingsService = settingsService;
		this.commandPermissions = commandPermissions;

		for (CommandPermission cp : commandPermissions) {
			CommandPermissionState state = new CommandPermissionState();
			state.setCommandName(cp.getCommandName());
			state.setInitialMinRoleId(cp.getMinRoleId());
			state.setInitialExplicitRoleIds(cp.getExplicitRoles().stream()
					.map(CommandExplicitRole::getRoleId)
					.collect(Collectors.toSet()));
			state.setCurrentMinRoleId(cp.getMinRoleId());
			state.setCurrentExplicitRoleIds(new HashSet<>(state.getInitialExplicitRoleIds()));
			states.add(state);
		}
	}

	public List<CommandPermission> getCommandPermissions() {
		return commandPermissions;
	}

	public List<CommandPermissionState> getStates() {
		return states;
	}

	public void updateCurrentMinRole(String commandName, Long roleId) {
		findState(commandName).ifPresent(s -> s.setCurrentMinRoleId(roleId));
	}

	public void updateCurrentExplicitRoles(String commandName, Set<Long> roleIds) {
		findState(commandName).ifPresent(s -> s.setCurrentExplicitRoleIds(roleIds));
	}

	public boolean isDirty() {
		for (CommandPermissionState state : states) {
			if (!Objects.equals(state.getCurrentMinRoleId(), state.getInitialMinRoleId())
					|| !state.getCurrentExplicitRoleIds().equals(state.getInitialExplicitRoleIds())) {
				return true;
			}
		}
		return false;
	}

	public void save() {
		for (CommandPermissionState state : states) {
			settingsService.updateCommandMinRole(state.getCommandName(), state.getCurrentMinRoleId());
			settingsService.replaceCommandExplicitRoles(state.getCommandName(),
					state.getCurrentExplicitRoleIds().stream().toList());

			state.setInitialMinRoleId(state.getCurrentMinRoleId());
			state.setInitialExplicitRoleIds(new HashSet<>(state.getCurrentExplicitRoleIds()));
		}
	}

	public void clearRolesCache() {
		cachedRoles = null;
	}

	public List<SettingsService.GuildRole> ensureRolesLoaded() {
		if (cachedRoles == null) {
			cachedRoles = settingsService.getGuildRoles();
		}
		return cachedRoles;
	}

	public Optional<SettingsService.GuildRole> getGuildRoleById(long id) {
		return settingsService.getGuildRoleById(id);
	}

	public List<SettingsService.GuildRole> getGuildRolesByIds(Set<Long> ids) {
		return settingsService.getGuildRolesByIds(ids);
	}

	private Optional<CommandPermissionState> findState(String commandName) {
		return states.stream().filter(s -> s.getCommandName().equals(commandName)).findFirst();
	}

	@Getter
	@Setter
	public static class CommandPermissionState {
		private String commandName;
		private Long initialMinRoleId;
		private Set<Long> initialExplicitRoleIds;
		private Long currentMinRoleId;
		private Set<Long> currentExplicitRoleIds;
	}
}
