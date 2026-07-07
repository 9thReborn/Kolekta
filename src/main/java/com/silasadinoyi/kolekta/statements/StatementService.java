package com.silasadinoyi.kolekta.statements;

import com.silasadinoyi.kolekta.domain.customer.Customer;
import com.silasadinoyi.kolekta.domain.customer.CustomerRepository;
import com.silasadinoyi.kolekta.domain.reconciliation.LedgerDirection;
import com.silasadinoyi.kolekta.domain.reconciliation.LedgerEntry;
import com.silasadinoyi.kolekta.domain.reconciliation.LedgerEntryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StatementService {

    private final CustomerRepository customers;
    private final LedgerEntryRepository ledger;

    public StatementService(CustomerRepository customers, LedgerEntryRepository ledger) {
        this.customers = customers;
        this.ledger = ledger;
    }

    @Transactional(readOnly = true)
    public StatementResponse buildStatement(UUID customerId) {
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown customer: " + customerId));

        List<LedgerEntry> entries = ledger.findByCustomerIdOrderByCreatedAtAsc(customerId);

        long running = 0, totalCredits = 0, totalDebits = 0;
        List<StatementResponse.Line> lines = new ArrayList<>();

        for (LedgerEntry e : entries) {
            if (e.getDirection() == LedgerDirection.CREDIT) {
                running += e.getAmountKobo();
                totalCredits += e.getAmountKobo();
            } else {
                running -= e.getAmountKobo();
                totalDebits += e.getAmountKobo();
            }
            lines.add(new StatementResponse.Line(
                    e.getCreatedAt(),
                    e.getDirection().name(),
                    e.getAmountKobo(),
                    formatKobo(e.getAmountKobo()),
                    running,
                    formatKobo(running)));
        }

        return new StatementResponse(
                customer.getId(),
                customer.getName(),
                "NGN",
                totalCredits, formatKobo(totalCredits),
                totalDebits, formatKobo(totalDebits),
                running, formatKobo(running),
                lines);
    }

    @Transactional(readOnly = true)
    public StatementResponse buildStatementForMerchant(UUID customerId, UUID merchantId) {
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        if (!customer.getMerchantId().equals(merchantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your customer");
        }
        return buildStatement(customerId);   // reuse existing logic
    }

    /** Format kobo as naira using integer math — never floats for money. */
    static String formatKobo(long kobo) {
        long naira = kobo / 100;
        long k = Math.abs(kobo % 100);
        return String.format("\u20a6%,d.%02d", naira, k);   // \u20a6 = ₦
    }
}