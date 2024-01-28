package dev.astamur.taxirides.processor;

import static dev.astamur.taxirides.utils.TestUtils.createRide;
import static dev.astamur.taxirides.utils.TestUtils.ridesToParquet;

import dev.astamur.taxirides.model.Config;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class RideAverageDistancesTest {
    private static final LocalDateTime START = LocalDateTime.parse("2024-01-01T00:00:00");
    private static final LocalDateTime END = LocalDateTime.parse("2024-02-01T00:00:00");

    @Test
    public void shouldProcessMultipleFiles() {
        String dir = "/tmp/" + UUID.randomUUID();
        createFiles(dir, 1, 10, 1, 5.);
        createFiles(dir, 2, 15, 2, 5.);
        createFiles(dir, 3, 20, 3, 5.);
        createFiles(dir, 4, 25, 4, 5.);
        createFiles(dir, 5, 30, 5, 5.);

        try (var processor = new RideAverageDistances(Config.defaultConfig())) {
            processor.init(Path.of(dir));

            var result = processor.getAverageDistances(START, END);

            System.out.println(result);
        }
    }

    private void createFiles(String dir, int numberOfFiles, int ridesPerFile, int passengerCount, double distance) {
        IntStream.range(0, numberOfFiles)
            .forEach(i -> ridesToParquet(IntStream.range(0, ridesPerFile).mapToObj(j -> createRide(passengerCount, distance)).toList(), dir));
    }
}