package dev.astamur.taxirides.model;

public interface Interval extends Comparable<Interval> {
    long start();

    long end();
}
