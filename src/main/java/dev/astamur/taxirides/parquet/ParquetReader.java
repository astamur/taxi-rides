package dev.astamur.taxirides.parquet;

import java.io.Closeable;
import java.util.Iterator;

public interface ParquetReader<T> extends Closeable {
    Iterator<T> read();
}
