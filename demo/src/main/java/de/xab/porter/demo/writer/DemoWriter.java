package de.xab.porter.demo.writer;

import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.transfer.jdbc.writer.JDBCWriter;

public class DemoWriter extends JDBCWriter {
    @Override
    public String getJDBCUrl(DataConnection dataConnection) {
        return "jdbc:h2:~/porter";
    }
}