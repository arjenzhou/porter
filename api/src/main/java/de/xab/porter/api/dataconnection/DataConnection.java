package de.xab.porter.api.dataconnection;

/**
 * super class of any input data structure
 * {@link SrcConnection} {@link SinkConnection}
 */
public class DataConnection {
    private String type;
    private String url;
    private String username;
    private String password;
    private String catalog;
    private String schema;
    private String table;

    public DataConnection() {
    }

    protected DataConnection(Builder<?> builder) {
        this.type = builder.type;
        this.url = builder.url;
        this.username = builder.username;
        this.password = builder.password;
        this.catalog = builder.catalog;
        this.schema = builder.schema;
        this.table = builder.table;
    }

    //setter and getter
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    //builder
    public abstract static class Builder<T extends DataConnection> {
        private String type;
        private String url;
        private String username;
        private String password;
        private String catalog;
        private String schema;
        private String table;

        public Builder<T> type(String type) {
            this.type = type;
            return this;
        }

        public Builder<T> url(String url) {
            this.url = url;
            return this;
        }

        public Builder<T> username(String username) {
            this.username = username;
            return this;
        }

        public Builder<T> password(String password) {
            this.password = password;
            return this;
        }

        public Builder<T> catalog(String catalog) {
            this.catalog = catalog;
            return this;
        }

        public Builder<T> schema(String schema) {
            this.schema = schema;
            return this;
        }

        public Builder<T> table(String table) {
            this.table = table;
            return this;
        }

        public abstract T build();
    }
}
