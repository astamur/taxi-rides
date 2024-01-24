package dev.astamur.taxirides.reader;

import java.io.Closeable;
import java.util.Iterator;

public interface Reader<T> extends Closeable {
    Iterator<T> read();
}
