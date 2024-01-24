package dev.astamur.taxirides.tree;

public interface ValueCollection<V, R> {
    void append(V value);

    void merge(ValueCollection<V, R> collector);

    R collection();
}
