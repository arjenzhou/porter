package de.xab.porter.transfer.jdbc.writer;

import de.xab.porter.api.dataconnection.DataConnection;

public class MySQLWriter extends JDBCWriter {

    @Override
    public String getJDBCUrl(DataConnection dataConnection) {
        String schema = dataConnection.getCatalog() == null ? dataConnection.getSchema() : dataConnection.getCatalog();
        return String.format("jdbc:%s://%s/%s?allowLoadLocalInfile=true", getType(), dataConnection.getUrl(), schema);
    }
}
