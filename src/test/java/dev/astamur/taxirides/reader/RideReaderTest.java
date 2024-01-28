package dev.astamur.taxirides.reader;

import static dev.astamur.taxirides.utils.TestUtils.createRide;
import static dev.astamur.taxirides.utils.TestUtils.readAllRides;
import static dev.astamur.taxirides.utils.TestUtils.ridesToParquet;
import static dev.astamur.taxirides.utils.TestUtils.timestamp;
import static org.assertj.core.api.Assertions.assertThat;

import dev.astamur.taxirides.model.Granularity;
import dev.astamur.taxirides.model.Ride;
import java.io.IOException;
import java.util.List;
import org.apache.hadoop.fs.Path;
import org.junit.jupiter.api.Test;

class RideReaderTest {
    @Test
    public void shouldReadOneFile() throws IOException {
        var rides = List.of(
            createRide(1, 1.0, timestamp("2024-01-26T11:00:00"), timestamp("2024-01-26T12:00:00")),
            createRide(2, 2.0, timestamp("2024-01-26T11:30:00"), timestamp("2024-01-26T12:00:00")),
            createRide(3, 3.0, timestamp("2024-01-26T12:00:00"), timestamp("2024-01-26T12:30:00")));

        var fileLocation = ridesToParquet(rides, "/tmp/test-test");

        try (var reader = Readers.createRideReader(new Path(fileLocation),
            new Ride.AvroParser(Granularity.MINUTES_15))) {
            assertThat(readAllRides(reader)).containsExactlyElementsOf(rides);
        }
    }
}