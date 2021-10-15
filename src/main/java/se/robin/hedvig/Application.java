package se.robin.hedvig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import se.robin.hedvig.model.Contract;
import se.robin.hedvig.model.ContractCreatedEvent;
import se.robin.hedvig.model.ContractEvent;
import se.robin.hedvig.model.ContractTerminatedEvent;

import java.io.File;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Application {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
        .findAndAddModules()
        .build();

    /**
     * args[0] = Test case
     * args[1] = Path to file with data
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            errorExit("""
                Usage: hedvig.jar task1/task2 path-to-data-file report-date
                Example: hedvig.jar task1 resources/test-data.txt 2020-08-10
                """);
        }

        var testCase = Task.valueOf(args[0]);
        var file = new File(args[1]);

        if (!file.exists()) {
            errorExit("File %s does not exist".formatted(file.getAbsolutePath()));
        }

        var allEvents = FileUtils.streamLines(file, UTF_8)
            .map(Application::deserializeEvent)
            .filter(testCase.eventFilter)
            .collect(Collectors.toList());

        var eventRepo = new ContractRepo(allEvents);
        var reportRange = new YearMonthRange(
            YearMonth.of(2020, 1),
            YearMonth.of(2021, 1)
        );

        var actualPaidPremiums = 0L;
        var expectedYearlyPremiums = 0L;
        for (YearMonth reportMonth : reportRange) {
            //Since reporting is done for the _entire_ month, the reportDate is +1 month
            //Eg: the report for 2020-02 will have a report based on data before 2020-03-01 00:00:00
            var reportDate = reportMonth.plusMonths(1).atDay(1);
            var contracts = eventRepo.getContracts(reportDate);
            //Active this month = premiums has been paid this month, even though the contract might have been terminated
            var contractActiveThisMonth = (Predicate<Contract>) c -> c.notTerminated() || YearMonth.from(c.terminatedAt()).equals(reportMonth);
            var monthsLeftToReport = reportRange.remainingMonths(reportMonth);

            var nrOfContracts = contracts.stream()
                .filter(contractActiveThisMonth)
                .count();

            // Actual gross written premium. Tallied up for each month we cycle through
            actualPaidPremiums += contracts.stream()
                .filter(contractActiveThisMonth)
                .mapToLong(Contract::premium)
                .sum();

            // Expected gross written premium.
            // Easiest to calculate using the already paid premiums + remaining expected premiums
            // (Taking into consideration if the contract was terminated some time this month)
            expectedYearlyPremiums = actualPaidPremiums + contracts.stream()
                .filter(Contract::notTerminated)
                .mapToLong(c -> c.premium() * monthsLeftToReport)
                .sum();

            System.out.printf(
                "Report for %s: [contracts=%d, AGWP=%d, EGWP=%d]%n",
                reportMonth,
                nrOfContracts,
                actualPaidPremiums,
                expectedYearlyPremiums
            );
        }
    }

    private static ContractEvent deserializeEvent(String line) {
        try {
            return MAPPER.readValue(line, ContractEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void errorExit(String errorMessage) {
        System.err.println(errorMessage);
        System.exit(1);
    }

    private enum Task {
        task1(e -> e instanceof ContractCreatedEvent || e instanceof ContractTerminatedEvent),
        task2(e -> true);

        private final Predicate<ContractEvent> eventFilter;
        Task(final Predicate<ContractEvent> eventFilter) {
            this.eventFilter = eventFilter;
        }
    }

    private static record YearMonthRange(
        YearMonth startMonthIncl,
        YearMonth endMonthExcl
    ) implements Iterable<YearMonth> {

        public long remainingMonths(YearMonth fromDate) {
            return fromDate.until(endMonthExcl, ChronoUnit.MONTHS) - 1; //-1 since end is exclusive
        }

        @Override
        public Iterator<YearMonth> iterator() {
            return new Iterator<>() {
                private YearMonth current = startMonthIncl.minusMonths(1);

                @Override
                public boolean hasNext() {
                    var potentialNext = current.plusMonths(1);
                    return potentialNext.isBefore(endMonthExcl);
                }

                @Override
                public YearMonth next() {
                    return current = current.plusMonths(1);
                }
            };
        }
    }
}
