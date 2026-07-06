package com.silasadinoyi.kolekta.reconciliation;

import com.fasterxml.jackson.databind.JsonNode;
import com.silasadinoyi.kolekta.domain.reconciliation.WebhookEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Pull-based safety net: cross-checks Nomba's transaction list against our inbox and flags drift —
 * transactions Nomba recorded that we have no record of (a missed webhook).
 *
 * Nomba's transaction record carries no virtual-account reference, so a drifted transaction can't
 * yet be auto-attributed to a customer; it's logged for review. Once a real inbound transaction
 * reveals the linking field, this upgrades to injecting the missed event into the inbox.
 * Active only against live Nomba (nomba.mock=false).
 */
@Component
@ConditionalOnProperty(name = "nomba.mock", havingValue = "false")
public class RequeryReconciler {

    private static final Logger log = LoggerFactory.getLogger(RequeryReconciler.class);

    private final NombaTransactionsClient transactions;
    private final WebhookEventRepository events;

    public RequeryReconciler(NombaTransactionsClient transactions, WebhookEventRepository events) {
        this.transactions = transactions;
        this.events = events;
    }

    @Scheduled(fixedDelayString = "${kolekta.reconciler.interval-ms:300000}")   // every 5 min
    public void reconcile() {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(1);
        List<JsonNode> txns = transactions.listSubAccountTransactions(from, to);
        if (txns.isEmpty()) return;

        int unreconciled = 0;
        for (JsonNode tx : txns) {
            if (!"SUCCESS".equalsIgnoreCase(tx.path("status").asText(""))) continue;
            String ref = firstText(tx, "merchantTxRef", "id");
            if (ref == null || events.existsByRequestId(ref)) continue;
            unreconciled++;
            log.warn("Requery: transaction {} on Nomba is not in our inbox (amount={}, type={})",
                    ref, tx.path("amount").asText("?"), tx.path("type").asText("?"));
        }

        if (unreconciled > 0) log.warn("Requery reconciler: {} unreconciled transaction(s) need review", unreconciled);
        else log.debug("Requery reconciler: all {} transaction(s) reconciled", txns.size());
    }

    private String firstText(JsonNode node, String... keys) {
        for (String k : keys) {
            JsonNode v = node.get(k);
            if (v != null && v.isValueNode() && !v.asText().isBlank()) return v.asText();
        }
        return null;
    }
}