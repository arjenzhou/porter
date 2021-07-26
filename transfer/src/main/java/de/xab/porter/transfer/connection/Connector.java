package de.xab.porter.transfer.connection;

import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.transfer.exception.ConnectionException;

/**
 * provide connection ability
 */
public interface Connector {
    /**
     * connect to data source
     *
     * @param dataConnection connection message of data source
     */
    void connect(DataConnection dataConnection) throws ConnectionException;

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

    String getType();

    void setType(String type);
}
