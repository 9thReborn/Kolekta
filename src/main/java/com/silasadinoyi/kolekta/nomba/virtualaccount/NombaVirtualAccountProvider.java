package com.silasadinoyi.kolekta.nomba.virtualaccount;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.silasadinoyi.kolekta.config.NombaProperties;
import com.silasadinoyi.kolekta.nomba.auth.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "nomba.mock", havingValue = "false")
public class NombaVirtualAccountProvider implements VirtualAccountProvider {

    private static final Logger log = LoggerFactory.getLogger(NombaVirtualAccountProvider.class);

    private final RestClient nombaRestClient;
    private final TokenManager tokenManager;
    private final NombaProperties props;
    private final ObjectMapper objectMapper;

    public NombaVirtualAccountProvider(RestClient nombaRestClient, TokenManager tokenManager,
                                       NombaProperties props, ObjectMapper objectMapper) {
        this.nombaRestClient = nombaRestClient;
        this.tokenManager = tokenManager;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProvisionedAccount create(CreateCommand cmd) {
        log.debug("Creating VA: ref={}, name='{}', subAccount={}",
                cmd.accountRef(), cmd.accountName(), props.subAccountId());

        String raw = nombaRestClient.post()
                .uri("/accounts/virtual/{subAccountId}", props.subAccountId())
                .header("Authorization", "Bearer " + tokenManager.getAccessToken())
                .header("accountId", props.accountId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new NombaRequest(cmd.accountRef(), cmd.accountName(), cmd.currency(), cmd.bvn(), cmd.expiryDate()))
                .retrieve()
                .body(String.class);

        log.debug("Raw Nomba VA response: {}", raw);   // TEMP: full body for debugging

        NombaResponse resp;
        try {
            resp = objectMapper.readValue(raw, NombaResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse Nomba response: " + raw, e);
        }
        if (resp.data() == null || resp.data().bankAccountNumber() == null) {
            throw new IllegalStateException("Nomba did not return a virtual account: " + raw);
        }
        var d = resp.data();
        return new ProvisionedAccount(d.bankAccountNumber(), d.bankName(), d.bankAccountName());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record NombaRequest(String accountRef, String accountName, String currency, String bvn, String expiryDate) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NombaResponse(String code, String description, Data data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record Data(String bankAccountNumber, String bankAccountName, String bankName, String accountRef) {}
    }
}