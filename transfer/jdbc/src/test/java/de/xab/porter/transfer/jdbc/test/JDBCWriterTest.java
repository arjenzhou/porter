package de.xab.porter.transfer.jdbc.test;

import de.xab.porter.api.Column;
import de.xab.porter.api.Relation;
import de.xab.porter.api.Result;
import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.transfer.connector.Connector;
import de.xab.porter.transfer.jdbc.writer.JDBCWriter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.JDBCType;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JDBCWriterTest {
    @Test
    public void valueModeShouldEscapeStringAndNullLiterals() throws Exception {
        TestWriter writer = new TestWriter();
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        Connector<?> connector = mock(Connector.class);
        SinkConnection sinkConnection = ((SinkConnection.Builder) new SinkConnection.Builder().
                connectorType("mock").
                type("mock").
                url("localhost").
                schema("PUBLIC").
                table("users")).
                properties(SinkConnection.Properties.builder().
                        writeMode(SinkConnection.Properties.STATEMENT_VALUES_MODE).
                        build()).
                build();
        Relation relation = new Relation(List.of(column("name"), column("note"), column("age")));
        relation.setData(List.of(List.of("O'Reilly", null, 42)));

        when(connector.getDataConnection()).thenReturn(sinkConnection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getIdentifierQuoteString()).thenReturn("\"");
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeUpdate(anyString())).thenReturn(1);

        writer.setConnector(connector);
        writer.setConnection(connection);
        writer.write(new Result<>(1L, relation));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(statement).executeUpdate(sql.capture());
        assertEquals("INSERT INTO \"PUBLIC\".\"users\" \n"
                + "(\"name\", \"note\", \"age\")VALUES\n"
                + "('O''Reilly', NULL, 42)", sql.getValue());
    }

    private static Column column(String name) {
        Column column = new Column(name);
        column.setColumnType(JDBCType.VARCHAR);
        column.setColumnTypeName("VARCHAR");
        return column;
    }

    private static final class TestWriter extends JDBCWriter {
        private void setConnection(Connection connection) {
            this.connection = connection;
        }
    }
}
