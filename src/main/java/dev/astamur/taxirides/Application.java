package dev.astamur.taxirides;

import dev.astamur.taxirides.model.Config;
import dev.astamur.taxirides.model.Granularity;
import dev.astamur.taxirides.processor.RideAverageDistances;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class Application implements AutoCloseable {
    private static final Logger log = LogManager.getLogger();

    private static final String PROMPT = "> ";
    private static final String LOAD_COMMAND = "load";
    private static final String QUERY_COMMAND = "query";
    private static final String EXIT_COMMAND = "exit";

    private RideAverageDistances averageDistances;
    private Path dataDir;

    public static void main(String[] args) throws IOException {
        try (var app = new Application()) {
            app.start();
        } catch (UserInterruptException e) {
            System.out.println("Exit. Bye!");
        }
    }

    public void start() throws IOException {
        Terminal terminal = TerminalBuilder.builder().system(true).build();
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).completer(new StringsCompleter(LOAD_COMMAND, QUERY_COMMAND)).build();

        while (true) {
            String line = reader.readLine(PROMPT);
            reader.getHistory().add(line);

            if (line == null) {
                printUsage();
                continue;
            }

            String[] parts = line.split(" ");

            switch (parts[0]) {
                case LOAD_COMMAND: {
                    if (averageDistances != null) {
                        System.out.println("Data was already loaded. You can only query it now.");
                        break;
                    }
                    new Load().exec(getArgs(parts));

                    if (averageDistances != null) {
                        averageDistances.init(dataDir);
                    }
                    break;
                }
                case QUERY_COMMAND: {
                    if (averageDistances == null) {
                        System.out.println("Data isn't loaded yet. Please, load it first.");
                        break;
                    }
                    new Query().exec(getArgs(parts));
                    break;
                }
                case EXIT_COMMAND:
                    return;
                default:
                    printUsage();
            }
        }
    }

    private void printUsage() {
        System.out.println("Available commands: load, query, exit");
    }

    private String[] getArgs(String[] parts) {
        return Arrays.copyOfRange(parts, 1, parts.length);
    }

    @Override
    public void close() {
        if (averageDistances != null) {
            averageDistances.close();
        }
    }

    @Command(name = "load", mixinStandardHelpOptions = true, version = "1.0",
        description = "Loads rides parquet files from local directory.")
    private class Load implements Runnable {
        @Parameters(index = "0", description = "A directory with parquet files.")
        private Path path;

        @Option(names = {"-g", "--granularity"},
            description = "Intervals granularity (SECONDS, MINUTES, MINUTES_5, MINUTES_15, HOURS, DAYS). Default: HOURS")
        private Granularity granularity;

        @Option(names = {"-q", "--queryThreadsCount"}, description = "Number of threads used for query execution. Default: 1.")
        private Integer queryThreadsCount;

        @Option(names = {"-f", "--fileThreadsCount"}, description = "Number of threads used for files loading. Default: 10.")
        private Integer fileThreadsCount;

        @Option(names = {"-l", "--limitPerFile"}, description = "Records limit per file. Default: unlimited.")
        private Long limitPerFile;

        @Option(names = {"-m", "--printMemoryLayout"}, description = "Print memory layout statistics. Default: false.")
        private boolean printMemoryLayout;

        public void exec(String... args) {
            new CommandLine(new Load()).execute(args);
        }

        @Override
        public void run() {
            averageDistances = new RideAverageDistances(getConfig());
            dataDir = path;
        }

        private Config getConfig() {
            var builder = Config.builder();
            if (granularity != null) {
                builder.granularity(granularity);
            }
            if (queryThreadsCount != null) {
                builder.queryThreadsCount(queryThreadsCount);
            }
            if (fileThreadsCount != null) {
                builder.fileThreadsCount(fileThreadsCount);
            }
            if (limitPerFile != null) {
                builder.limitPerFile(limitPerFile);
            }

            builder.printMemoryLayout(printMemoryLayout);

            return builder.build();
        }
    }

    @Command(name = "query", mixinStandardHelpOptions = true, version = "1.0",
        description = "Calculates an average of all trips started and finished within an incoming interval.")
    private class Query implements Runnable {
        @Parameters(index = "0", description = "A start of the interval (inclusively). ISO date-time: 2024-01-01T00:00:00.")
        private LocalDateTime start;

        @Parameters(index = "1", description = "An end of the interval (inclusively). ISO date-time: 2024-01-01T00:00:00.")
        private LocalDateTime end;

        @Option(names = {"-r", "--repeat"}, description = "Number of repeats for this query. Default: 1.")
        private int repeat = 1;

        public void exec(String... args) {
            new CommandLine(new Query()).execute(args);
        }

        @Override
        public void run() {
            try {
                IntStream.range(0, repeat)
                    .forEach(i -> System.out.println(averageDistances.getAverageDistances(start, end)));
            } catch (DateTimeParseException e) {
                log.error("Bad incoming interval values. ", e);
                System.out.println("Bad interval date. Use ISO date-time please (e.g. 2024-01-01T00:00:00).");
            }
        }
    }
}
