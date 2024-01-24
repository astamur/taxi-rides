package dev.astamur.taxirides.tree;

import dev.astamur.taxirides.model.Ride;
import dev.astamur.taxirides.model.RideStatistics;

public class StatisticsCollectionProvider implements ValueCollectionProvider<Ride, Statistics<Ride>> {
    @Override
    public ValueCollection<Ride, Statistics<Ride>> emptyCollection() {
        return new StatisticsCollection(new RideStatistics());
    }

    @Override
    public ValueCollection<Ride, Statistics<Ride>> collectionOf(Ride value) {
        return new StatisticsCollection(new RideStatistics(value));
    }
}
