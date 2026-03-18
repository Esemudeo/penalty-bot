package nrw.heilmann.quarkus.bot.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import nrw.heilmann.quarkus.bot.persistence.model.CommandPermission;

import java.util.Optional;

@ApplicationScoped
@Transactional
public class CommandPermissionRepository {

	public Optional<CommandPermission> findByGuildAndCommand(long guildId, String commandName) {
		return CommandPermission.find("guildId = ?1 and commandName = ?2", guildId, commandName).firstResultOptional();
	}

	public void persistIfAbsent(long guildId, String commandName) {
		if (findByGuildAndCommand(guildId, commandName).isEmpty()) {
			CommandPermission.builder()
					.guildId(guildId)
					.commandName(commandName)
					.build()
					.persist();
		}
	}
}
