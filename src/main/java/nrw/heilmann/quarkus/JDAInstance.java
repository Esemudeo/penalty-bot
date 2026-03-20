package nrw.heilmann.quarkus;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import nrw.heilmann.quarkus.bot.commands.PenaltySummaryCommand;
import nrw.heilmann.quarkus.bot.commands.ReportPenaltyCommand;
import nrw.heilmann.quarkus.bot.commands.ShowPenaltiesCommand;
import nrw.heilmann.quarkus.bot.listeners.GuildReadyListener;
import nrw.heilmann.quarkus.bot.listeners.PenaltySummaryModalListener;
import nrw.heilmann.quarkus.bot.listeners.ReportPenaltyModalListener;
import nrw.heilmann.quarkus.bot.listeners.ShowPenaltiesModalListener;
import nrw.heilmann.quarkus.bot.listeners.SlashCommandListener;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class JDAInstance {

	private JDA jda;

	@ConfigProperty(name = "discord.bot-token")
	String discordToken;

	@Inject
	GuildReadyListener guildReadyListener;

	@Inject
	SlashCommandListener slashCommandListener;

	@Inject
	ReportPenaltyCommand reportPenaltyCommand;

	@Inject
	ReportPenaltyModalListener reportPenaltyModalListener;

	@Inject
	ShowPenaltiesCommand showPenaltiesCommand;

	@Inject
	ShowPenaltiesModalListener showPenaltiesModalListener;

	@Inject
	PenaltySummaryCommand penaltySummaryCommand;

	@Inject
	PenaltySummaryModalListener penaltySummaryModalListener;

	@Inject
	Logger log;


	void onStart(@Observes StartupEvent event) {

		if (discordToken == null || discordToken.isBlank()) {
			log.error("Discord bot token is not set! Please set the 'DISCORD_BOT_TOKEN' environment variable.");
			return;
		}

		try {
			jda = JDABuilder.createDefault(discordToken)
					.addEventListeners(guildReadyListener, slashCommandListener, reportPenaltyModalListener, showPenaltiesModalListener,
							penaltySummaryModalListener)
					.build();
			jda.awaitReady();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		log.info("✓ JDA is ready!");

		registerSlashCommands();
	}

	private void registerSlashCommands() {
		jda.updateCommands()
				.addCommands(reportPenaltyCommand.toCommandData())
				.addCommands(showPenaltiesCommand.toCommandData())
				.addCommands(penaltySummaryCommand.toCommandData())
				.queue();
	}

	@PreDestroy
	void onStop() {
		if (jda != null) {
			jda.shutdown();
			log.info("JDA was stopped");
		}
	}
}

