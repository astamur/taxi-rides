package dev.astamur.taxirides.processor;

import static dev.astamur.taxirides.config.Metrics.COUNTER_RIDES_FAILED;
import static dev.astamur.taxirides.config.Metrics.COUNTER_RIDES_TOTAL;
import static dev.astamur.taxirides.config.Metrics.HISTOGRAM_RIDES_QUEUE_SIZE;
import static dev.astamur.taxirides.config.Metrics.METER_RIDES_READ_RATE;
import static dev.astamur.taxirides.config.Metrics.METER_RIDES_WRITE_RATE;
import static dev.astamur.taxirides.config.Metrics.TIMER_RIDES_INSERT;
import static dev.astamur.taxirides.config.Metrics.TIMER_RIDES_QUERY;

import dev.astamur.taxirides.config.Metrics;
import dev.astamur.taxirides.model.Config;
import dev.astamur.taxirides.model.Intervals;
import dev.astamur.taxirides.model.Ride;
import dev.astamur.taxirides.reader.Parser;
import dev.astamur.taxirides.reader.Readers;
import dev.astamur.taxirides.tree.AVLIntervalTree;
import dev.astamur.taxirides.tree.IntervalTree;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.avro.generic.GenericRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RideAverageDistances implements AverageDistances {
    private static final Logger log = LogManager.getLogger();

    private final IntervalTree<Ride, Map<Integer, Double>, StatisticsCollector> tree =
        new AVLIntervalTree<>(StatisticsCollector.provider());

    private final Config config;

    private final Metrics metrics = new Metrics();

    public RideAverageDistances(Config config) {
        this.config = config;
    }

    @Override
    public void init(Path dataDir) {
        try (var loader = new FilesLoader(dataDir)) {
            loader.load();
        }
        if (config.printMemoryLayout()) {
            metrics.logMemoryConsumption(this);
        }
    }

    @Override
    public Map<Integer, Double> getAverageDistances(LocalDateTime start, LocalDateTime end) {
        var interval = Intervals.of(
            config.granularity().truncate(start),
            config.granularity().truncate(end));

        try (var ctx = metrics.time(TIMER_RIDES_QUERY)) {
            return tree.search(interval).result();
        }
    }

    @Override
    public void close() {
        metrics.close();
    }

    /**
     * An inner class which encapsulates files reading and tree population tasks.
     */
    private final class FilesLoader implements AutoCloseable {
        private static final Logger log = LogManager.getLogger();

        private static final String PARQUET_EXTENSION = ".parquet";

        private static final int DEFAULT_QUEUE_CAPACITY = 1000;
        private static final int DEFAULT_READ_TIMEOUT_IN_MILLIS = 10;
        private static final int DEFAULT_WRITE_TIMEOUT_IN_MILLIS = 30_000;

        private final ExecutorService filesExecutor;
        private final ExecutorService treeExecutor = Executors.newSingleThreadExecutor();
        private final BlockingQueue<Ride> ridesQueue = new ArrayBlockingQueue<>(DEFAULT_QUEUE_CAPACITY);
        private final Parser<GenericRecord, Ride> parser;

        private final Path dataDir;

        private volatile boolean readingFinished = false;


        public FilesLoader(Path dataDir) {
            this.dataDir = dataDir;
            filesExecutor = Executors.newFixedThreadPool(config.fileThreadsCount());
            parser = new Ride.AvroParser(config.granularity());
        }

        public void load() {
            var fileFutures = new ArrayList<Future<?>>();

            try (var files = Files.list(dataDir).filter(this::isParquet)) {
                files.forEach(filePath -> fileFutures.add(
                    filesExecutor.submit(new FileReader(filePath))));
            } catch (IOException e) {
                throw new RuntimeException("Can't process files.", e);
            }

            await(fileFutures, treeExecutor.submit(new TreeWriter()));
        }


        private boolean isParquet(Path path) {
            return path != null && path.getFileName().toString().endsWith(PARQUET_EXTENSION);
        }

        private void terminateExecutors() {
            terminateExecutor(treeExecutor);
            terminateExecutor(filesExecutor);
        }

        private void terminateExecutor(ExecutorService executorService) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                        log.error("Executor service '{}' didn't terminate", executorService.toString());
                    }
                }
            } catch (InterruptedException ie) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        private void await(List<Future<?>> fileFutures, Future<?> treeFuture) {
            try {
                for (var future : fileFutures) {
                    try {
                        future.get();
                    } catch (ExecutionException | CancellationException e) {
                        log.error("Exception in a reader thread", e);
                    }
                }

                readingFinished = true;

                try {
                    treeFuture.get();
                } catch (ExecutionException e) {
                    log.error("Exception in a tree populating thread", e);
                }

                log.info("Tree population finished");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Main thread was interrupted");
            } finally {
                readingFinished = true;
            }
        }

        @Override
        public void close() {
            terminateExecutors();
        }

        private final class FileReader implements Runnable {
            private static final Logger log = LogManager.getLogger();

            private final Path path;

            private FileReader(Path path) {
                this.path = path;
            }

            @Override
            public void run() {
                try (var reader = Readers.createRideReader(Readers.asHadoopPath(path), parser, config.limitPerFile())) {
                    Ride ride;
                    while (true) {
                        try {
                            if ((ride = reader.read()) == null) {
                                log.info("Finished reading file: {}.", path);
                                return;
                            }
                            metrics.meterMark(METER_RIDES_READ_RATE);
                            while (!ridesQueue.offer(ride, DEFAULT_WRITE_TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS)) {
                                log.warn("Writing timeout triggered");
                            }
                            metrics.increment(COUNTER_RIDES_TOTAL);
                        } catch (IllegalArgumentException e) {
                            // Log and skip a single row parsing problem
                            log.debug("Can't process record", e);
                            metrics.increment(COUNTER_RIDES_FAILED);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(String.format("Can't process file: '%s'", path), e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("File reader was interrupted");
                }
            }
        }

        private final class TreeWriter implements Runnable {
            private static final Logger log = LogManager.getLogger();

            @Override
            public void run() {
                Ride ride;
                try {
                    while (!readingFinished) {
                        metrics.histogramUpdate(HISTOGRAM_RIDES_QUEUE_SIZE, ridesQueue.size());
                        if ((ride = ridesQueue.poll(DEFAULT_READ_TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS)) != null) {
                            metrics.meterMark(METER_RIDES_WRITE_RATE);
                            try (var ctx = metrics.time(TIMER_RIDES_INSERT)) {
                                tree.insert(ride.interval(), ride);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Tree writer was interrupted");
                }
            }
        }
    }
}
