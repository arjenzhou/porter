package de.xab.porter.transfer.http.connector;

import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.transfer.connector.Connector;

/**
 * connect to data source via HTTP
 */
public class HttpConnector implements Connector<Void> {
    private DataConnection dataConnection;

    /**
     * do not hold a connection
     */
    @Override
    public Void connect(Object... args) {
        this.dataConnection = (DataConnection) args[0];
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public DataConnection getDataConnection() {
        return dataConnection;
    }
}
