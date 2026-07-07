package com.silasadinoyi.kolekta.provisioning;

import com.silasadinoyi.kolekta.domain.customer.Customer;
import com.silasadinoyi.kolekta.domain.customer.CustomerRepository;
import com.silasadinoyi.kolekta.domain.merchant.Merchant;
import com.silasadinoyi.kolekta.domain.merchant.MerchantRepository;
import com.silasadinoyi.kolekta.domain.virtualaccount.VirtualAccount;
import com.silasadinoyi.kolekta.domain.virtualaccount.VirtualAccountRepository;
import com.silasadinoyi.kolekta.domain.virtualaccount.VirtualAccountStatus;
import com.silasadinoyi.kolekta.domain.virtualaccount.VirtualAccountType;
import com.silasadinoyi.kolekta.nomba.virtualaccount.VirtualAccountProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ProvisioningService {

    private final MerchantRepository merchants;
    private final CustomerRepository customers;
    private final VirtualAccountRepository virtualAccounts;
    private final VirtualAccountProvider provider;

    public ProvisioningService(MerchantRepository merchants, CustomerRepository customers,
                               VirtualAccountRepository virtualAccounts, VirtualAccountProvider provider) {
        this.merchants = merchants;
        this.customers = customers;
        this.virtualAccounts = virtualAccounts;
        this.provider = provider;
    }

    @Transactional
    public Merchant createMerchant(String name) {
        return merchants.save(new Merchant(newRef("mrc_"), name));
    }

    /**
     * Onboards an end-customer and issues their dedicated virtual account.
     * NOTE: the provider call sits inside this transaction for MVP simplicity. In
     * production we'd persist a PENDING account first, then reconcile (same idea as
     * the payout outbox), so we never hold a DB transaction across a network call.
     */
    @Transactional
    public VirtualAccount onboardCustomer(UUID merchantId, String name, String email, String phone) {
        Merchant merchant = merchants.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown merchant: " + merchantId));

        Customer customer = customers.save(new Customer(merchant.getId(), name, email, phone));

        String accountRef = newRef("kva");
        String accountName = sanitizeAccountName(merchant.getName() + " " + name);
        VirtualAccount va = new VirtualAccount(
                merchant.getId(), customer.getId(), accountRef, accountName, VirtualAccountType.STATIC);

        VirtualAccountProvider.ProvisionedAccount issued = provider.create(
                new VirtualAccountProvider.CreateCommand(accountRef, accountName, "NGN", null, null));
        va.attachNombaDetails(issued.accountNumber(), issued.bankName());

        return virtualAccounts.save(va);
    }

    @Transactional
    public void deleteMerchant(UUID merchantId) {
        Merchant merchant = merchants.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown merchant: " + merchantId));
        if (!customers.findByMerchantId(merchantId).isEmpty()) {
            throw new IllegalStateException("Merchant has customers; cannot delete");
        }
        merchants.delete(merchant);
    }

    @Transactional
    public void closeCustomer(UUID merchantId, UUID customerId) {
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown customer: " + customerId));
        if (!customer.getMerchantId().equals(merchantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your customer");
        }
        virtualAccounts.findByCustomerId(customerId).ifPresent(va -> {
            if (va.getStatus() == VirtualAccountStatus.ACTIVE) {
                provider.expire(va.getAccountRef());
                va.setStatus(VirtualAccountStatus.CLOSED);
                virtualAccounts.save(va);
            }
        });
        customer.setStatus("CLOSED");
        customers.save(customer);
    }

    /** Ops utility: expire a virtual account at Nomba by accountRef (frees a sandbox slot). */
    public void expireByRef(String accountRef) {
        provider.expire(accountRef);
        virtualAccounts.findByAccountRef(accountRef).ifPresent(va -> {
            va.setStatus(VirtualAccountStatus.CLOSED);
            virtualAccounts.save(va);
        });
    }

    private String newRef(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "");  // e.g. kva_ + 32 hex = 36 chars
    }

    /** Nomba allows only letters, digits and spaces in account names. */
    private String sanitizeAccountName(String raw) {
        String cleaned = raw.replaceAll("[^A-Za-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return cleaned.length() > 64 ? cleaned.substring(0, 64).trim() : cleaned;
    }
}