package dev.astamur.taxirides.model;

import static dev.astamur.taxirides.utils.TestUtils.createRideRecord;
import static dev.astamur.taxirides.utils.TestUtils.timestamp;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RideAvroParserTest {
    private static final long START_TIMESTAMP = timestamp("2024-01-26T11:39:10");
    private static final long END_TIMESTAMP = timestamp("2024-01-27T11:39:10");

    private static List<Arguments> ridesWithDifferentIntervals() {
        return List.of(
            Arguments.of(Granularity.SECONDS, START_TIMESTAMP, END_TIMESTAMP),
            Arguments.of(Granularity.MINUTES, timestamp("2024-01-26T11:39:00"), timestamp("2024-01-27T11:39:00")),
            Arguments.of(Granularity.MINUTES_5, timestamp("2024-01-26T11:35:00"), timestamp("2024-01-27T11:35:00")),
            Arguments.of(Granularity.MINUTES_15, timestamp("2024-01-26T11:30:00"), timestamp("2024-01-27T11:30:00")),
            Arguments.of(Granularity.HOURS, timestamp("2024-01-26T11:00:00"), timestamp("2024-01-27T11:00:00")),
            Arguments.of(Granularity.DAYS, timestamp("2024-01-26T00:00:00"), timestamp("2024-01-27T00:00:00"))
        );
    }

    @ParameterizedTest
    @MethodSource("ridesWithDifferentIntervals")
    public void shouldParseGranularIntervals(Granularity granularity, long expectedStart, long expectedEnd) {
        var record = createRideRecord(1, 1, START_TIMESTAMP, END_TIMESTAMP);
        assertIntervalParsing(record, granularity, expectedStart, expectedEnd);
    }

    private void assertIntervalParsing(GenericRecord record, Granularity granularity, long expectedStart, long expectedEnd) {
        var ride = new Ride.AvroParser(granularity).parse(record);
        assertEquals(ride.interval().start(), expectedStart);
        assertEquals(ride.interval().end(), expectedEnd);
    }

}