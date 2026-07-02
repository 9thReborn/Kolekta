package com.silasadinoyi.kolekta.query;

import com.silasadinoyi.kolekta.common.Money;
import com.silasadinoyi.kolekta.domain.customer.CustomerRepository;
import com.silasadinoyi.kolekta.domain.merchant.MerchantRepository;
import com.silasadinoyi.kolekta.domain.reconciliation.LedgerDirection;
import com.silasadinoyi.kolekta.domain.reconciliation.LedgerEntryRepository;
import com.silasadinoyi.kolekta.domain.reconciliation.MisdirectedPaymentRepository;
import com.silasadinoyi.kolekta.domain.virtualaccount.VirtualAccount;
import com.silasadinoyi.kolekta.domain.virtualaccount.VirtualAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class QueryService {

    private final MerchantRepository merchants;
    private final CustomerRepository customers;
    private final VirtualAccountRepository virtualAccounts;
    private final LedgerEntryRepository ledger;
    private final MisdirectedPaymentRepository misdirected;

    public QueryService(MerchantRepository merchants, CustomerRepository customers,
                        VirtualAccountRepository virtualAccounts, LedgerEntryRepository ledger,
                        MisdirectedPaymentRepository misdirected) {
        this.merchants = merchants;
        this.customers = customers;
        this.virtualAccounts = virtualAccounts;
        this.ledger = ledger;
        this.misdirected = misdirected;
    }

    @Transactional(readOnly = true)
    public List<MerchantSummary> listMerchants() {
        return merchants.findAll().stream()
                .map(m -> new MerchantSummary(m.getId(), m.getAccountRef(), m.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerSummary> listCustomers(UUID merchantId) {
        return customers.findByMerchantId(merchantId).stream().map(c -> {
            VirtualAccount va = virtualAccounts.findByCustomerId(c.getId()).orElse(null);
            long balance = ledger.sumAmount(c.getId(), LedgerDirection.CREDIT)
                    - ledger.sumAmount(c.getId(), LedgerDirection.DEBIT);
            return new CustomerSummary(
                    c.getId(), c.getName(), c.getEmail(),
                    va != null ? va.getNombaAccountNumber() : null,
                    va != null ? va.getBankName() : null,
                    balance, Money.formatKobo(balance));
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<MisdirectedSummary> listMisdirected() {
        return misdirected.findTop200ByOrderByCreatedAtDesc().stream()
                .map(m -> new MisdirectedSummary(
                        m.getId(), m.getMerchantId(), m.getReason(),
                        m.getAmountKobo(),
                        m.getAmountKobo() == null ? null : Money.formatKobo(m.getAmountKobo()),
                        m.getStatus(), m.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public OverviewResponse getOverview() {
        long collected = ledger.sumByDirection(LedgerDirection.CREDIT);
        return new OverviewResponse(
                merchants.count(),
                customers.count(),
                collected,
                Money.formatKobo(collected),
                misdirected.countByStatus("OPEN"));
    }

    // --- response DTOs ---
    public record MerchantSummary(UUID merchantId, String accountRef, String name) {}
    public record CustomerSummary(UUID customerId, String name, String email,
                                  String accountNumber, String bankName,
                                  long balanceKobo, String balanceText) {}
    public record MisdirectedSummary(UUID id, UUID merchantId, String reason,
                                     Long amountKobo, String amountText, String status,
                                     java.time.Instant createdAt) {}

    public record OverviewResponse(long merchantCount, long customerCount,
                                   long totalCollectedKobo, String totalCollectedText,
                                   long misdirectedOpenCount) {}
}