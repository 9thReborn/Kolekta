package com.silasadinoyi.kolekta.reconciliation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.silasadinoyi.kolekta.domain.reconciliation.WebhookEvent;
import com.silasadinoyi.kolekta.domain.reconciliation.WebhookEventRepository;
import com.silasadinoyi.kolekta.domain.virtualaccount.VirtualAccount;
import com.silasadinoyi.kolekta.domain.virtualaccount.VirtualAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Pull-based safety net: periodically fetches Nomba's transaction list and back-fills any inbound
 * funding the webhook (push) channel missed, by injecting it into the same inbox — so the normal
 * reconciliation path handles it. Push and pull converge on one code path.
 *
 * Runs only against live Nomba (nomba.mock=false). Field mapping is intentionally defensive;
 * confirm exact transaction fields against real sandbox data and tighten reference matching.
 */
@Component
@ConditionalOnProperty(name = "nomba.mock", havingValue = "false")
public class RequeryReconciler {

    private static final Logger log = LoggerFactory.getLogger(RequeryReconciler.class);

    private final NombaTransactionsClient transactions;
    private final VirtualAccountRepository virtualAccounts;
    private final WebhookEventRepository events;
    private final ObjectMapper objectMapper;

    public RequeryReconciler(NombaTransactionsClient transactions, VirtualAccountRepository virtualAccounts,
                             WebhookEventRepository events, ObjectMapper objectMapper) {
        this.transactions = transactions;
        this.virtualAccounts = virtualAccounts;
        this.events = events;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${kolekta.reconciler.interval-ms:300000}")   // every 5 min
    public void reconcile() {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(1);
        List<JsonNode> txns = transactions.listSuccessful(from, to);
        if (txns.isEmpty()) return;

        int backfilled = 0;
        for (JsonNode tx : txns) {
            String ref = firstText(tx, "requestId", "sessionId", "reference", "id", "merchantTxRef");
            if (ref == null || events.existsByRequestId(ref)) continue;   // no ref, or already have it

            String accountRef = firstText(tx, "accountRef");
            String accountNumber = firstText(tx, "accountNumber", "bankAccountNumber");
            Long amount = firstLong(tx, "amount", "amountReceived");

            Optional<VirtualAccount> va = Optional.empty();
            if (accountRef != null) va = virtualAccounts.findByAccountRef(accountRef);
            if (va.isEmpty() && accountNumber != null) va = virtualAccounts.findByNombaAccountNumber(accountNumber);
            if (va.isEmpty()) continue;   // not one of our accounts

            ObjectNode root = objectMapper.createObjectNode();
            root.put("event", "virtual_account.funded");
            root.put("requestId", ref);
            ObjectNode data = root.putObject("data");
            data.put("accountRef", va.get().getAccountRef());
            if (amount != null) data.put("amount", amount);

            try {
                events.save(new WebhookEvent(ref, "virtual_account.funded", root.toString(), true));
                backfilled++;
            } catch (DataIntegrityViolationException dup) {
                // a webhook inserted it concurrently — safe to ignore
            }
        }
        if (backfilled > 0) log.info("Requery reconciler back-filled {} missed transaction(s)", backfilled);
    }

    private String firstText(JsonNode node, String... keys) {
        for (String k : keys) {
            JsonNode v = node.get(k);
            if (v != null && v.isValueNode() && !v.asText().isBlank()) return v.asText();
        }
        return null;
    }

    private Long firstLong(JsonNode node, String... keys) {
        for (String k : keys) {
            JsonNode v = node.get(k);
            if (v != null && v.isNumber()) return v.asLong();
        }
        return null;
    }
}