package dev.astamur.taxirides.reader;

import java.io.Closeable;
import java.io.IOException;

public interface Reader<T> extends Closeable {
    T read() throws IOException;
}
