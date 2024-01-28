package dev.astamur.taxirides;

import static dev.astamur.taxirides.reader.Readers.asHadoopPath;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

import dev.astamur.taxirides.model.Config;
import dev.astamur.taxirides.model.Granularity;
import dev.astamur.taxirides.model.Intervals;
import dev.astamur.taxirides.model.Ride;
import dev.astamur.taxirides.processor.Collector;
import dev.astamur.taxirides.processor.RideAverageDistances;
import dev.astamur.taxirides.processor.StatisticsCollector;
import dev.astamur.taxirides.reader.Parser;
import dev.astamur.taxirides.reader.Readers;
import dev.astamur.taxirides.tree.AVLIntervalTree;
import dev.astamur.taxirides.tree.IntervalTree;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.avro.generic.GenericRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessage;
import org.openjdk.jol.info.GraphLayout;

/**
 * A test for manual local experiments.
 */
public class MemoryTest {
    private static final Logger log = LogManager.getLogger();

    private static List<TestEntry<Ride, Map<Integer, Double>, StatisticsCollector>> getTestEntries() {
        var provider = StatisticsCollector.provider();
        return List.of(
            new TestEntry<>("1s", new AVLIntervalTree<>(provider), new Ride.AvroParser(Granularity.SECONDS)),
            new TestEntry<>("1m", new AVLIntervalTree<>(provider), new Ride.AvroParser(Granularity.MINUTES)),
            new TestEntry<>("5m", new AVLIntervalTree<>(provider), new Ride.AvroParser(Granularity.MINUTES_5)),
            new TestEntry<>("15m", new AVLIntervalTree<>(provider), new Ride.AvroParser(Granularity.MINUTES_15)),
            new TestEntry<>("1h", new AVLIntervalTree<>(provider), new Ride.AvroParser(Granularity.HOURS)),
            new TestEntry<>("1d", new AVLIntervalTree<>(provider), new Ride.AvroParser(Granularity.DAYS)));
    }


    public static void main(String[] args) {
        readDir();
    }

    // For manual experiments
    public static void readDir() {
        var dataDir = java.nio.file.Path.of("/Users/astamur.kirillin/Downloads/taxi-rides-data");
        var start = LocalDateTime.parse("2019-01-01T00:00:00");
        var end = LocalDateTime.parse("2022-01-01T00:00:00");

        var config = Config.builder()
            .limitPerFile(100_000)
            .build();

        try (var processor = new RideAverageDistances(config)) {
            processor.init(dataDir);
            log.info("{}", processor.getAverageDistances(start, end));
        }
    }

    // For manual experiments
    //@Test
    public void readOneFile() {
        var limit = 100_000;
        Path path = Path.of("/Users/astamur.kirillin/Downloads/taxi-rides-data/yellow_tripdata_2020-01.parquet");
        var trees = getTestEntries();

        try (var reader = Readers.createGenericRecordReader(asHadoopPath(path), limit)) {
            GenericRecord record;

            while ((record = reader.read()) != null) {
                try {
                    for (var entry : trees) {
                        var ride = entry.parser().parse(record);
                        entry.tree().insert(ride.interval(), ride);
                    }
                } catch (Exception e) {
                    log.error(new FormattedMessage("Can't process record: {}", record), e);
                }
            }


            for (var entry : trees) {
                log.info("{}: \t{}", entry.name(), entry.tree().search(Intervals.of(0, Long.MAX_VALUE)).result());
            }

            for (var entry : trees) {
                log.info("Memory ({}):\t{}", entry.name(),
                    byteCountToDisplaySize(GraphLayout.parseInstance(entry.tree()).totalSize()));
            }

            // System.out.println(GraphLayout.parseInstance(hoursTree).toFootprint());
            //tree.print();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record TestEntry<V, R, C extends Collector<V, R, C>>(
        String name,
        IntervalTree<V, R, C> tree,
        Parser<GenericRecord, V> parser) {
    }
}
