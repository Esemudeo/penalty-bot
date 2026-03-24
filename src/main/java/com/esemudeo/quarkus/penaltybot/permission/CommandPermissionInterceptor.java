package com.esemudeo.quarkus.penaltybot.permission;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import com.esemudeo.quarkus.penaltybot.configuration.commandpermission.model.CommandExplicitRole;
import com.esemudeo.quarkus.penaltybot.configuration.commandpermission.model.CommandPermission;
import com.esemudeo.quarkus.penaltybot.configuration.commandpermission.repository.CommandRepository;
import com.esemudeo.quarkus.penaltybot.shared.command.UserContextMenuCommand;
import org.jboss.logging.Logger;

import java.util.Optional;

@RequiresCommandPermission
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class CommandPermissionInterceptor {

	private static final boolean DENY_IF_UNCONFIGURED = true;

	private static final Logger LOG = Logger.getLogger(CommandPermissionInterceptor.class);

	@Inject
	PermissionService permissionService;

	@Inject
	CommandRepository commandPermissionRepository;

	@AroundInvoke
	public Object checkPermission(InvocationContext ctx) throws Exception {
		GenericCommandInteractionEvent event = findEvent(ctx);
		if (event == null) {
			return ctx.proceed();
		}

		Guild guild = event.getGuild();
		Member member = event.getMember();

		if (guild == null || member == null) {
			event.reply("This command can only be used inside a server.").setEphemeral(true).queue();
			return null;
		}

		String commandName = ctx.getTarget() instanceof UserContextMenuCommand c
				? c.getName()
				: event.getName();

		if (!isAllowed(guild, member, commandName)) {
			event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
			return null;
		}

		return ctx.proceed();
	}

	private boolean isAllowed(Guild guild, Member member, String commandName) {
		if (member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR)) {
			return true;
		}

		Optional<CommandPermission> commandOpt = commandPermissionRepository.findByGuildAndCommand(guild.getIdLong(), commandName);
		if (commandOpt.isEmpty()) {
			LOG.debug("No permission config for guild=%d command=%s; DENY_IF_UNCONFIGURED=%b"
					.formatted(guild.getIdLong(), commandName, DENY_IF_UNCONFIGURED));
			return !DENY_IF_UNCONFIGURED;
		}

		CommandPermission command = commandOpt.get();
		if (command.getMinRoleId() != null && permissionService.hasMinRole(guild, member, command.getMinRoleId())) {
			return true;
		}

		return permissionService.hasExplicitRole(member,
				command.getExplicitRoles().stream().map(CommandExplicitRole::getRoleId).toList());
	}

	private static GenericCommandInteractionEvent findEvent(InvocationContext ctx) {
		for (Object param : ctx.getParameters()) {
			if (param instanceof GenericCommandInteractionEvent e) {
				return e;
			}
		}
		return null;
	}
}
