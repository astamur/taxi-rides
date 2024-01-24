package dev.astamur.taxirides.tree;

public interface Interval extends Comparable<Interval> {
    long start();

    long end();
}
