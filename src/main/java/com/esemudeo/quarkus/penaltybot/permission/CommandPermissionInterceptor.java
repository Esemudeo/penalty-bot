package com.esemudeo.quarkus.penaltybot.permission;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import com.esemudeo.quarkus.penaltybot.shared.command.UserContextMenuCommand;

@RequiresCommandPermission
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class CommandPermissionInterceptor {

	@Inject
	PermissionService permissionService;

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

		if (!permissionService.isAllowedForCommand(guild, member, commandName)) {
			event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
			return null;
		}

		return ctx.proceed();
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
