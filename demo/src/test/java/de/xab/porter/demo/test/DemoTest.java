package de.xab.porter.demo.test;

import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.api.dataconnection.SrcConnection;
import de.xab.porter.api.task.Context;
import de.xab.porter.core.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.xab.porter.api.dataconnection.SinkConnection.Properties.PREPARE_BATCH_MODE;

/**
 * test class for demo reader and writer
 */
public class DemoTest {
    private final int rows = 4;
    private final String url = "jdbc:h2:~/porter";
    private final String type = "demo";
    private final String schema = "PUBLIC";
    private final String table = "mock_table";
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @BeforeEach
    private void createH2Table() {
        final String drop = "DROP TABLE IF EXISTS " + table;

        final String ddl = "CREATE TABLE " + table
                + "(id INTEGER not NULL, "
                + " first VARCHAR(255), "
                + " last VARCHAR(255), "
                + " age INTEGER, "
                + " PRIMARY KEY ( id ))";

        final String sql = "INSERT INTO " + table + " VALUES "
                + "(100, 'Zara', 'Ali', 18), "
                + "(101, 'Mahnaz', 'Fatma', 25), "
                + "(102, 'Zaid', 'Khan', 30), "
                + "(103, 'Sumit', 'Mittal', 28)";

        try (Connection connection = DriverManager.getConnection(url, "", "");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(drop);
            statement.executeUpdate(ddl);
            final int i = statement.executeUpdate(sql);
            assert i == rows;
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, exception.getMessage());
        }
    }

    @Test
    public void testNewReader() {
        Session session = new Session();
        Context context = new Context();

        SrcConnection srcConn = ((SrcConnection.Builder) new SrcConnection.Builder().
                type(type).
                username("").
                password("").
                schema(schema).
                table(table)).
                build();

        SinkConnection sinkConnection = ((SinkConnection.Builder) new SinkConnection.Builder().
                type(type).
                username("").
                password("").
                schema(schema).
                table(table + "_tmp")).
                properties(
                        SinkConnection.Properties.builder().
                                drop(true).
                                create(true).
                                allColumns(false).
                                writeMode(PREPARE_BATCH_MODE).
                                build()).
                build();

        context.setSrcConnection(srcConn);
        context.setSinkConnections(List.of(sinkConnection));
        session.start(context);

        try (Connection connection = DriverManager.getConnection(url, "", "");
             Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM \"PUBLIC\".\"mock_table_tmp\"");
            final int columnCount = resultSet.getMetaData().getColumnCount();
            int row = 0;
            while (resultSet.next()) {
                final StringBuilder res = new StringBuilder();
                for (int i = 0; i < columnCount; i++) {
                    res.append(resultSet.getString(i + 1)).append(" ");
                }
                logger.log(Level.INFO, res.toString());
                row++;
            }
            assert row == rows;
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, exception.getMessage());
        }
    }
}
