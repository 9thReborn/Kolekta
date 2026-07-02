package com.silasadinoyi.kolekta.domain.reconciliation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface MisdirectedPaymentRepository extends JpaRepository<MisdirectedPayment, UUID> {
    List<MisdirectedPayment> findTop200ByOrderByCreatedAtDesc();
    long countByStatus(String status);
}