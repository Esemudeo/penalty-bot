package nrw.heilmann.quarkus.bot.permissions;

import jakarta.enterprise.context.ApplicationScoped;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jboss.logging.Logger;

import java.util.Collection;

@ApplicationScoped
public class CommandPermissionService {

	private static final Logger LOG = Logger.getLogger(CommandPermissionService.class);

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
