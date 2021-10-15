package se.robin.hedvig.model;

import java.time.LocalDate;

public record ContractTerminatedEvent(
    long contractId,
    LocalDate terminationDate
) implements ContractEvent {
    @Override
    public LocalDate date() {
        return terminationDate;
    }
}
