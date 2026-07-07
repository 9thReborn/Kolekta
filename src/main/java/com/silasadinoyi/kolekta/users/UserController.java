package com.silasadinoyi.kolekta.users;

import com.silasadinoyi.kolekta.domain.virtualaccount.VirtualAccount;
import com.silasadinoyi.kolekta.provisioning.ProvisioningService;
import com.silasadinoyi.kolekta.query.QueryService;
import com.silasadinoyi.kolekta.security.AuthPrincipal;
import com.silasadinoyi.kolekta.statements.StatementResponse;
import com.silasadinoyi.kolekta.statements.StatementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final QueryService query;
    private final StatementService statements;
    private final ProvisioningService provisioning;

    public UserController(QueryService query, StatementService statements, ProvisioningService provisioning) {
        this.query = query; this.statements = statements; this.provisioning = provisioning;
    }

    @GetMapping("/me")
    public QueryService.MerchantOverviewResponse me(@AuthenticationPrincipal AuthPrincipal p) {
        return query.getMerchantOverview(p.merchantId());
    }

    @GetMapping("/customers")
    public List<QueryService.CustomerSummary> customers(@AuthenticationPrincipal AuthPrincipal p) {
        return query.listCustomers(p.merchantId());
    }

    @PostMapping("/customers")
    public CustomerAccountResponse onboard(@AuthenticationPrincipal AuthPrincipal p,
                                           @Valid @RequestBody CreateCustomerRequest req) {
        VirtualAccount va = provisioning.onboardCustomer(p.merchantId(), req.name(), req.email(), req.phone());
        return new CustomerAccountResponse(va.getCustomerId(), va.getId(), va.getAccountRef(),
                va.getNombaAccountNumber(), va.getBankName(), va.getAccountName());
    }

    @GetMapping("/customers/{customerId}/statement")
    public StatementResponse statement(@AuthenticationPrincipal AuthPrincipal p, @PathVariable UUID customerId) {
        return statements.buildStatementForMerchant(customerId, p.merchantId());
    }

    @GetMapping("/misdirected-payments")
    public List<QueryService.MisdirectedSummary> misdirected(@AuthenticationPrincipal AuthPrincipal p) {
        return query.listMisdirectedForMerchant(p.merchantId());
    }
    @DeleteMapping("/customers/{customerId}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void closeCustomer(@AuthenticationPrincipal AuthPrincipal p, @PathVariable UUID customerId) {
        provisioning.closeCustomer(p.merchantId(), customerId);
    }

    public record CreateCustomerRequest(@NotBlank String name, @Email String email, String phone) {}
    public record CustomerAccountResponse(UUID customerId, UUID virtualAccountId, String accountRef,
                                          String accountNumber, String bankName, String accountName) {}
}