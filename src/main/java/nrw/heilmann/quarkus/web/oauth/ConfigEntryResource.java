package nrw.heilmann.quarkus.web.oauth;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import nrw.heilmann.quarkus.persistence.repository.ConfigSessionTokenRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Path("/configure")
public class ConfigEntryResource {

    @Inject
    ConfigSessionTokenRepository configSessionTokenRepository;

    @ConfigProperty(name = "discord.oauth.client-id")
    String clientId;

    @ConfigProperty(name = "discord.oauth.redirect-uri")
    String redirectUri;

    @GET
    public Response enter(@QueryParam("token") String token) {
        if (token == null || configSessionTokenRepository.findValidByToken(token).isEmpty()) {
            return Response.seeOther(URI.create("/error")).build();
        }

        String discordOAuthUrl = "https://discord.com/oauth2/authorize"
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&scope=identify"
                + "&state=" + token;

        return Response.seeOther(URI.create(discordOAuthUrl)).build();
    }
}
