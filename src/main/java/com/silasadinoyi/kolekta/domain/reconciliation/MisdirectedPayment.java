package com.silasadinoyi.kolekta.domain.reconciliation;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "misdirected_payments")
public class MisdirectedPayment {

    @Id @UuidGenerator
    private UUID id;

    @Column(name = "merchant_id")
    private UUID merchantId;

    @Column(name = "raw_event_id")
    private UUID rawEventId;

    @Column(nullable = false)
    private String reason;     // unmatched / closed / overpaid / underpaid / unparseable

    @Column(name = "amount_kobo")
    private Long amountKobo;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(name = "resolved_by")
    private String resolvedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MisdirectedPayment() { }

    public MisdirectedPayment(UUID merchantId, UUID rawEventId, String reason, Long amountKobo) {
        this.merchantId = merchantId;
        this.rawEventId = rawEventId;
        this.reason = reason;
        this.amountKobo = amountKobo;
    }

    public UUID getId() { return id; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public UUID getMerchantId() { return merchantId; }
    public UUID getRawEventId() { return rawEventId; }
    public Long getAmountKobo() { return amountKobo; }
    public java.time.Instant getCreatedAt() { return createdAt; }
}