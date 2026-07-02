package com.silasadinoyi.kolekta.domain.reconciliation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    List<LedgerEntry> findByCustomerIdOrderByCreatedAtAsc(UUID customerId);   // for statements later

    @Query("select coalesce(sum(l.amountKobo), 0) from LedgerEntry l " +
            "where l.customerId = :customerId and l.direction = :direction")
    long sumAmount(@Param("customerId") UUID customerId, @Param("direction") LedgerDirection direction);

    @Query("select coalesce(sum(l.amountKobo), 0) from LedgerEntry l where l.direction = :direction")
    long sumByDirection(@Param("direction") LedgerDirection direction);
}