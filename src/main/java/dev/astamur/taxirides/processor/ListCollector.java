package dev.astamur.taxirides.processor;

import dev.astamur.taxirides.model.Ride;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class ListCollector implements Collector<Ride, Collection<Ride>, ListCollector> {
    private final Collection<Ride> rides = new LinkedList<>();

    public static ListCollectorProvider provider() {
        return new ListCollectorProvider();
    }

    @Override
    public void add(Ride value) {
        rides.add(value);
    }

    @Override
    public void merge(ListCollector collector) {
        rides.addAll(collector.result());
    }

    @Override
    public Collection<Ride> result() {
        return Collections.unmodifiableCollection(rides);
    }

    public static class ListCollectorProvider
        implements CollectorProvider<Ride, Collection<Ride>, ListCollector> {
        @Override
        public ListCollector create() {
            return new ListCollector();
        }

        @Override
        public ListCollector create(Ride value) {
            var collector = create();
            collector.add(value);
            return collector;
        }
    }
}
