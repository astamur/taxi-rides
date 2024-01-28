package dev.astamur.taxirides.processor;

public interface Collector<V, R, C extends Collector<V, R, C>> {
    void add(V value);

    void merge(C collector);

    R result();
}
