package dev.astamur.taxirides.model;

import dev.astamur.taxirides.reader.Parser;
import java.time.temporal.ChronoUnit;
import org.apache.avro.generic.GenericRecord;


public record Ride(int passengerCount, double distance, Interval interval) {
    public record AvroParser(Granularity granularity) implements Parser<GenericRecord, Ride> {
        public static final String FIELD_PASSENGER_COUNT = "passenger_count";
        public static final String FIELD_DISTANCE = "trip_distance";
        public static final String FIELD_PICKUP_TIME = "tpep_pickup_datetime";
        public static final String FIELD_DROP_OFF_TIME = "tpep_dropoff_datetime";

        @Override
        public Ride parse(GenericRecord record) {
            return parseRide(record);
        }

        private Ride parseRide(GenericRecord record) {
            try {
                validate(record);

                var count = (int) Double.parseDouble(record.get(FIELD_PASSENGER_COUNT).toString());
                var distance = Double.parseDouble(record.get(FIELD_DISTANCE).toString());

                return new Ride(count, distance, parseInterval(record));
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("Parsing exception. Record: %s", record), e);
            }
        }

        private Interval parseInterval(GenericRecord record) {
            var startMicros = Long.parseLong(record.get(FIELD_PICKUP_TIME).toString());
            var endMicros = Long.parseLong(record.get(FIELD_DROP_OFF_TIME).toString());

            return Intervals.of(
                granularity.truncate(startMicros, ChronoUnit.MICROS),
                granularity.truncate(endMicros, ChronoUnit.MICROS));
        }

        private void validate(GenericRecord record) {
            if (record == null ||
                record.get(FIELD_PASSENGER_COUNT) == null ||
                record.get(FIELD_DISTANCE) == null ||
                record.get(FIELD_PICKUP_TIME) == null ||
                record.get(FIELD_DROP_OFF_TIME) == null) {
                throw new IllegalArgumentException(String.format("Invalid format. Record: %s", record));
            }
        }

    }
}
