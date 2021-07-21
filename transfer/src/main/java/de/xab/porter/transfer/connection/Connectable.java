package de.xab.porter.transfer.connection;

import de.xab.porter.api.dataconnection.DataConnection;

/**
 * provide connection ability
 */
public interface Connectable {
    /**
     * connect to data source
     *
     * @param dataConnection connection message of data source
     * @param dataSource     represent a database or datasource instance
     * @return connection to a data source
     */
    Object connect(DataConnection dataConnection, Object dataSource);

    /**
     * get the data source instance
     */
    Object getDataSource(DataConnection dataConnection);

    /**
     * close connection to data source
     */
    void close(Object connection, Object dataSource);

    /**
     * connection is closed or not
     *
     * @param connection connection to data source
     */
    default boolean closed(Object connection) {
        return true;
    }

    String getType();

    void setType(String type);
}
