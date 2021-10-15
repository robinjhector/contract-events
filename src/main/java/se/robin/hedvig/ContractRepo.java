package se.robin.hedvig;

import se.robin.hedvig.model.*;

import java.time.LocalDate;
import java.util.*;

public class ContractRepo {

    private final List<ContractEvent> events = new ArrayList<>();
    public ContractRepo(Collection<ContractEvent> events) {
        this.events.addAll(events);
    }

    public Collection<Contract> getContracts(LocalDate at) {
        var contractMaterializer = new ContractMaterializer();
        events.stream()
            .filter(e -> e.date().isBefore(at))
            .forEach(contractMaterializer::applyEvent);

        return contractMaterializer.contracts.values();
    }

    private static class ContractMaterializer {
        private final Map<Long, Contract> contracts = new HashMap<>();

        private void applyEvent(ContractEvent event) {
            try {
                switch (event) {
                    case ContractCreatedEvent cce -> applyContractCreatedEvent(cce);
                    case PriceIncreasedEvent pie -> applyPriceIncreasedEvent(pie);
                    case PriceDecreasedEvent pde -> applyPriceDecreasedEvent(pde);
                    case ContractTerminatedEvent cte -> applyContractTerminatedEvent(cte);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to handle event: " + event, e);
            }
        }

        private void applyContractCreatedEvent(ContractCreatedEvent event) {
            contracts.put(event.contractId(), Contract.create(event));
        }

        private void applyPriceIncreasedEvent(PriceIncreasedEvent event) {
            var contract = contracts.get(event.contractId());
            var updatedContract = contract.withPriceIncrease(event.premiumIncrease());
            contracts.put(event.contractId(), updatedContract);
        }

        private void applyPriceDecreasedEvent(PriceDecreasedEvent event) {
            var contract = contracts.get(event.contractId());
            var updatedContract = contract.withPriceReduction(event.premiumReduction());
            contracts.put(event.contractId(), updatedContract);
        }

        private void applyContractTerminatedEvent(ContractTerminatedEvent event) {
            var contract = contracts.get(event.contractId());
            var updatedContract = contract.withTermination(event.terminationDate());
            contracts.put(event.contractId(), updatedContract);
        }
    }
}
