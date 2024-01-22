package dev.astamur.taxirides.parquet;

import dev.astamur.taxirides.model.Ride;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.apache.hadoop.fs.Path;

public class SerialParquetReader implements ParquetReader<Ride> {
    private final List<Path> paths;

    public SerialParquetReader(Path... paths) {
        this.paths = List.of(paths);
    }

    @Override
    public Iterator<Ride> read() {
        // TODO: Implement a serial reading
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
