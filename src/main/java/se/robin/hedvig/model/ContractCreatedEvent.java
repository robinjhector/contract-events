package se.robin.hedvig.model;

import java.time.LocalDate;

public record ContractCreatedEvent(
    long contractId,
    long premium,
    LocalDate startDate
) implements ContractEvent {
    @Override
    public LocalDate date() {
        return startDate;
    }
}
