package dev.astamur.taxirides.tree;

import dev.astamur.taxirides.model.Interval;
import dev.astamur.taxirides.processor.Collector;

public interface IntervalTree<V, R, C extends Collector<V, R, C>> {
    /**
     * Returns a collector with values from a single tree's node which exactly matches a provided interval.
     *
     * @param interval an interval's value for exact matching
     * @return a collector with values stored in a found tree's node
     */
    C get(Interval interval);

    void insert(Interval interval, V value);

    /**
     * Returns a collector with values from all nodes which are fully overlapped by a query interval.
     *
     * @param interval a query interval to find all overlapped intervals
     * @return a collector with values stored in all overlapped intervals
     */
    C search(Interval interval);
}
