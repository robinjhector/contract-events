package se.robin.hedvig.model;

import java.time.LocalDate;
import java.time.YearMonth;

public record Contract(
    long contractId,
    long premium,
    LocalDate startedAt,
    LocalDate terminatedAt
) {
    public static Contract create(ContractCreatedEvent cce) {
        return new Contract(cce.contractId(), cce.premium(), cce.date(), null);
    }

    public Contract withPriceIncrease(long premiumIncrease) {
        return new Contract(
            contractId,
            premium + premiumIncrease,
            startedAt,
            terminatedAt
        );
    }

    public Contract withPriceReduction(long premiumReduction) {
        return new Contract(
            contractId,
            premium - premiumReduction,
            startedAt,
            terminatedAt
        );
    }

    public Contract withTermination(LocalDate terminatedAt) {
        return new Contract(
            contractId,
            premium,
            startedAt,
            terminatedAt
        );
    }

    public boolean notTerminated() {
        return terminatedAt == null;
    }
}
