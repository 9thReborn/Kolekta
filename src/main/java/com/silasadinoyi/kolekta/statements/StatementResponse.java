package com.silasadinoyi.kolekta.statements;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StatementResponse(
        UUID customerId,
        String customerName,
        String currency,
        long totalCreditsKobo,
        String totalCreditsText,
        long totalDebitsKobo,
        String totalDebitsText,
        long balanceKobo,
        String balanceText,
        List<Line> lines
) {
    public record Line(
            Instant date,
            String direction,
            long amountKobo,
            String amountText,
            long runningBalanceKobo,
            String runningBalanceText
    ) {}
}