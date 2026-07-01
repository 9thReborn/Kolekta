package com.silasadinoyi.kolekta.provisioning;

import com.silasadinoyi.kolekta.domain.merchant.Merchant;
import com.silasadinoyi.kolekta.domain.virtualaccount.VirtualAccount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ProvisioningController {

    private final ProvisioningService provisioning;

    public ProvisioningController(ProvisioningService provisioning) {
        this.provisioning = provisioning;
    }

    @PostMapping("/merchants")
    public MerchantResponse createMerchant(@Valid @RequestBody CreateMerchantRequest req) {
        Merchant m = provisioning.createMerchant(req.name());
        return new MerchantResponse(m.getId(), m.getAccountRef(), m.getName());
    }

    @PostMapping("/merchants/{merchantId}/customers")
    public CustomerAccountResponse onboardCustomer(@PathVariable UUID merchantId,
                                                   @Valid @RequestBody CreateCustomerRequest req) {
        VirtualAccount va = provisioning.onboardCustomer(merchantId, req.name(), req.email(), req.phone());
        return new CustomerAccountResponse(
                va.getCustomerId(), va.getId(), va.getAccountRef(),
                va.getNombaAccountNumber(), va.getBankName(), va.getAccountName());
    }

    @DeleteMapping("/merchants/{merchantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMerchant(@PathVariable UUID merchantId) {
        provisioning.deleteMerchant(merchantId);
    }

    public record CreateMerchantRequest(@NotBlank String name) {}
    public record MerchantResponse(UUID merchantId, String accountRef, String name) {}
    public record CreateCustomerRequest(@NotBlank String name, @Email String email, String phone) {}
    public record CustomerAccountResponse(UUID customerId, UUID virtualAccountId, String accountRef,
                                          String accountNumber, String bankName, String accountName) {}
}