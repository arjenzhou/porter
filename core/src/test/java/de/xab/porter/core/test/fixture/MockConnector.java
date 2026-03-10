package de.xab.porter.core.test.fixture;

import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.transfer.connector.Connector;
import de.xab.porter.transfer.exception.ConnectionException;

public class MockConnector implements Connector<Object> {
    private DataConnection dataConnection;

    @Override
    public Object connect(Object... args) throws ConnectionException {
        this.dataConnection = (DataConnection) args[0];
        if ("fail".equals(this.dataConnection.getUrl())) {
            throw new ConnectionException("mock connection failed");
        }
        return new Object();
    }

    @Override
    public void close() {
    }

    @Override
    public DataConnection getDataConnection() {
        return dataConnection;
    }
}
