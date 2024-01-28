package dev.astamur.taxirides.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class AverageAccumulatorTest {
    @Test
    public void shouldAddSuccessfully() {
        assertThat(append(0, 1, 2, 3).average()).isEqualTo(1.5); // 6/4
        assertThat(append(0, 3, 4, 4).average()).isEqualTo(2.75); // 11/4
        assertThat(append(4, 4, 3).average()).isEqualTo(3.6666666666); // 11/3
    }

    @Test
    public void shouldMergeSuccessfully() {
        var acc1 = append(0, 0, 1, 2, 3, 3, 4, 4);
        var acc2 = append(0, 1, 2, 3);
        acc2.merge(append(0, 3, 4, 4));


        assertThat(acc1.average()).isEqualTo(2.125); // 17/8
        assertThat(acc2.average()).isEqualTo(acc1.average());
    }

    private AverageAccumulator append(double... values) {
        var accumulator = new AverageAccumulator();
        Arrays.stream(values).forEach(accumulator::append);
        return accumulator;
    }
}