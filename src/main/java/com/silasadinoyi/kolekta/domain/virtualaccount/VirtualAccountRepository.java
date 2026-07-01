package com.silasadinoyi.kolekta.domain.virtualaccount;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, UUID> {
    Optional<VirtualAccount> findByAccountRef(String accountRef);            // webhook matching uses this
    Optional<VirtualAccount> findByNombaAccountNumber(String accountNumber);
    Optional<VirtualAccount> findByCustomerId(UUID customerId);
}