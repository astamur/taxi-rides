package dev.astamur.taxirides.reader;

import dev.astamur.taxirides.model.Ride;
import org.apache.hadoop.fs.Path;

public class Readers {
    public static Reader<Ride> serialParquetReader(Path... paths) {
        return new SerialParquetReader(paths);
    }
}
