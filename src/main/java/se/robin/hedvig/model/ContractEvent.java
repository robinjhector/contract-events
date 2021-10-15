package se.robin.hedvig.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDate;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "name"
)
@JsonSubTypes({
    @JsonSubTypes.Type(name = "ContractCreatedEvent", value = ContractCreatedEvent.class),
    @JsonSubTypes.Type(name = "PriceIncreasedEvent", value = PriceIncreasedEvent.class),
    @JsonSubTypes.Type(name = "PriceDecreasedEvent", value = PriceDecreasedEvent.class),
    @JsonSubTypes.Type(name = "ContractTerminatedEvent", value = ContractTerminatedEvent.class)
})
public sealed interface ContractEvent permits ContractCreatedEvent, ContractTerminatedEvent, PriceDecreasedEvent, PriceIncreasedEvent {
    long contractId();

    LocalDate date();
}
