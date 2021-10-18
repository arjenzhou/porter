package de.xab.porter.api.dataconnection;

/**
 * connection message of source datasource,
 * inner class {@link Properties} describe the behavior how data was read from source table.
 */
public final class SrcConnection extends DataConnection {
    private Properties properties;
    private String sql;

    //constructors
    private SrcConnection() {
        super();
    }

    private SrcConnection(Builder builder) {
        super(builder);
        this.sql = builder.sql;
        this.properties = builder.properties == null ? new Properties() : builder.properties;
    }

    //getter and setter
    public Properties getProperties() {
        return properties;
    }

    public String getSql() {
        return sql;
    }

    /**
     * builder
     */
    public static final class Builder extends AbstractBuilder<SrcConnection> {
        private Properties properties;
        private String sql;

        public Builder properties(Properties properties) {
            this.properties = properties;
            return this;
        }

        public Builder sql(String sql) {
            this.sql = sql;
            return this;
        }

        @Override
        public SrcConnection build() {
            return new SrcConnection(this);
        }
    }

    /**
     * properties inner class
     */
    public static final class Properties {
        private boolean readTableMeta;
        private int batchSize;

        private Properties(Builder builder) {
            readTableMeta = builder.readTableMeta;
            batchSize = builder().batchSize;
        }

        private Properties() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public boolean isReadTableMeta() {
            return readTableMeta;
        }

        public int getBatchSize() {
            return batchSize;
        }

        /**
         * properties builder
         */
        public static final class Builder {
            private boolean readTableMeta;
            private int batchSize;

            private Builder() {
            }

            public Builder readTableMeta(boolean readTableMeta) {
                this.readTableMeta = readTableMeta;
                return this;
            }

            public Builder batchSize(int batchSize) {
                this.batchSize = batchSize;
                return this;
            }

            public Properties build() {
                return new Properties(this);
            }
        }
    }
}
