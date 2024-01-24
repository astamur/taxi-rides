package dev.astamur.taxirides.tree;

public interface Statistics<V> {
    void append(V value);

    void append(Iterable<V> values);

    void merge(Statistics<V> statistics);
}
