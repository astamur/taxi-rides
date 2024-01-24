package dev.astamur.taxirides;

import dev.astamur.taxirides.model.Ride;
import dev.astamur.taxirides.tree.AVLIntervalTree;
import dev.astamur.taxirides.tree.Intervals;
import dev.astamur.taxirides.tree.StatisticsCollectionProvider;

public class Test {
    public static void main(String[] args) {
        var provider = new StatisticsCollectionProvider();
        var tree = new AVLIntervalTree<>(provider);

        tree.insert(Intervals.of(5, 20), new Ride(1, 3));
        tree.insert(Intervals.of(10, 30), new Ride(1, 3));
        tree.insert(Intervals.of(12, 15), new Ride(4, 0));
//        tree.insert(Intervals.of(15, 20), new Ride(1, 11));
//        tree.insert(Intervals.of(17, 19), new Ride(1, 13));
//        tree.insert(Intervals.of(30, 40), new Ride(1, 17));
//        tree.insert(Intervals.of(18, 18), new Ride(1, 19));
//        tree.insert(Intervals.of(13, 19), new Ride(1, 23));
        //[5, 20] max = 20
        //[10, 30] max = 30
        //[12, 15] max = 15
        //[15, 20] max = 40
        //[17, 19] max = 40
        //[30, 40] max = 40

        tree.print();

        var stats = tree.search(Intervals.of(0, 50));

        System.out.println("Found:");
        System.out.println(stats);
    }

}
