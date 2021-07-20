package de.xab.porter.transfer.datasource;

import de.xab.porter.api.dataconnection.DataConnection;

public interface DataSource {
    Object connect(DataConnection dataConnection, Object dataSource);

    Object getDataSource(DataConnection dataConnection);

    void close(Object connection, Object dataSource);

    default boolean closed(Object connection) {
        return true;
    }

    String getType();

    void setType(String type);
}
