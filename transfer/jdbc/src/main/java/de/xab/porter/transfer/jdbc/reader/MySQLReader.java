package de.xab.porter.transfer.jdbc.reader;

import de.xab.porter.api.dataconnection.DataConnection;

public class MySQLReader extends JDBCReader {
    @Override
    public String getJDBCUrl(DataConnection dataConnection) {
        String schema = dataConnection.getCatalog() == null ? dataConnection.getSchema() : dataConnection.getCatalog();
        return String.format("jdbc:%s://%s/%s?useCursorFetch=true", getType(), dataConnection.getUrl(), schema);
    }
}