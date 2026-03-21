package com.esemudeo.quarkus.penaltybot.bot.commands;

import io.quarkus.test.junit.QuarkusTest;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@QuarkusTest
class ReportPenaltyCommandTest {

	private static final String ERROR_NOT_ON_GUILD = "Command wasn't executed inside a server.";

	private static final ReportPenaltyCommand UNDER_TEST = new ReportPenaltyCommand();


	SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);
	ReplyCallbackAction reply = mock(ReplyCallbackAction.class);


	@BeforeEach
	void init() {
		Mockito.reset(event, reply);
	}

	@Nested
	class NotInGuildReply {

		@BeforeEach
		void init() {
			Mockito.reset(event, reply);
			when(event.reply(ERROR_NOT_ON_GUILD)).thenReturn(reply);
			when(reply.setEphemeral(true)).thenReturn(reply);
		}

		@Test
		void whenAuthorNotResolvable() {
			when(event.getMember()).thenReturn(null);

			UNDER_TEST.handleCommand(event);

			verifyNotInGuildReply();
		}

		@Test
		void whenMentionedMemberNotResolvable() {
			givenValidMember();
			OptionMapping someOptionMapping = givenMemberOptionMapping();
			when(someOptionMapping.getAsMember()).thenReturn(null);

			UNDER_TEST.handleCommand(event);

			verifyNotInGuildReply();
		}

		@Test
		void whenGuildIsNotResolvable() {
			Member member = givenValidMember();
			OptionMapping someOption = givenMemberOptionMapping();
			when(someOption.getAsMember()).thenReturn(member);
			when(event.getGuild()).thenReturn(null);

			UNDER_TEST.handleCommand(event);

			verifyNotInGuildReply();
		}

		private void verifyNotInGuildReply() {
			verify(event).reply(ERROR_NOT_ON_GUILD);
			verify(reply).setEphemeral(true);
			verify(reply).queue();
		}


	}

	private OptionMapping givenMemberOptionMapping() {
		OptionMapping someOptionMapping = mock(OptionMapping.class);
		when(event.getOption("member")).thenReturn(someOptionMapping);
		return someOptionMapping;
	}

	private Member givenValidMember() {
		Member member = mock(Member.class);
		when(event.getMember()).thenReturn(member);
		return member;
	}


}
