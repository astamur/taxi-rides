package dev.astamur.taxirides.processor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class AverageAccumulator {
    // Used for calculation to reduce cumulative error
    private static final RoundingMode DEFAULT_MODE = RoundingMode.HALF_EVEN;

    // Used for scaling final output to remove a cumulative error at the end
    private static final RoundingMode OUTPUT_MODE = RoundingMode.DOWN;
    private static final int DEFAULT_SCALE = 20;
    private static final int OUTPUT_SCALE = 10;
    private long count;
    private BigDecimal average;

    public AverageAccumulator() {
        this(0, BigDecimal.ZERO);
    }

    public AverageAccumulator(double initialValue) {
        this(1, asBigDecimal(initialValue));
    }

    public AverageAccumulator(long count, BigDecimal average) {
        this.count = count;
        this.average = average;
    }

    private static BigDecimal asBigDecimal(double value) {
        return new BigDecimal(String.valueOf(value));
    }

    private static BigDecimal asBigDecimal(long value) {
        return BigDecimal.valueOf(value);
    }

    public void append(double value) {
        count += 1;
        average = average.add(asBigDecimal(value).subtract(average)
            .divide(asBigDecimal(count), DEFAULT_SCALE, DEFAULT_MODE));
    }

    public void merge(AverageAccumulator accumulator) {
        long total = count + accumulator.count;

        BigDecimal left = average.multiply(asBigDecimal(count))
            .divide(asBigDecimal(total), DEFAULT_MODE);

        BigDecimal right = accumulator.average.multiply(asBigDecimal(accumulator.count))
            .divide(asBigDecimal(total), DEFAULT_MODE);

        count = total;
        average = left.add(right);
    }

    public double average() {
        return average.setScale(OUTPUT_SCALE, OUTPUT_MODE).doubleValue();
    }

    public long count() {
        return count;
    }

    @Override
    public String toString() {
        return String.format("{count=%d, average=%s}", count, average);
    }
}
