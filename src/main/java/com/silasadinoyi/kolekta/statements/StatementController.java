package com.silasadinoyi.kolekta.statements;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class StatementController {

    private final StatementService statements;

    public StatementController(StatementService statements) {
        this.statements = statements;
    }

    @GetMapping("/customers/{customerId}/statement")
    public StatementResponse statement(@PathVariable UUID customerId) {
        return statements.buildStatement(customerId);
    }
}