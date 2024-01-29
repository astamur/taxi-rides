package dev.astamur.taxirides;

import dev.astamur.taxirides.model.Config;
import dev.astamur.taxirides.model.Granularity;
import dev.astamur.taxirides.processor.RideAverageDistances;
import java.time.LocalDateTime;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A test for manual local experiments.
 */
public class MemoryTest {
    private static final Logger log = LogManager.getLogger();

    public static void main(String[] args) {
        readDir();
    }

    public static void readDir() {
        var dataDir = java.nio.file.Path.of("/Users/astamur.kirillin/git-personal/taxi-rides/data");
        var start = LocalDateTime.parse("2019-01-01T00:00:00");
        var end = LocalDateTime.parse("2022-01-01T00:00:00");

        var config = Config.builder()
            .granularity(Granularity.SECONDS)
            .limitPerFile(100_000)
            .build();

        try (var processor = new RideAverageDistances(config)) {
            processor.init(dataDir);
            IntStream.range(0, 1000).forEach(i ->
                log.info("Query {}: {}", i, processor.getAverageDistances(start, end)));
        }
    }
}
