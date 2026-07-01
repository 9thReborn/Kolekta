package com.silasadinoyi.kolekta.domain.reconciliation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {
    boolean existsByRequestId(String requestId);
    List<WebhookEvent> findTop100BySignatureValidTrueAndStatusOrderByReceivedAtAsc(String status);
}