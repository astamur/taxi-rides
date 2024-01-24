package dev.astamur.taxirides.reader;

import dev.astamur.taxirides.model.Ride;
import dev.astamur.taxirides.tree.AVLIntervalTree;
import dev.astamur.taxirides.tree.Intervals;
import dev.astamur.taxirides.tree.StatisticsCollectionProvider;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.InputFile;

public class SerialParquetReader implements Reader<Ride> {
    private final List<Path> paths;

    public SerialParquetReader(Path... paths) {
        this.paths = List.of(paths);
    }

    public static void main(String[] args) {
        Path path = new Path("/Users/astamur.kirillin/Downloads/yellow_tripdata_2020-01.parquet");

        try (var reader = new SerialParquetReader(path)) {
            reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterator<Ride> read() {
        InputFile inputFile;
        try {
            inputFile = HadoopInputFile.fromPath(paths.get(0), new Configuration());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var provider = new StatisticsCollectionProvider();
        var tree = new AVLIntervalTree<>(provider);

        try (ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(inputFile).build()) {
            int i = 0;
            GenericRecord record;
            while (i++ < 10_000 && (record = reader.read()) != null) {
                try {
                    if (record.get("passenger_count") == null) {
                        continue;
                    }
                    var start = Long.parseLong(record.get("tpep_pickup_datetime").toString()) / 1000;
                    var end = Long.parseLong(record.get("tpep_dropoff_datetime").toString()) / 1000;

                    var unit = ChronoUnit.MINUTES;
//                    var unit = ChronoUnit.HOURS;

                    var interval = Intervals.of(truncateTimestamp(start, unit), truncateTimestamp(end, unit));
//                    var interval = Intervals.of(start, end);

                    var ride = new Ride(
                        (int) Double.parseDouble(record.get("passenger_count").toString()),
                        Double.parseDouble(record.get("trip_distance").toString()));

                    tree.insert(interval, ride);
                } catch (Exception e) {
                    // Skip
                }
            }

            var stats = tree.search(Intervals.of(0, Long.MAX_VALUE));
            System.out.println("Found:");
            System.out.println(stats.collection());

            //System.out.println(GraphLayout.parseInstance(tree).toFootprint());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private long truncateTimestamp(long timestamp, ChronoUnit unit) {
        return Instant.ofEpochMilli(timestamp / 1000)
            .truncatedTo(unit)
            .toEpochMilli();
    }

    @Override
    public void close() throws IOException {

    }
}
