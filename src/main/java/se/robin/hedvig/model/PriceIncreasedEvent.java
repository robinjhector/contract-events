package se.robin.hedvig.model;

import java.time.LocalDate;

public record PriceIncreasedEvent(
    long contractId,
    long premiumIncrease,
    LocalDate atDate
) implements ContractEvent {
    @Override
    public LocalDate date() {
        return atDate;
    }
}
