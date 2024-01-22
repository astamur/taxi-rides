package dev.astamur.taxirides.parquet;

import dev.astamur.taxirides.model.Ride;
import org.apache.hadoop.fs.Path;

public class ParquetReaders {
    public static ParquetReader<Ride> readSerial(Path... paths) {
        return new SerialParquetReader(paths);
    }
}
