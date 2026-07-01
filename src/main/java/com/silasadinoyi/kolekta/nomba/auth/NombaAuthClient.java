package com.silasadinoyi.kolekta.nomba.auth;

import com.silasadinoyi.kolekta.config.NombaProperties;
import com.silasadinoyi.kolekta.nomba.auth.dto.TokenRequest;
import com.silasadinoyi.kolekta.nomba.auth.dto.TokenResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NombaAuthClient {
    private final RestClient nombaRestClient;
    private final NombaProperties props;

    public NombaAuthClient(RestClient nombaRestClient, NombaProperties props) {
        this.nombaRestClient = nombaRestClient;
        this.props = props;
    }

    public TokenResponse issueToken() {
        return nombaRestClient.post()
                .uri("/auth/token/issue")
                .header("accountId", props.accountId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(TokenRequest.clientCredentials(props.clientId(), props.clientSecret()))
                .retrieve()
                .body(TokenResponse.class);
    }
}
