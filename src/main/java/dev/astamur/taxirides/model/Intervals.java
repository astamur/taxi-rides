package dev.astamur.taxirides.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class Intervals {
    public static Interval of(long start, long end) {
        return new BaseInterval(start, end);
    }

    public static Interval of(LocalDateTime start, LocalDateTime end) {
        return new BaseInterval(
            start.toInstant(ZoneOffset.UTC).toEpochMilli(),
            end.toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    public record BaseInterval(long start, long end) implements Interval {
        @Override
        public int compareTo(final Interval o) {
            if (this.start() < o.start()) {
                return -1;
            } else if (this.start() > o.start()) {
                return 1;
            } else {
                return Long.compare(this.end(), o.end());
            }
        }
    }
}
