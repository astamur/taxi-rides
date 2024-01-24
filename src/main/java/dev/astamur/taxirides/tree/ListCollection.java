package dev.astamur.taxirides.tree;

import dev.astamur.taxirides.model.Ride;
import java.util.Collection;
import java.util.LinkedList;

public class ListCollection implements ValueCollection<Ride, Collection<Ride>> {
    private final Collection<Ride> stats = new LinkedList<>();

    @Override
    public void append(Ride value) {
        stats.add(value);
    }

    @Override
    public void merge(ValueCollection<Ride, Collection<Ride>> collector) {
        stats.addAll(collector.collection());
    }

    @Override
    public Collection<Ride> collection() {
        return stats;
    }
}
