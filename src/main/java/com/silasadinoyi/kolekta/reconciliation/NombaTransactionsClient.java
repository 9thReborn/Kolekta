package com.silasadinoyi.kolekta.reconciliation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.silasadinoyi.kolekta.config.NombaProperties;
import com.silasadinoyi.kolekta.nomba.auth.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Component
public class NombaTransactionsClient {

    private static final Logger log = LoggerFactory.getLogger(NombaTransactionsClient.class);

    private final RestClient nombaRestClient;
    private final TokenManager tokenManager;
    private final NombaProperties props;
    private final ObjectMapper objectMapper;

    public NombaTransactionsClient(RestClient nombaRestClient, TokenManager tokenManager,
                                   NombaProperties props, ObjectMapper objectMapper) {
        this.nombaRestClient = nombaRestClient;
        this.tokenManager = tokenManager;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    /** GET /transactions/accounts/{subAccountId}. Defensive: returns empty on any problem. */
    public List<JsonNode> listSubAccountTransactions(LocalDate from, LocalDate to) {
        String dateFrom = from.atStartOfDay().atOffset(ZoneOffset.UTC).toString();
        String dateTo = to.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC).toString();
        try {
            String raw = nombaRestClient.get()
                    .uri(b -> b.path("/transactions/accounts/{subAccountId}")
                            .queryParam("dateFrom", dateFrom)
                            .queryParam("dateTo", dateTo)
                            .queryParam("limit", 50)
                            .build(props.subAccountId()))
                    .header("Authorization", "Bearer " + tokenManager.getAccessToken())
                    .header("accountId", props.accountId())
                    .retrieve()
                    .body(String.class);

            JsonNode results = objectMapper.readTree(raw).path("data").path("results");
            List<JsonNode> out = new ArrayList<>();
            if (results.isArray()) results.forEach(out::add);
            log.debug("Requery: fetched {} sub-account transaction(s)", out.size());
            return out;
        } catch (Exception e) {
            log.warn("Requery: could not fetch transactions ({})", e.getMessage());
            return List.of();
        }
    }
}