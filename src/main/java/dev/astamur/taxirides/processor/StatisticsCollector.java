package dev.astamur.taxirides.processor;

import dev.astamur.taxirides.model.Ride;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsCollector implements Collector<Ride, Map<Integer, Double>, StatisticsCollector> {
    private final Map<Integer, AverageAccumulator> averages = new HashMap<>();

    public StatisticsCollector() {
    }

    public StatisticsCollector(Ride ride) {
        add(ride);
    }

    public static StatisticsCollectorProvider provider() {
        return new StatisticsCollectorProvider();
    }

    @Override
    public void add(Ride ride) {
        averages.computeIfAbsent(ride.passengerCount(), k -> new AverageAccumulator()).append(ride.distance());
    }

    @Override
    public void merge(StatisticsCollector collector) {
        if (collector == null) {
            return;
        }

        collector.averages.forEach((key, value) -> averages.computeIfAbsent(key, k -> new AverageAccumulator()).merge(value));
    }

    @Override
    public Map<Integer, Double> result() {
        return averages.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().average()));
    }

    @Override
    public String toString() {
        return averages.toString();
    }

    public static class StatisticsCollectorProvider implements CollectorProvider<Ride, Map<Integer, Double>, StatisticsCollector> {
        @Override
        public StatisticsCollector create() {
            return new StatisticsCollector();
        }

        @Override
        public StatisticsCollector create(Ride value) {
            return new StatisticsCollector(value);
        }
    }
}
