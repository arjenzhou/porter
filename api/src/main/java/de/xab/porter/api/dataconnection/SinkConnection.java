package de.xab.porter.api.dataconnection;

/**
 * connection message of sink datasource,
 * inner class {@link Properties} describe the behavior how sink table was handled.
 */
public final class SinkConnection extends DataConnection {
    private Properties properties;

    //constructors
    public SinkConnection() {
        super();
    }

    protected SinkConnection(Builder builder) {
        super(builder);
        this.properties = builder.properties == null ? new Properties() : builder.properties;
    }

    public Properties getProperties() {
        return properties;
    }

    /**
     * builder
     */
    public static final class Builder extends AbstractBuilder<SinkConnection> {
        private Properties properties;

        public Builder properties(Properties properties) {
            this.properties = properties;
            return this;
        }

        @Override
        public SinkConnection build() {
            return new SinkConnection(this);
        }
    }

    /**
     * properties inner class
     */
    public static final class Properties {
        public static final String PREPARE_BATCH_MODE = "PREPARE_BATCH";
        public static final String STATEMENT_BATCH_MODE = "STATEMENT_BATCH";
        public static final String STATEMENT_VALUES_MODE = "STATEMENT_VALUES";

        private String writeMode = "";
        private boolean allColumns = true;
        private String quote;
        private String tableIdentifier;
        private boolean create;
        private boolean drop;

        private Properties(Builder builder) {
            writeMode = builder.writeMode;
            allColumns = builder.allColumns;
            quote = builder.quote;
            tableIdentifier = builder.tableIdentifier;
            create = builder.create;
            drop = builder.drop;
        }

        public Properties() {

        }

        public static Builder builder() {
            return new Builder();
        }

        public static String getPrepareBatchMode() {
            return PREPARE_BATCH_MODE;
        }

        public static String getStatementBatchMode() {
            return STATEMENT_BATCH_MODE;
        }

        public static String getStatementValuesMode() {
            return STATEMENT_VALUES_MODE;
        }

        public String getWriteMode() {
            return writeMode;
        }

        public boolean isAllColumns() {
            return allColumns;
        }

        public String getQuote() {
            return quote;
        }

        public void setQuote(String quote) {
            this.quote = quote;
        }

        public String getTableIdentifier() {
            return tableIdentifier;
        }

        public void setTableIdentifier(String tableIdentifier) {
            this.tableIdentifier = tableIdentifier;
        }

        public boolean isCreate() {
            return create;
        }

        public boolean isDrop() {
            return drop;
        }

        /**
         * properties builder
         */
        public static final class Builder {
            private String writeMode;
            private boolean allColumns;
            private String quote;
            private String tableIdentifier;
            private boolean create;
            private boolean drop;

            private Builder() {
            }

            public Builder writeMode(String writeMode) {
                this.writeMode = writeMode;
                return this;
            }

            public Builder allColumns(boolean allColumns) {
                this.allColumns = allColumns;
                return this;
            }

            public Builder quote(String quote) {
                this.quote = quote;
                return this;
            }

            public Builder tableIdentifier(String tableIdentifier) {
                this.tableIdentifier = tableIdentifier;
                return this;
            }

            public Builder create(boolean create) {
                this.create = create;
                return this;
            }

            public Builder drop(boolean drop) {
                this.drop = drop;
                return this;
            }

            public Properties build() {
                return new Properties(this);
            }
        }
    }
}