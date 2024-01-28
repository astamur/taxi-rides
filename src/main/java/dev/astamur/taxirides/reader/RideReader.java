package dev.astamur.taxirides.reader;

import static org.apache.parquet.hadoop.util.HadoopInputFile.fromPath;

import dev.astamur.taxirides.model.Ride;
import java.io.IOException;
import javax.annotation.concurrent.NotThreadSafe;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;

@NotThreadSafe
public class RideReader implements Reader<Ride> {
    private final ParquetReader<GenericRecord> reader;
    private final Parser<GenericRecord, Ride> parser;

    private final long limit;
    private long index;

    public RideReader(Path path, Parser<GenericRecord, Ride> parser) throws IOException {
        this(path, parser, Long.MAX_VALUE);
    }

    public RideReader(Path path, Parser<GenericRecord, Ride> parser, long limit) throws IOException {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit can't be negative");
        }

        reader = AvroParquetReader.<GenericRecord>builder(fromPath(path, new Configuration())).build();
        this.parser = parser;
        this.limit = limit;
    }

    @Override
    public Ride read() throws IOException {
        if (index++ >= limit) {
            return null;
        }

        GenericRecord record = reader.read();
        return record != null ? parser.parse(record) : null;
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}
