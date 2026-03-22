package com.esemudeo.quarkus.penaltybot.configuration.auth.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType
) {
}
