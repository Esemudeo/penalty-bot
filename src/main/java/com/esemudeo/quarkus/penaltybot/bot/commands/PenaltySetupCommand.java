package com.esemudeo.quarkus.penaltybot.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import com.esemudeo.quarkus.penaltybot.bot.exceptions.NotInGuildException;
import com.esemudeo.quarkus.penaltybot.bot.permissions.RequiresCommandPermission;
import com.esemudeo.quarkus.penaltybot.persistence.model.ConfigSessionToken;
import com.esemudeo.quarkus.penaltybot.persistence.repository.ConfigSessionTokenRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@RequiresCommandPermission
@ApplicationScoped
public class PenaltySetupCommand extends SlashCommand {


	private static final Logger LOG = Logger.getLogger(PenaltySetupCommand.class);

	private static final int TOKEN_VALIDITY_MINUTES = 10;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	@Inject
	ConfigSessionTokenRepository configSessionTokenRepository;

	@ConfigProperty(name = "app.base-url")
	String baseUrl;

	@Override
	public String getName() {
		return "penalty-setup";
	}

	@Override
	protected String getDescription() {
		return "Open the configuration page for this server.";
	}

	@Override
	public void handleCommand(SlashCommandInteractionEvent event) {
		try {
			Guild guild = event.getGuild();
			if (guild == null) {
				throw new NotInGuildException();
			}

			long userId = event.getUser().getIdLong();
			long guildId = guild.getIdLong();

			configSessionTokenRepository.invalidateAllForUser(userId);

			String token = generateToken();
			configSessionTokenRepository.save(ConfigSessionToken.builder()
					.userId(userId)
					.guildId(guildId)
					.token(token)
					.expiresAt(Instant.now().plus(TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES))
					.used(false)
					.build());

			String configUrl = baseUrl + "/configure?token=" + token;
			event.reply("Here is your configuration link (valid for %d minutes): <%s>".formatted(TOKEN_VALIDITY_MINUTES, configUrl))
					.setEphemeral(true)
					.queue();
		} catch (NotInGuildException e) {
			event.reply("Command wasn't executed inside a server.").setEphemeral(true).queue();
		} catch (Exception e) {
			event.reply("An error occurred while processing the command.").setEphemeral(true).queue();
			LOG.error("Error handling penalty-setup command", e);
		}
	}

	private static String generateToken() {
		byte[] bytes = new byte[32];
		SECURE_RANDOM.nextBytes(bytes);
		return HexFormat.of().formatHex(bytes);
	}
}
