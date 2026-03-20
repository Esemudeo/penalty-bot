package nrw.heilmann.quarkus.bot.permissions;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import nrw.heilmann.quarkus.bot.persistence.model.Command;
import nrw.heilmann.quarkus.bot.persistence.model.CommandExplicitRole;
import nrw.heilmann.quarkus.bot.persistence.repository.CommandRepository;
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
		SlashCommandInteractionEvent event = findEvent(ctx);
		if (event == null) {
			return ctx.proceed();
		}

		Guild guild = event.getGuild();
		Member member = event.getMember();

		if (guild == null || member == null) {
			event.reply("This command can only be used inside a server.").setEphemeral(true).queue();
			return null;
		}

		if (!isAllowed(guild, member, event.getName())) {
			event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
			return null;
		}

		return ctx.proceed();
	}

	private boolean isAllowed(Guild guild, Member member, String commandName) {
		Optional<Command> configOpt = commandPermissionRepository.findByGuildAndCommand(guild.getIdLong(), commandName);

		if (configOpt.isEmpty()) {
			LOG.debug("No permission config for guild=%d command=%s; DENY_IF_UNCONFIGURED=%b"
					.formatted(guild.getIdLong(), commandName, DENY_IF_UNCONFIGURED));
			return !DENY_IF_UNCONFIGURED;
		}

		Command config = configOpt.get();

		if (config.getMinRoleId() != null && permissionService.hasMinRole(guild, member, config.getMinRoleId())) {
			return true;
		}

		return permissionService.hasExplicitRole(member,
				config.getExplicitRoles().stream().map(CommandExplicitRole::getRoleId).toList());
	}

	private static SlashCommandInteractionEvent findEvent(InvocationContext ctx) {
		for (Object param : ctx.getParameters()) {
			if (param instanceof SlashCommandInteractionEvent e) {
				return e;
			}
		}
		return null;
	}
}
