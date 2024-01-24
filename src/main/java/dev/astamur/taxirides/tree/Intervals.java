package dev.astamur.taxirides.tree;

public final class Intervals {
    public static Interval of(long start, long end) {
        return new BaseInterval(start, end);
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
