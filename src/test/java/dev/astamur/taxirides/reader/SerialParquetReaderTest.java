package dev.astamur.taxirides.reader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.junit.jupiter.api.Test;

class SerialParquetReaderTest {
    @Test
    public void shouldReadOneFile() throws IOException {
        try (var reader = Readers.serialParquetReader(new Path("test"))) {
            assertNotNull(reader.read());
        }
    }
}