package dev.astamur.taxirides.tree;

import dev.astamur.taxirides.model.Ride;

public class StatisticsCollection implements ValueCollection<Ride, Statistics<Ride>> {
    private final Statistics<Ride> statistics;

    public StatisticsCollection(Statistics<Ride> statistics) {
        this.statistics = statistics;
    }

    @Override
    public void append(Ride value) {
        statistics.append(value);
    }

    @Override
    public void merge(ValueCollection<Ride, Statistics<Ride>> collection) {
        statistics.merge(collection.collection());
    }

    @Override
    public Statistics<Ride> collection() {
        return statistics;
    }
}
