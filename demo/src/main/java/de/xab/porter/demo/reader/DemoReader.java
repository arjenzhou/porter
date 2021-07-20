package de.xab.porter.demo.reader;

import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.transfer.jdbc.reader.JDBCReader;

public class DemoReader extends JDBCReader {
    @Override
    public String getJDBCUrl(DataConnection dataConnection) {
        return "jdbc:h2:~/porter";
    }
}