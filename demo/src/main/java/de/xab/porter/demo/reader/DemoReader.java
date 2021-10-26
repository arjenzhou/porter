package de.xab.porter.demo.reader;

import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.transfer.jdbc.reader.JDBCReader;

/**
 * demo reader with h2 database
 */
public class DemoReader extends JDBCReader {
    @Override
    public String getJDBCUrl(DataConnection dataConnection) {
        return String.format("jdbc:h2:mem:%s", dataConnection.getCatalog());
    }

    @Override
    protected String getIdentifierQuote() {
        return "`";
    }
}