package com.silasadinoyi.kolekta.nomba.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TokenResponse(
        String code,
        String description,
        TokenData data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TokenData(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") String expiresIn,
            @JsonProperty("refresh_token") String refreshToken
    ) {}
}
