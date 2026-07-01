package com.silasadinoyi.kolekta.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nomba")
public record NombaProperties (
        String baseUrl,
        String accountId,
        String subAccountId,
        String clientId,
        String clientSecret,
        String webhookSecret,
        int tokenRefreshMarginSeconds
){}
