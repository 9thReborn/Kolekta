package com.silasadinoyi.kolekta.reconciliation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.silasadinoyi.kolekta.domain.reconciliation.*;
import com.silasadinoyi.kolekta.domain.virtualaccount.VirtualAccount;
import com.silasadinoyi.kolekta.domain.virtualaccount.VirtualAccountRepository;
import com.silasadinoyi.kolekta.domain.virtualaccount.VirtualAccountStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;

@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);

    private final WebhookEventRepository events;
    private final VirtualAccountRepository virtualAccounts;
    private final LedgerEntryRepository ledger;
    private final MisdirectedPaymentRepository misdirected;
    private final ObjectMapper objectMapper;

    public ReconciliationService(WebhookEventRepository events, VirtualAccountRepository virtualAccounts,
                                 LedgerEntryRepository ledger, MisdirectedPaymentRepository misdirected,
                                 ObjectMapper objectMapper) {
        this.events = events;
        this.virtualAccounts = virtualAccounts;
        this.ledger = ledger;
        this.misdirected = misdirected;
        this.objectMapper = objectMapper;
    }

    /** Processes one inbox event exactly once, in its own transaction. */
    @Transactional
    public void process(UUID eventId) {
        WebhookEvent event = events.findById(eventId).orElseThrow();
        if (!"RECEIVED".equals(event.getStatus())) return;

        MDC.put("ref", event.getRequestId());
        try {
            if (!"virtual_account.funded".equals(event.getEventType())) { markProcessed(event); return; }

            JsonNode data;
            try {
                data = objectMapper.readTree(event.getRawPayload()).path("data");
            } catch (Exception e) {
                quarantine(null, event, "unparseable", null); return;
            }

            String accountRef = data.path("accountRef").asText(null);
            Long amountKobo = extractAmountKobo(data);

            Optional<VirtualAccount> vaOpt =
                    (accountRef == null) ? Optional.empty() : virtualAccounts.findByAccountRef(accountRef);

            if (vaOpt.isEmpty())                               { quarantine(null, event, "unmatched", amountKobo); return; }
            VirtualAccount va = vaOpt.get();
            if (va.getStatus() != VirtualAccountStatus.ACTIVE) { quarantine(va.getMerchantId(), event, "closed", amountKobo); return; }
            if (amountKobo == null)                            { quarantine(va.getMerchantId(), event, "unparseable", null); return; }
            if (va.getExpectedAmountKobo() != null && !va.getExpectedAmountKobo().equals(amountKobo)) {
                String reason = amountKobo > va.getExpectedAmountKobo() ? "overpaid" : "underpaid";
                quarantine(va.getMerchantId(), event, reason, amountKobo); return;
            }

            UUID group = UUID.randomUUID();
            ledger.save(new LedgerEntry(va.getMerchantId(), va.getCustomerId(),
                    LedgerDirection.CREDIT, amountKobo, group, event.getId()));
            ledger.save(new LedgerEntry(va.getMerchantId(), null,
                    LedgerDirection.DEBIT, amountKobo, group, event.getId()));
            markProcessed(event);
            log.info("Reconciled {} kobo to customer {}", amountKobo, va.getCustomerId());
        } finally {
            MDC.remove("ref");
        }
    }

    private void quarantine(UUID merchantId, WebhookEvent event, String reason, Long amountKobo) {
        misdirected.save(new MisdirectedPayment(merchantId, event.getId(), reason, amountKobo));
        markProcessed(event);
        log.warn("Misdirected payment ({}) from event {}", reason, event.getRequestId());
    }

    private void markProcessed(WebhookEvent event) {
        event.setStatus("PROCESSED");
        event.setProcessedAt(Instant.now());
        events.save(event);
    }

    private Long extractAmountKobo(JsonNode data) {
        for (String key : new String[]{"amount", "amountReceived", "amountInKobo"}) {
            JsonNode n = data.get(key);
            if (n != null && n.isNumber()) return n.asLong();
        }
        return null;
    }
}