package dev.astamur.taxirides.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public enum Granularity {
    SECONDS {
        @Override
        public long truncate(long timestamp, ChronoUnit chronoUnit) {
            return doTruncate(timestamp, chronoUnit, ChronoUnit.SECONDS);
        }
    }, MINUTES {
        @Override
        public long truncate(long timestamp, ChronoUnit chronoUnit) {
            return doTruncate(timestamp, chronoUnit, ChronoUnit.MINUTES);
        }
    }, MINUTES_5 {
        @Override
        public long truncate(long timestamp, ChronoUnit chronoUnit) {
            return doTruncate(timestamp, chronoUnit, 5);
        }
    }, MINUTES_15 {
        @Override
        public long truncate(long timestamp, ChronoUnit chronoUnit) {
            return doTruncate(timestamp, chronoUnit, 15);
        }
    }, HOURS {
        @Override
        public long truncate(long timestamp, ChronoUnit chronoUnit) {
            return doTruncate(timestamp, chronoUnit, ChronoUnit.HOURS);
        }
    }, DAYS {
        @Override
        public long truncate(long timestamp, ChronoUnit chronoUnit) {
            return doTruncate(timestamp, chronoUnit, ChronoUnit.DAYS);
        }
    };

    private static long doTruncate(long timestamp, ChronoUnit originUnit, int minutesInterval) {
        var instantInMinutes = Instant.EPOCH.plus(timestamp, originUnit)
            .truncatedTo(ChronoUnit.MINUTES).atZone(ZoneOffset.UTC);
        var minutes = instantInMinutes.getMinute();

        return instantInMinutes.minusMinutes(minutes % minutesInterval).toInstant().toEpochMilli();
    }

    private static long doTruncate(long timestamp, ChronoUnit originUnit, ChronoUnit targetUnit) {
        return Instant.EPOCH.plus(timestamp, originUnit)
            .truncatedTo(targetUnit).toEpochMilli();
    }

    /**
     * Truncates incoming timestamps in the {@link ChronoUnit} format to required granularity.
     * A UTC timezone is used.
     *
     * @param timestamp  an incoming timestamp
     * @param chronoUnit a unit of the incoming timestamp
     * @return a truncated value in milliseconds
     */
    public long truncate(long timestamp, ChronoUnit chronoUnit) {
        throw new UnsupportedOperationException();
    }

    /**
     * Truncates incoming {@link LocalDateTime} to required granularity.
     * A UTC timezone is used.
     *
     * @param localDateTime an incoming {@link ChronoUnit}
     * @return a truncated value in milliseconds
     */
    public long truncate(LocalDateTime localDateTime) {
        return truncate(localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli(), ChronoUnit.MILLIS);
    }
}