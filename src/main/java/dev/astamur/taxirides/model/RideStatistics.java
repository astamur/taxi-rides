package dev.astamur.taxirides.model;

import dev.astamur.taxirides.tree.Statistics;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class RideStatistics implements Statistics<Ride> {
    private final Map<Integer, AverageAccumulator> averages = new HashMap<>();

    public RideStatistics() {
    }

    public RideStatistics(Ride ride) {
        append(ride);
    }

    @Override
    public void append(Ride ride) {
        averages.computeIfAbsent(ride.passengerCount(), k -> new AverageAccumulator()).append(ride.distance());
    }

    @Override
    public void append(Iterable<Ride> values) {
        values.forEach(this::append);
    }

    @Override
    public void merge(Statistics<Ride> statistics) {
        if (statistics == null) {
            return;
        }

        if (!(statistics instanceof RideStatistics)) {
            throw new RuntimeException("Can't merge different statistics");
        }

        ((RideStatistics) statistics).averages.forEach((key, value) -> averages.computeIfAbsent(key, k -> new AverageAccumulator()).merge(value));
    }

    public Map<Integer, Double> toResultMap() {
        return averages.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().average()));
    }

    @Override
    public String toString() {
        return averages.toString();
    }
}
