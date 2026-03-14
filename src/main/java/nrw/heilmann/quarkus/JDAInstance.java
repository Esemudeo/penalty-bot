package nrw.heilmann.quarkus;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import nrw.heilmann.quarkus.bot.commands.ReportTeamkillsCommand;
import nrw.heilmann.quarkus.bot.commands.ShowTeamkillsCommand;
import nrw.heilmann.quarkus.bot.listeners.SlashCommandListener;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class JDAInstance {

	private JDA jda;

	@ConfigProperty(name = "discord.bot-token")
	String discordToken;

	@Inject
	SlashCommandListener slashCommandListener;

	@Inject
	ReportTeamkillsCommand reportTeamkillsCommand;

	@Inject
	ShowTeamkillsCommand showTeamkillsCommand;

	@Inject
	Logger log;

	void onStart(@Observes StartupEvent event) {

		if (discordToken == null || discordToken.isBlank()) {
			log.error("Discord bot token is not set! Please set the 'discord.bot-token' configuration property.");
			return;
		}

		try {
			jda = JDABuilder.createDefault(discordToken).build();
			jda.awaitReady();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		log.info("✓ JDA is ready!");

		registerSlashCommands();
	}

	private void registerSlashCommands() {
		jda.updateCommands()
				.addCommands(reportTeamkillsCommand.toCommandData())
				.addCommands(showTeamkillsCommand.toCommandData())
				.queue();

		jda.addEventListener(slashCommandListener);
	}

	@PreDestroy
	void onStop() {
		if (jda != null) {
			jda.shutdown();
			log.info("JDA was stopped");
		}
	}

	public JDA getJDA() {
		return jda;
	}
}

