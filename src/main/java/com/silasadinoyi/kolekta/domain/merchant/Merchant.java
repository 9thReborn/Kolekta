package com.silasadinoyi.kolekta.domain.merchant;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "merchants")
public class Merchant {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "account_ref", nullable = false, unique = true, updatable = false)
    private String accountRef;

    @Column(nullable = false)
    private String name;

    /** Platform fee in basis points (100 bps = 1%). */
    @Column(name = "fee_bps", nullable = false)
    private int feeBps = 0;

    @Column(name = "payout_bank_code")
    private String payoutBankCode;

    @Column(name = "payout_account_number")
    private String payoutAccountNumber;

    /** Nomba's id is a FOREIGN reference only — our account_ref is the primary key. */
    @Column(name = "nomba_sub_account_id")
    private String nombaSubAccountId;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Merchant() { }   // JPA requires a no-arg constructor

    public Merchant(String accountRef, String name) {
        this.accountRef = accountRef;
        this.name = name;
    }

    public UUID getId() { return id; }
    public String getAccountRef() { return accountRef; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getFeeBps() { return feeBps; }
    public void setFeeBps(int feeBps) { this.feeBps = feeBps; }
    public String getPayoutBankCode() { return payoutBankCode; }
    public void setPayoutBankCode(String v) { this.payoutBankCode = v; }
    public String getPayoutAccountNumber() { return payoutAccountNumber; }
    public void setPayoutAccountNumber(String v) { this.payoutAccountNumber = v; }
    public String getNombaSubAccountId() { return nombaSubAccountId; }
    public void setNombaSubAccountId(String v) { this.nombaSubAccountId = v; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}