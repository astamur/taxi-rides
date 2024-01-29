package dev.astamur.taxirides.utils;

import static java.nio.file.Path.of;

import dev.astamur.taxirides.model.Intervals;
import dev.astamur.taxirides.model.Ride;
import dev.astamur.taxirides.processor.Collector;
import dev.astamur.taxirides.reader.Reader;
import dev.astamur.taxirides.tree.IntervalTree;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.avro.Schema;
import org.apache.avro.data.TimeConversions;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.hadoop.util.HadoopOutputFile;
import org.apache.parquet.io.OutputFile;


public class TestUtils {
    public static final LocalDateTime START = LocalDateTime.parse("2024-01-01T00:00:00");
    public static final LocalDateTime END = LocalDateTime.parse("2024-02-01T00:00:00");
    private static final Logger log = LogManager.getLogger();
    private static final String RIDE_SCHEMA_FILE = "ride.avsc";
    private static final String DEFAULT_PARQUET_DIR = "/tmp";
    private static final Schema RIDE_SCHEMA = parseRideSchema();

    private static final Random random = new Random();

    private static Schema parseRideSchema() {
        try (var stream = TestUtils.class.getClassLoader().getResourceAsStream(RIDE_SCHEMA_FILE)) {
            return new Schema.Parser().parse(stream);
        } catch (IOException e) {
            throw new RuntimeException("Can't parse a test avro schema for rides", e);
        }
    }

    public static Ride createRide() {
        return createRide(random.nextInt(10), random.nextDouble(10));
    }

    public static Ride createRide(int count, double distance) {
        // An interval within January 2024
        var diff = timestamp(END) - timestamp(START);
        var start = timestamp(START);
        var end = start + random.nextLong(diff);

        return new Ride(count, distance, Intervals.of(start, end));
    }

    public static Ride createRide(int count, double distance, long start, long end) {
        return new Ride(count, distance, Intervals.of(start, end));
    }

    public static GenericRecord covertRide(Ride ride) {
        return createRideRecord(ride.passengerCount(), ride.distance(), ride.interval().start(), ride.interval().end());
    }

    public static GenericRecord createRideRecord(double count, double distance, long start, long end) {
        var record = new GenericData.Record(RIDE_SCHEMA);
        record.put(0, count);
        record.put(1, distance);
        record.put(2, start * 1000); // original data is in microseconds
        record.put(3, end * 1000); // original data is in microseconds
        return record;
    }

    public static long timestamp(String dateTime) {
        return timestamp(LocalDateTime.parse(dateTime));
    }

    public static long timestamp(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static <R, C extends Collector<Ride, R, C>> void populateRides(IntervalTree<Ride, R, C> tree,
                                                                          Collection<Ride> values) {
        if (values == null) {
            return;
        }
        values.forEach(ride -> tree.insert(ride.interval(), ride));
    }

    public static String ridesToParquet(Collection<Ride> rides) {
        return ridesToParquet(rides, DEFAULT_PARQUET_DIR);
    }

    public static String ridesToParquet(Collection<Ride> rides, String filesDir) {
        if (rides == null || rides.isEmpty()) {
            throw new IllegalArgumentException("Rides collection is empty or null");
        }

        return recordsToParquet(rides.stream().map(TestUtils::covertRide).toList(), filesDir);
    }

    public static String recordsToParquet(Collection<GenericRecord> records) {
        return recordsToParquet(records, DEFAULT_PARQUET_DIR);
    }

    public static String recordsToParquet(Collection<GenericRecord> records, String filesDir) {
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("Records collection is empty or null");
        }
        Schema schema = records.iterator().next().getSchema();
        String fileName = String.format(of(filesDir, UUID.randomUUID() + ".parquet").toString());
        GenericData genericData = GenericData.get();
        genericData.addLogicalTypeConversion(new TimeConversions.TimeMillisConversion());


        try {
            OutputFile outputFile = HadoopOutputFile.fromPath(new Path(fileName), new Configuration());

            try (ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(outputFile)
                .withSchema(schema)
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .build()) {
                records.forEach(record -> {
                    try {
                        writer.write(record);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            log.info("Parquet file '{}' was created", fileName);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Collection<T> readAllRides(Reader<T> reader) {
        return Stream.generate(() -> uncheckedRead(reader))
            .takeWhile(Objects::nonNull)
            .toList();
    }

    private static <T> T uncheckedRead(Reader<T> reader) {
        try {
            return reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
