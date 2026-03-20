package nrw.heilmann.quarkus.web.oauth;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri = "https://discord.com/api")
public interface DiscordOAuthClient {

    @POST
    @Path("/oauth2/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    DiscordTokenResponse exchangeCode(
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("grant_type") String grantType,
            @FormParam("code") String code,
            @FormParam("redirect_uri") String redirectUri
    );

    @GET
    @Path("/users/@me")
    DiscordUser getCurrentUser(@HeaderParam("Authorization") String bearerToken);
}