package com.esemudeo.quarkus.penaltybot.permission;

import com.esemudeo.quarkus.penaltybot.configuration.commandpermission.model.CommandExplicitRole;
import com.esemudeo.quarkus.penaltybot.configuration.commandpermission.model.CommandPermission;
import com.esemudeo.quarkus.penaltybot.configuration.commandpermission.repository.CommandRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jboss.logging.Logger;

import java.util.Collection;
import java.util.Optional;

@ApplicationScoped
public class PermissionService {

	private static final boolean DENY_IF_UNCONFIGURED = true;

	private static final Logger LOG = Logger.getLogger(PermissionService.class);

	@Inject
	CommandRepository commandRepository;

	public boolean isAllowedForCommand(Guild guild, Member member, String commandName) {
		if (member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR)) {
			return true;
		}

		Optional<CommandPermission> commandOpt = commandRepository.findByGuildAndCommand(guild.getIdLong(), commandName);
		if (commandOpt.isEmpty()) {
			LOG.debug("No permission config for guild=%d command=%s; DENY_IF_UNCONFIGURED=%b"
					.formatted(guild.getIdLong(), commandName, DENY_IF_UNCONFIGURED));
			return !DENY_IF_UNCONFIGURED;
		}

		CommandPermission command = commandOpt.get();
		if (command.getMinRoleId() != null && hasMinRole(guild, member, command.getMinRoleId())) {
			return true;
		}

		return hasExplicitRole(member,
				command.getExplicitRoles().stream().map(CommandExplicitRole::getRoleId).toList());
	}

	public boolean hasMinRole(Guild guild, Member member, Long minRoleId) {
		Role minRole = guild.getRoleById(minRoleId);
		if (minRole == null) {
			LOG.warn("Role %d no longer exists in guild %d".formatted(minRoleId, guild.getIdLong()));
			return false;
		}
		int minPosition = minRole.getPosition();
		return member.getRoles().stream().anyMatch(role -> role.getPosition() >= minPosition);
	}

	public boolean hasExplicitRole(Member member, Collection<Long> explicitRoleIds) {
		return member.getRoles().stream()
				.map(Role::getIdLong)
				.anyMatch(explicitRoleIds::contains);
	}
}
