package dev.astamur.taxirides.processor;

public interface CollectorProvider<V, R, C extends Collector<V, R, C>> {
    C create();

    C create(V value);
}
