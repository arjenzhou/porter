package de.xab.porter.api.dataconnection;

public class SrcConnection extends DataConnection {
    private Properties properties = new Properties();

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public static class Properties {
        private String sql;
        private boolean table = true;
        private boolean create;

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public boolean isTable() {
            return table;
        }

        public void setTable(boolean table) {
            this.table = table;
        }

        public boolean isCreate() {
            return create;
        }

        public void setCreate(boolean create) {
            this.create = create;
        }
    }
}
