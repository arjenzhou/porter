package de.xab.porter.transfer.connector;

import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.transfer.exception.ConnectionException;

/**
 * provide connection ability
 */
public interface Connectable<T> {
    /**
     * connect to data source
     *
     * @param dataConnection connection message of data source
     */
    T connect(DataConnection dataConnection) throws ConnectionException;

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

    void setConnector(Connector<?> connector);

    Connector<?> getConnector();
}
