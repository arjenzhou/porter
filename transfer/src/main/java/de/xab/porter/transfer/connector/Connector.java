package de.xab.porter.transfer.connector;

import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.transfer.exception.ConnectionException;

/**
 * Connector is used to connect to any data source, usually delegated by {@link Connectable}
 *
 * @param <T> the Connection object returned by client
 */
public interface Connector<T> {
    T connect(Object... args) throws ConnectionException;

    /**
     * close connection to data source
     */
    void close();

    /**
     * connection is closed or not
     */
    default boolean closed() {
        return true;
    }

    /**
     * @return the connection properties
     */
    DataConnection getDataConnection();
}
