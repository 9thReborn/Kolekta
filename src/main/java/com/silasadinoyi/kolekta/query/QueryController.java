package com.silasadinoyi.kolekta.query;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class QueryController {

    private final QueryService query;

    public QueryController(QueryService query) {
        this.query = query;
    }

    @GetMapping("/merchants")
    public List<QueryService.MerchantSummary> merchants() {
        return query.listMerchants();
    }

    @GetMapping("/merchants/{merchantId}/customers")
    public List<QueryService.CustomerSummary> customers(@PathVariable UUID merchantId) {
        return query.listCustomers(merchantId);
    }

    @GetMapping("/misdirected-payments")
    public List<QueryService.MisdirectedSummary> misdirected() {
        return query.listMisdirected();
    }
}