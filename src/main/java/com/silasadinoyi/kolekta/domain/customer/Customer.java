package com.silasadinoyi.kolekta.domain.customer;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @UuidGenerator
    private UUID id;

    /** FK stored as a plain id (no JPA relationship) — explicit and avoids lazy-loading surprises. */
    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(nullable = false)
    private String name;

    @Column
    private String email;

    @Column
    private String phone;

    /** KYC tier (1/2/3) — maps to Nigerian CBN limits; gates provisioning later. */
    @Column(name = "kyc_tier", nullable = false)
    private int kycTier = 1;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Customer() { }

    public Customer(UUID merchantId, String name, String email, String phone) {
        this.merchantId = merchantId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public UUID getId() { return id; }
    public UUID getMerchantId() { return merchantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getKycTier() { return kycTier; }
    public void setKycTier(int kycTier) { this.kycTier = kycTier; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}