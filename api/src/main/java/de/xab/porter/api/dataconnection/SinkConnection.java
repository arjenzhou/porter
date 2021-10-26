package de.xab.porter.api.dataconnection;

/**
 * connection message of sink datasource,
 * inner class {@link Properties} describe the behavior how sink table was handled.
 */
public final class SinkConnection extends DataConnection {
    private final Environments environments = new Environments();
    private Properties properties;

    //constructors
    private SinkConnection() {
        super();
    }

    private SinkConnection(Builder builder) {
        super(builder);
        this.properties = builder.properties == null ? new Properties() : builder.properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public Environments getEnvironments() {
        return environments;
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

    public static final class Environments {
        private String quote;
        private String tableIdentifier;

        private Environments() {
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
    }

    /**
     * properties inner class
     */
    public static final class Properties {
        public static final String PREPARE_BATCH_MODE = "PREPARED_BATCH";
        public static final String STATEMENT_BATCH_MODE = "STATEMENT_BATCH";
        public static final String STATEMENT_VALUES_MODE = "STATEMENT_VALUES";

        private String writeMode = STATEMENT_VALUES_MODE;
        private boolean create;

        private Properties(PropertiesBuilder builder) {
            writeMode = builder.writeMode;
            create = builder.create;
        }

        private Properties() {
        }

        public static PropertiesBuilder builder() {
            return new PropertiesBuilder();
        }

        public String getWriteMode() {
            return writeMode;
        }

        public boolean isCreate() {
            return create;
        }

        /**
         * properties builder
         */
        public static final class PropertiesBuilder {
            private String writeMode;
            private boolean create;

            private PropertiesBuilder() {
            }

            public PropertiesBuilder writeMode(String writeMode) {
                this.writeMode = writeMode;
                return this;
            }

            public PropertiesBuilder create(boolean create) {
                this.create = create;
                return this;
            }

            public Properties build() {
                return new Properties(this);
            }
        }
    }
}