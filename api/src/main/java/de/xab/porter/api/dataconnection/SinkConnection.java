package de.xab.porter.api.dataconnection;

public class SinkConnection extends DataConnection {
    private Properties properties = new Properties();

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public static class Properties {
        public static final String PREPARE_BATCH_MODE = "PREPARE_BATCH";
        public static final String STATEMENT_BATCH_MODE = "STATEMENT_BATCH";
        public static final String STATEMENT_VALUES_MODE = "STATEMENT_VALUES";

        private String writeMode = "";
        private boolean allColumns = true;
        private String quote;
        private String tableIdentifier;
        private boolean create = false;
        private boolean drop = false;

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

        public void setWriteMode(String writeMode) {
            this.writeMode = writeMode;
        }

        public boolean isAllColumns() {
            return allColumns;
        }

        public void setAllColumns(boolean allColumns) {
            this.allColumns = allColumns;
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

        public void setCreate(boolean create) {
            this.create = create;
        }

        public boolean isDrop() {
            return drop;
        }

        public void setDrop(boolean drop) {
            this.drop = drop;
        }
    }
}