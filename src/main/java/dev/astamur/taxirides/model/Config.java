package dev.astamur.taxirides.model;

public record Config(Granularity granularity, int fileThreadsCount, long limitPerFile, boolean printMemoryLayout) {
    private Config(Builder builder) {
        this(builder.granularity, builder.fileThreadsCount, builder.limitPerFile, builder.printMemoryLayout);
    }

    public static Config defaultConfig() {
        return new Builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Granularity granularity = Granularity.HOURS;
        private int fileThreadsCount = 10;
        private long limitPerFile = Long.MAX_VALUE;
        private boolean printMemoryLayout = false;

        public Builder granularity(Granularity granularity) {
            this.granularity = granularity;
            return this;
        }

        public Builder fileThreadsCount(int fileThreadsCount) {
            this.fileThreadsCount = fileThreadsCount;
            return this;
        }

        public Builder limitPerFile(long limitPerFile) {
            this.limitPerFile = limitPerFile;
            return this;
        }

        public Builder printMemoryLayout(boolean printMemoryLayout) {
            this.printMemoryLayout = printMemoryLayout;
            return this;
        }

        public Config build() throws IllegalStateException {
            validate();
            return new Config(this);
        }

        private void validate() throws IllegalStateException {
            StringBuilder sb = new StringBuilder();
            if (fileThreadsCount < 1) {
                sb.append("'fileThreadsCount' should be greater than 0.");
            } else if (limitPerFile < 0) {
                sb.append("'limitPerFile' should be greater than or equal 0");
            }

            if (!sb.isEmpty()) {
                throw new IllegalStateException(sb.toString());
            }
        }
    }
}
