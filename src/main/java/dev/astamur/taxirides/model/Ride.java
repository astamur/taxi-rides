package dev.astamur.taxirides.model;

import java.time.Instant;

public record Ride(int passengerCount, double distance, Instant pickup, Instant dropOff) {
}
