package com.silasadinoyi.kolekta.domain.reconciliation;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id @UuidGenerator
    private UUID id;

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "customer_id", updatable = false)
    private UUID customerId;            // null = the platform side of the entry

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private LedgerDirection direction;

    @Column(name = "amount_kobo", nullable = false, updatable = false)
    private long amountKobo;

    @Column(nullable = false, updatable = false)
    private String currency = "NGN";

    @Column(name = "txn_group_id", nullable = false, updatable = false)
    private UUID txnGroupId;            // the two halves of one transaction share this

    @Column(name = "source_event_id", updatable = false)
    private UUID sourceEventId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LedgerEntry() { }

    public LedgerEntry(UUID merchantId, UUID customerId, LedgerDirection direction,
                       long amountKobo, UUID txnGroupId, UUID sourceEventId) {
        this.merchantId = merchantId;
        this.customerId = customerId;
        this.direction = direction;
        this.amountKobo = amountKobo;
        this.txnGroupId = txnGroupId;
        this.sourceEventId = sourceEventId;
    }

    public UUID getId() { return id; }
    public UUID getMerchantId() { return merchantId; }
    public UUID getCustomerId() { return customerId; }
    public LedgerDirection getDirection() { return direction; }
    public long getAmountKobo() { return amountKobo; }
    public String getCurrency() { return currency; }
    public UUID getTxnGroupId() { return txnGroupId; }
    public UUID getSourceEventId() { return sourceEventId; }
    public Instant getCreatedAt() { return createdAt; }
}