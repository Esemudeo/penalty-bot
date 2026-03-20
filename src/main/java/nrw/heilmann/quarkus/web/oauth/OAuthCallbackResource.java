package nrw.heilmann.quarkus.web.oauth;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import nrw.heilmann.quarkus.persistence.model.ConfigSessionToken;
import nrw.heilmann.quarkus.persistence.repository.ConfigSessionTokenRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.net.URI;
import java.util.Optional;

@Path("/oauth/callback")
public class OAuthCallbackResource {

    @Inject
    Logger log;

    @Inject
    ConfigSessionTokenRepository configSessionTokenRepository;

    @RestClient
    DiscordOAuthClient discordOAuthClient;

    @ConfigProperty(name = "discord.oauth.client-id")
    String clientId;

    @ConfigProperty(name = "discord.oauth.client-secret")
    String clientSecret;

    @ConfigProperty(name = "discord.oauth.redirect-uri")
    String redirectUri;

    @GET
    @Transactional
    public Response callback(@QueryParam("code") String code, @QueryParam("state") String state) {
        if (code == null || state == null) {
            log.warn("OAuth callback called without code or state");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // state is the config session token we generated and passed to Discord.
        // Discord echoes it back unchanged as a CSRF protection mechanism.
        Optional<ConfigSessionToken> tokenOpt = configSessionTokenRepository.findValidByToken(state);
        if (tokenOpt.isEmpty()) {
            log.warn("OAuth callback: no valid session token found for state=%s".formatted(state));
            return Response.seeOther(URI.create("/error")).build();
        }

        ConfigSessionToken sessionToken = tokenOpt.get();

        DiscordTokenResponse tokenResponse;
        try {
            tokenResponse = discordOAuthClient.exchangeCode(clientId, clientSecret, "authorization_code", code, redirectUri);
        } catch (Exception e) {
            log.error("OAuth callback: Discord token exchange failed", e);
            return Response.seeOther(URI.create("/error")).build();
        }

        DiscordUser discordUser;
        try {
            discordUser = discordOAuthClient.getCurrentUser("Bearer " + tokenResponse.accessToken());
        } catch (Exception e) {
            log.error("OAuth callback: Discord user fetch failed", e);
            return Response.seeOther(URI.create("/error")).build();
        }

        long discordUserId;
        try {
            discordUserId = Long.parseLong(discordUser.id());
        } catch (NumberFormatException e) {
            log.error("OAuth callback: Discord user ID is not a valid long: %s".formatted(discordUser.id()));
            return Response.seeOther(URI.create("/error")).build();
        }

        if (discordUserId != sessionToken.getUserId()) {
            log.warn("OAuth callback: user mismatch – expected %d, got %d".formatted(sessionToken.getUserId(), discordUserId));
            return Response.seeOther(URI.create("/error")).build();
        }

        sessionToken.setUsed(true);

        return Response.seeOther(URI.create("/settings?userId=" + discordUserId + "&guildId=" + sessionToken.getGuildId())).build();
    }
}