package com.esemudeo.quarkus.penaltybot.shared;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import com.esemudeo.quarkus.penaltybot.configuration.command.PenaltySetupCommand;
import com.esemudeo.quarkus.penaltybot.penalty.command.PenaltySummaryCommand;
import com.esemudeo.quarkus.penaltybot.penalty.command.ReportPenaltyCommand;
import com.esemudeo.quarkus.penaltybot.penalty.command.ShowPenaltiesCommand;
import com.esemudeo.quarkus.penaltybot.shared.init.GuildReadyListener;
import com.esemudeo.quarkus.penaltybot.penalty.listener.PenaltySummaryModalListener;
import com.esemudeo.quarkus.penaltybot.penalty.listener.ReportPenaltyModalListener;
import com.esemudeo.quarkus.penaltybot.penalty.listener.ShowPenaltiesModalListener;
import com.esemudeo.quarkus.penaltybot.shared.listener.SlashCommandListener;
import com.esemudeo.quarkus.penaltybot.shared.listener.UserContextMenuCommandListener;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class JDAInstance {

	@Getter
	private JDA jda;

	@ConfigProperty(name = "discord.bot-token")
	String discordToken;

	@Inject
	GuildReadyListener guildReadyListener;

	@Inject
	SlashCommandListener slashCommandListener;

	@Inject
	UserContextMenuCommandListener userContextMenuCommandListener;

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
	PenaltySetupCommand penaltySetupCommand;

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
					.addEventListeners(guildReadyListener, slashCommandListener, userContextMenuCommandListener,
							reportPenaltyModalListener, showPenaltiesModalListener, penaltySummaryModalListener)
					.build();
			jda.awaitReady();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		log.info("✓ JDA is ready!");

		registerCommands();
	}

	private void registerCommands() {
		jda.getGuilds().forEach(guild -> guild.updateCommands().queue());
		jda.updateCommands()
				.addCommands(reportPenaltyCommand.toSlashCommandData())
				.addCommands(reportPenaltyCommand.toContextMenuCommandData())
				.addCommands(showPenaltiesCommand.toSlashCommandData())
				.addCommands(showPenaltiesCommand.toContextMenuCommandData())
				.addCommands(penaltySummaryCommand.toSlashCommandData())
				.addCommands(penaltySetupCommand.toSlashCommandData())
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