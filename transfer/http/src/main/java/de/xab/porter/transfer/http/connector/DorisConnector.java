package de.xab.porter.transfer.http.connector;

import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.transfer.connector.Connector;
import okhttp3.Credentials;

import java.util.HashMap;
import java.util.Map;

/**
 * connect to Doris via HTTP
 */
public class DorisConnector implements Connector<Void> {
    private DataConnection dataConnection;
    private Map<String, String> header;

    /**
     * do not hold a connection
     */
    @Override
    public Void connect(Object... args) {
        this.dataConnection = (DataConnection) args[0];
        String basic = Credentials.basic(dataConnection.getUsername(), dataConnection.getPassword());
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", basic);
        header.put("format", "json");
        this.header = header;
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public DataConnection getDataConnection() {
        return dataConnection;
    }

    public Map<String, String> getHeader() {
        return header;
    }
}
