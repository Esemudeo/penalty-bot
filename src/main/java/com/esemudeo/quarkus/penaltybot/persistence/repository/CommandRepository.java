package com.esemudeo.quarkus.penaltybot.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import com.esemudeo.quarkus.penaltybot.persistence.model.Command;

import java.util.Optional;

@ApplicationScoped
@Transactional
public class CommandRepository {

	public Optional<Command> findByGuildAndCommand(long guildId, String commandName) {
		return Command.find("guildId = ?1 and commandName = ?2", guildId, commandName).firstResultOptional();
	}

	public void persistIfAbsent(long guildId, String commandName) {
		if (findByGuildAndCommand(guildId, commandName).isEmpty()) {
			Command.builder()
					.guildId(guildId)
					.commandName(commandName)
					.build()
					.persist();
		}
	}
}
