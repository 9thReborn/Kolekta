package com.silasadinoyi.kolekta.reconciliation;

import com.silasadinoyi.kolekta.domain.reconciliation.WebhookEvent;
import com.silasadinoyi.kolekta.domain.reconciliation.WebhookEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/** Drains the inbox every few seconds: processes verified, unprocessed events. */
@Component
public class InboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(InboxProcessor.class);

    private final WebhookEventRepository events;
    private final ReconciliationService reconciliation;

    public InboxProcessor(WebhookEventRepository events, ReconciliationService reconciliation) {
        this.events = events;
        this.reconciliation = reconciliation;
    }

    @Scheduled(fixedDelay = 5000)
    public void drain() {
        List<WebhookEvent> pending =
                events.findTop100BySignatureValidTrueAndStatusOrderByReceivedAtAsc("RECEIVED");
        for (WebhookEvent e : pending) {
            try {
                reconciliation.process(e.getId());          // own transaction per event
            } catch (Exception ex) {
                log.error("Failed to process event {}: {}", e.getId(), ex.getMessage(), ex);
            }
        }
    }
}