package de.xab.porter.transfer.jdbc.test;

import de.xab.porter.api.dataconnection.SrcConnection;
import de.xab.porter.transfer.connector.Connector;
import de.xab.porter.transfer.jdbc.reader.JDBCReader;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JDBCReaderTest {
    @Test
    public void splitShouldCapRangesAndSkipEmptyPartitions() throws Exception {
        TestReader reader = new TestReader();
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet maxResultSet = mock(ResultSet.class);
        ResultSet minResultSet = mock(ResultSet.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        Connector<?> connector = mock(Connector.class);
        SrcConnection srcConnection = ((SrcConnection.Builder) new SrcConnection.Builder().
                connectorType("mock").
                type("mock").
                url("localhost")).
                sql("SELECT * FROM users").
                properties(SrcConnection.Properties.builder().
                        split(true).
                        splitColumn("id").
                        readerNumber(5).
                        build()).
                build();

        when(connector.getDataConnection()).thenReturn(srcConnection);
        when(connection.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY)).thenReturn(statement);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getIdentifierQuoteString()).thenReturn("`");
        when(statement.executeQuery("SELECT MAX(`id`) FROM (SELECT * FROM users) AS MAX_TEMP WHERE 1=1"))
                .thenReturn(maxResultSet);
        when(statement.executeQuery("SELECT MIN(`id`) FROM (SELECT * FROM users) AS MIN_TEMP WHERE 1=1"))
                .thenReturn(minResultSet);
        when(maxResultSet.next()).thenReturn(true);
        when(maxResultSet.getInt(1)).thenReturn(17);
        when(minResultSet.next()).thenReturn(true);
        when(minResultSet.getInt(1)).thenReturn(2);

        reader.setConnector(connector);
        reader.setConnection(connection);

        assertEquals(List.of(
                "SELECT * FROM (SELECT * FROM users) as TEMP WHERE `id` >= 2 AND `id` <= 5",
                "SELECT * FROM (SELECT * FROM users) as TEMP WHERE `id` >= 6 AND `id` <= 9",
                "SELECT * FROM (SELECT * FROM users) as TEMP WHERE `id` >= 10 AND `id` <= 13",
                "SELECT * FROM (SELECT * FROM users) as TEMP WHERE `id` >= 14 AND `id` <= 17"
        ), reader.split());
    }

    private static final class TestReader extends JDBCReader {
        private void setConnection(Connection connection) {
            this.connection = connection;
        }
    }
}
