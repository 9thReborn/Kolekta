package com.silasadinoyi.kolekta.nomba.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.silasadinoyi.kolekta.config.NombaProperties;
import com.silasadinoyi.kolekta.domain.reconciliation.WebhookEvent;
import com.silasadinoyi.kolekta.domain.reconciliation.WebhookEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookSignatureVerifier verifier;
    private final WebhookEventRepository events;
    private final NombaProperties props;
    private final ObjectMapper objectMapper;

    public WebhookController(WebhookSignatureVerifier verifier, WebhookEventRepository events,
                             NombaProperties props, ObjectMapper objectMapper) {
        this.verifier = verifier;
        this.events = events;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/webhooks/nomba")
    public ResponseEntity<Void> receive(@RequestBody(required = false) byte[] rawBody,
                                        @RequestHeader(name = "nomba-signature", required = false) String signature) {
        byte[] body = (rawBody == null) ? new byte[0] : rawBody;

        // 1. Verify the signature over the RAW bytes — before trusting anything.
        boolean valid = verifier.isValid(body, signature, props.webhookSecret());

        // 2. Pull out event + requestId.
        String payload = new String(body, StandardCharsets.UTF_8);
        String requestId = null, eventType = null;
        try {
            JsonNode node = objectMapper.readTree(payload);
            requestId = node.path("requestId").asText(null);
            eventType = node.path("event").asText(null);
        } catch (Exception e) {
            log.debug("Webhook body was not valid JSON (len={})", body.length);
        }

        // 3. No requestId => likely a URL-validation ping. Acknowledge and move on.
        if (requestId == null || requestId.isBlank()) {
            return ResponseEntity.ok().build();
        }

        // 4. Idempotent insert into the inbox (dedupe on requestId). ALWAYS ack 200 fast.
        if (!events.existsByRequestId(requestId)) {
            try {
                events.save(new WebhookEvent(requestId, eventType, payload, valid));
                log.debug("Stored webhook {} (event={}, signatureValid={})", requestId, eventType, valid);
            } catch (DataIntegrityViolationException dup) {
                log.debug("Duplicate webhook {} ignored", requestId);
            }
        } else {
            log.debug("Duplicate webhook {} ignored", requestId);
        }
        return ResponseEntity.ok().build();
    }
}