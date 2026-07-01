package com.silasadinoyi.kolekta.domain.virtualaccount;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "virtual_accounts")
public class VirtualAccount {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    /** Our stable id — also the reference we send to Nomba. */
    @Column(name = "account_ref", nullable = false, unique = true, updatable = false)
    private String accountRef;

    /** The NUBAN Nomba issues — filled in after the create call returns. */
    @Column(name = "nomba_account_number")
    private String nombaAccountNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VirtualAccountType type = VirtualAccountType.STATIC;

    @Column(name = "expected_amount_kobo")
    private Long expectedAmountKobo;   // nullable — amounts are in kobo

    @Column(name = "expiry_date")
    private Instant expiryDate;        // null => static/permanent

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VirtualAccountStatus status = VirtualAccountStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected VirtualAccount() { }

    public VirtualAccount(UUID merchantId, UUID customerId, String accountRef,
                          String accountName, VirtualAccountType type) {
        this.merchantId = merchantId;
        this.customerId = customerId;
        this.accountRef = accountRef;
        this.accountName = accountName;
        this.type = type;
    }

    /** Called once Nomba returns the issued account. */
    public void attachNombaDetails(String nombaAccountNumber, String bankName) {
        this.nombaAccountNumber = nombaAccountNumber;
        this.bankName = bankName;
    }

    public UUID getId() { return id; }
    public UUID getMerchantId() { return merchantId; }
    public UUID getCustomerId() { return customerId; }
    public String getAccountRef() { return accountRef; }
    public String getNombaAccountNumber() { return nombaAccountNumber; }
    public String getBankName() { return bankName; }
    public String getAccountName() { return accountName; }
    public VirtualAccountType getType() { return type; }
    public Long getExpectedAmountKobo() { return expectedAmountKobo; }
    public void setExpectedAmountKobo(Long v) { this.expectedAmountKobo = v; }
    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant v) { this.expiryDate = v; }
    public VirtualAccountStatus getStatus() { return status; }
    public void setStatus(VirtualAccountStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}