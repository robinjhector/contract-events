package se.robin.hedvig.model;

import java.time.LocalDate;

public record PriceDecreasedEvent(
    long contractId,
    long premiumReduction,
    LocalDate atDate
) implements ContractEvent {
    @Override
    public LocalDate date() {
        return atDate;
    }
}
