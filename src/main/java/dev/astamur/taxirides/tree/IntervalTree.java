package dev.astamur.taxirides.tree;

public interface IntervalTree<V, R> {
    /**
     * Returns a collection of values from a single tree's node which exactly matches a provided interval.
     *
     * @param interval an interval's value for exact matching
     * @return a collection of values stored in a found tree's node
     */
    ValueCollection<V, R> get(Interval interval);

    void insert(Interval interval, V value);

    /**
     * Returns a collection of all values from all nodes which are fully overlapped by a query interval.
     *
     * @param interval a query interval to find all overlapped intervals
     * @return a collection of values stored in all overlapped intervals
     */
    ValueCollection<V, R> search(Interval interval);

    void print();
}
