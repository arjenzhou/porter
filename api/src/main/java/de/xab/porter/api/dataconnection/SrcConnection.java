package de.xab.porter.api.dataconnection;

/**
 * connection message of source datasource,
 * inner class {@link Properties} describe the behavior how data was read from source table.
 */
public final class SrcConnection extends DataConnection {
    private Properties properties = new Properties();

    //constructors
    public SrcConnection() {
        super();
    }

    protected SrcConnection(Builder builder) {
        super(builder);
    }

    //getter and setter
    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * builder
     */
    public static final class Builder extends AbstractBuilder<SrcConnection> {
        private Properties properties;

        public Builder properties(Properties properties) {
            this.properties = properties;
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
        private String sql;
        private boolean table = true;
        private boolean create;

        private Properties(Builder builder) {
            sql = builder.sql;
            table = builder.table;
            create = builder.create;
        }

        public Properties() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public boolean isTable() {
            return table;
        }

        public boolean isCreate() {
            return create;
        }

        public void setCreate(boolean create) {
            this.create = create;
        }

        /**
         * properties builder
         */
        public static final class Builder {
            private String sql;
            private boolean table;
            private boolean create;

            private Builder() {
            }

            public Builder sql(String sql) {
                this.sql = sql;
                return this;
            }

            public Builder table(boolean table) {
                this.table = table;
                return this;
            }

            public Builder create(boolean create) {
                this.create = create;
                return this;
            }

            public Properties build() {
                return new Properties(this);
            }
        }
    }
}
