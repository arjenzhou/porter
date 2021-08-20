package de.xab.porter.transfer.jdbc.connector;

import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.transfer.connector.Connectable;

import java.sql.Connection;

public interface JDBCConnector extends Connectable<Connection> {
    default String getJDBCUrl(DataConnection dataConnection) {
        String schema = dataConnection.getCatalog() == null ? dataConnection.getSchema() : dataConnection.getCatalog();
        return String.format("jdbc:%s://%s/%s", dataConnection.getType(), dataConnection.getUrl(), schema);
    }
}
