package dev.astamur.taxirides.reader;

import dev.astamur.taxirides.model.Ride;
import java.io.IOException;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.Path;

public class Readers {
    public static Reader<Ride> createRideReader(Path path, Parser<GenericRecord, Ride> parser) throws IOException {
        return new RideReader(path, parser);
    }

    public static Reader<Ride> createRideReader(Path path, Parser<GenericRecord, Ride> parser, long limit) throws IOException {
        return new RideReader(path, parser, limit);
    }

    public static Reader<GenericRecord> createGenericRecordReader(Path path) throws IOException {
        return new GenericRecordReader(path);
    }

    public static Reader<GenericRecord> createGenericRecordReader(Path path, long limit) throws IOException {
        return new GenericRecordReader(path, limit);
    }

    public static Path asHadoopPath(java.nio.file.Path path) {
        return new Path(path.toString());
    }
}
