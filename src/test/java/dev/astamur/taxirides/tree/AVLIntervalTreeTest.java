package dev.astamur.taxirides.tree;

import static dev.astamur.taxirides.utils.TestUtils.createRide;
import static dev.astamur.taxirides.utils.TestUtils.populateRides;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.astamur.taxirides.model.Intervals;
import dev.astamur.taxirides.processor.ListCollector;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AVLIntervalTreeTest {
    @Test
    public void shouldGetExactValue() {
        var tree = new AVLIntervalTree<>(ListCollector.provider());
        var ride = createRide(1, 1.0, 0, 1000);
        populateRides(tree, List.of(ride, createRide(1, 2.0, 0, 500)));

        var collector = tree.get(Intervals.of(0, 1000));

        assertNotNull(collector);
        assertThat(collector.result()).containsExactly(ride);
    }

    @Test
    public void shouldSearchAllWithinQueryInterval() {
        var tree = new AVLIntervalTree<>(ListCollector.provider());
        var rides = List.of(
            createRide(1, 1.0, 0, 500),
            createRide(1, 2.0, 0, 1000),
            createRide(1, 3.0, 0, 1500));

        populateRides(tree, rides);

        var collector = tree.search(Intervals.of(0, 1000));

        assertNotNull(collector);
        assertThat(collector.result()).containsExactlyInAnyOrder(rides.get(0), rides.get(1));
    }

    @Test
    public void shouldWorkWithDuplicates() {
        var tree = new AVLIntervalTree<>(ListCollector.provider());
        var rides = List.of(
            createRide(1, 1.0, 0, 500),
            createRide(1, 1.0, 0, 500));

        populateRides(tree, rides);

        var collector = tree.search(Intervals.of(0, 1000));

        assertNotNull(collector);
        assertThat(collector.result()).containsExactlyInAnyOrderElementsOf(rides);
    }

    @Test
    public void shouldWorkWithEmptyIntervals() {
        var tree = new AVLIntervalTree<>(ListCollector.provider());
        var rides = List.of(
            createRide(1, 1.0, 100, 100),
            createRide(1, 1.0, 200, 200));

        populateRides(tree, rides);

        var collector = tree.search(Intervals.of(0, 500));

        assertNotNull(collector);
        assertThat(collector.result()).containsExactlyInAnyOrderElementsOf(rides);
    }
}