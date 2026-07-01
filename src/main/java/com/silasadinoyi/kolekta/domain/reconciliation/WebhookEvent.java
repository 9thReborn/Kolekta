package com.silasadinoyi.kolekta.domain.reconciliation;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_events")
public class WebhookEvent {

    @Id @UuidGenerator
    private UUID id;

    /** Nomba's requestId — our idempotency / dedupe key. */
    @Column(name = "request_id", nullable = false, unique = true, updatable = false)
    private String requestId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "raw_payload", columnDefinition = "text")
    private String rawPayload;

    @Column(name = "signature_valid", nullable = false)
    private boolean signatureValid;

    @Column(nullable = false)
    private String status = "RECEIVED";

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected WebhookEvent() { }

    public WebhookEvent(String requestId, String eventType, String rawPayload, boolean signatureValid) {
        this.requestId = requestId;
        this.eventType = eventType;
        this.rawPayload = rawPayload;
        this.signatureValid = signatureValid;
    }

    public UUID getId() { return id; }
    public String getRequestId() { return requestId; }
    public String getEventType() { return eventType; }
    public String getRawPayload() { return rawPayload; }
    public boolean isSignatureValid() { return signatureValid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getReceivedAt() { return receivedAt; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}