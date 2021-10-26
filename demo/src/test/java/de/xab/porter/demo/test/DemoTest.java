package de.xab.porter.demo.test;

import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.api.dataconnection.SrcConnection;
import de.xab.porter.api.task.Context;
import de.xab.porter.core.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;
import java.util.logging.Logger;

import static de.xab.porter.api.dataconnection.SinkConnection.Properties.PREPARE_BATCH_MODE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test class for demo reader and writer
 */
public class DemoTest {
    private final int rows = 4;
    private final String connectorType = "hikari";
    private final String type = "demo";
    private final String catalog = "porter";
    private final String schema = "PUBLIC";
    private final String table = "mock_table";
    private final String url = String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1", catalog);
    private final int batchSize = 500;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @BeforeEach
    private void createH2Table() {
        String ddl = "CREATE TABLE " + table
                + "(id INTEGER not NULL, "
                + " first VARCHAR(255), "
                + " last VARCHAR(255), "
                + " age INTEGER, "
                + " PRIMARY KEY ( id ))";

        String sql = "INSERT INTO " + table + " VALUES "
                + "(100, 'Zara', 'Ali', 18), "
                + "(101, 'Mahnaz', 'Fatma', 25), "
                + "(102, 'Zaid', 'Khan', 30), "
                + "(103, 'Sumit', 'Mittal', 28)";

        try (Connection connection = DriverManager.getConnection(url, "", "");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(ddl);
            int i = statement.executeUpdate(sql);
            assert i == rows;
        } catch (SQLException exception) {
            logger.severe(exception.getMessage());
        }
    }

    @Test
    public void testNewDataSource() {
        Session session = new Session();
        Context context = new Context();
        SrcConnection srcConn = ((SrcConnection.Builder) new SrcConnection.Builder().
                connectorType(connectorType).
                type(type).
                username("").
                password("").
                catalog(catalog).
                schema(schema).
                table(table)).
                sql("SELECT * FROM `mock_table`").
                properties(SrcConnection.Properties.builder().
                        batchSize(batchSize).
                        split(true).
                        readerNumber(2).
                        splitColumn("id").
                        readTableMeta(true).
                        build()).
                build();

        SinkConnection sinkConnection = ((SinkConnection.Builder) new SinkConnection.Builder().
                connectorType(connectorType).
                type(type).
                username("").
                password("").
                catalog(catalog).
                schema(schema).
                table(table + "_tmp")).
                properties(
                        SinkConnection.Properties.builder().
                                create(true).
                                writeMode(PREPARE_BATCH_MODE).
                                build()).
                build();
        context.setSrcConnection(srcConn);
        context.setSinkConnections(List.of(sinkConnection));
        session.start(context);
        assertEquals(getRows(), rows);
    }

    private int getRows() {
        try (Connection connection = DriverManager.getConnection(url, "", "");
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM \"" + schema + "\".\"" + table + "_tmp" + "\"");
            int columnCount = resultSet.getMetaData().getColumnCount();
            int row = 0;
            while (resultSet.next()) {
                StringBuilder res = new StringBuilder();
                for (int i = 0; i < columnCount; i++) {
                    res.append(resultSet.getString(i + 1)).append(" ");
                }
                logger.info(res.toString());
                row++;
            }
            return row;
        } catch (SQLException exception) {
            logger.severe(exception.getMessage());
        }
        return 0;
    }
}
