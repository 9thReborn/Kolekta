package com.silasadinoyi.kolekta.admin;

import com.silasadinoyi.kolekta.provisioning.ProvisioningService;
import com.silasadinoyi.kolekta.query.QueryService;
import com.silasadinoyi.kolekta.statements.StatementResponse;
import com.silasadinoyi.kolekta.statements.StatementService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final QueryService query;
    private final StatementService statements;
    private final ProvisioningService provisioning;

    public AdminController(QueryService query, StatementService statements, ProvisioningService provisioning) {
        this.query = query; this.statements = statements; this.provisioning = provisioning;
    }

    @GetMapping("/overview")
    public QueryService.OverviewResponse overview() { return query.getOverview(); }

    @GetMapping("/merchants")
    public List<QueryService.MerchantSummary> merchants() { return query.listMerchants(); }

    @GetMapping("/merchants/{merchantId}/customers")
    public List<QueryService.CustomerSummary> customers(@PathVariable UUID merchantId) {
        return query.listCustomers(merchantId);
    }

    @GetMapping("/customers/{customerId}/statement")
    public StatementResponse statement(@PathVariable UUID customerId) {
        return statements.buildStatement(customerId);
    }

    @GetMapping("/misdirected-payments")
    public List<QueryService.MisdirectedSummary> misdirected() { return query.listMisdirected(); }

    @DeleteMapping("/merchants/{merchantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMerchant(@PathVariable UUID merchantId) { provisioning.deleteMerchant(merchantId); }

    @DeleteMapping("/virtual-accounts/{accountRef}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void expireVirtualAccount(@PathVariable String accountRef) {
        provisioning.expireByRef(accountRef);
    }
}