package dev.astamur.taxirides.reader;

import static org.apache.parquet.hadoop.util.HadoopInputFile.fromPath;

import java.io.IOException;
import javax.annotation.concurrent.NotThreadSafe;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;

@NotThreadSafe
public class GenericRecordReader implements Reader<GenericRecord> {
    private final ParquetReader<GenericRecord> reader;

    private final long limit;
    private long index;

    public GenericRecordReader(Path path) throws IOException {
        this(path, Long.MAX_VALUE);
    }

    public GenericRecordReader(Path path, long limit) throws IOException {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit can't be negative");
        }

        reader = AvroParquetReader.<GenericRecord>builder(fromPath(path, new Configuration())).build();
        this.limit = limit;
    }

    @Override
    public GenericRecord read() throws IOException {
        if (index++ >= limit) {
            return null;
        }

        return reader.read();
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}
