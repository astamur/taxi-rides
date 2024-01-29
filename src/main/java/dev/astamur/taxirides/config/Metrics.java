package dev.astamur.taxirides.config;

import static com.codahale.metrics.MetricRegistry.name;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import java.io.Closeable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openjdk.jol.info.GraphLayout;

public class Metrics implements Closeable {
    public static final String COUNTER_RIDES_TOTAL = "rides.counter.total";
    public static final String COUNTER_RIDES_FAILED = "rides.counter.failed";
    public static final String TIMER_RIDES_INSERT = "rides.timer.insert";
    public static final String TIMER_RIDES_QUERY = "rides.timer.query";
    public static final String METER_RIDES_READ_RATE = "rides.meter.read.rate";
    public static final String METER_RIDES_WRITE_RATE = "rides.meter.write.rate";
    public static final String HISTOGRAM_RIDES_QUEUE_SIZE = "rides.histogram.queue.size";
    private static final Logger log = LogManager.getLogger();
    private static final String MAGIC_FIELD_OFFSET_OPTION = "jol.magicFieldOffset";
    private final MetricRegistry registry = new MetricRegistry();
    private final Map<String, Counter> counters = new HashMap<>();
    private final Map<String, Timer> timers = new HashMap<>();
    private final Map<String, Meter> meters = new HashMap<>();

    private final Map<String, Histogram> histograms = new HashMap<>();

    private final ConsoleReporter reporter = ConsoleReporter.forRegistry(registry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();

    public Metrics() {
        System.setProperty(MAGIC_FIELD_OFFSET_OPTION, "true");
        registry.register("memory", new MemoryUsageGaugeSet());
        init();
    }

    public void increment(String name) {
        if (counters.containsKey(name)) {
            counters.get(name).inc();
        }
    }

    public Timer.Context time(String name) {
        var timer = timers.get(name);

        return timer != null ? timer.time() : null;
    }

    public void histogramUpdate(String name, int value) {
        if (histograms.containsKey(name)) {
            histograms.get(name).update(value);
        }
    }

    public void meterMark(String name) {
        if (meters.containsKey(name)) {
            meters.get(name).mark();
        }
    }

    public void logMemoryConsumption(Object object) {
        try {
            var layout = GraphLayout.parseInstance(object);
            log.info("MEMORY. Total size of '{}': {}", object, byteCountToDisplaySize(layout.totalSize()));
            log.info("MEMORY. Statistics for '{}': {}", object, layout.toFootprint());
        } catch (Exception e) {
            log.warn("Can't use JOL to gather detailed memory statistics", e);
        }
    }

    @Override
    public void close() {
        reporter.close();
    }

    private void init() {
        registerCounters(COUNTER_RIDES_TOTAL, COUNTER_RIDES_FAILED);
        registerTimers(TIMER_RIDES_QUERY, TIMER_RIDES_INSERT);
        registerHistograms(HISTOGRAM_RIDES_QUEUE_SIZE);
        registerMeters(METER_RIDES_READ_RATE, METER_RIDES_WRITE_RATE);
    }

    private void registerCounters(String... names) {
        Arrays.stream(names).forEach(name -> counters.put(name, registry.counter(name)));
    }

    private void registerTimers(String... names) {
        Arrays.stream(names).forEach(name -> timers.put(name, registry.timer(name(name))));
    }

    private void registerHistograms(String... names) {
        Arrays.stream(names).forEach(name -> histograms.put(name, registry.histogram(name(name))));
    }

    private void registerMeters(String... names) {
        Arrays.stream(names).forEach(name -> meters.put(name, registry.meter(name(name))));
    }
}
