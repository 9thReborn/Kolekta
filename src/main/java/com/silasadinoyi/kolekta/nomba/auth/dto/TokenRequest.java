package com.silasadinoyi.kolekta.nomba.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenRequest(
        @JsonProperty("grant_type") String grantType,
        @JsonProperty("client_id") String clientId,
        @JsonProperty("client_secret") String clientSecret
){
    public static TokenRequest clientCredentials(String clientId, String clientSecret) {
        return new TokenRequest("client_credentials", clientId, clientSecret);
    }
}