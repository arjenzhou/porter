package de.xab.porter.demo;

import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.api.dataconnection.SrcConnection;
import de.xab.porter.api.task.Context;
import de.xab.porter.core.Session;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.xab.porter.api.dataconnection.SinkConnection.Properties.PREPARE_BATCH_MODE;

public class DemoTest {
    Logger logger = Logger.getLogger(this.getClass().getName());

    private void createH2Table(String table) {
        String drop = "DROP TABLE IF EXISTS " + table;

        String ddl = "CREATE TABLE " + table +
                "(id INTEGER not NULL, " +
                " first VARCHAR(255), " +
                " last VARCHAR(255), " +
                " age INTEGER, " +
                " PRIMARY KEY ( id ))";

        String sql = "INSERT INTO " + table + " VALUES " +
                "(100, 'Zara', 'Ali', 18), " +
                "(101, 'Mahnaz', 'Fatma', 25), " +
                "(102, 'Zaid', 'Khan', 30), " +
                "(103, 'Sumit', 'Mittal', 28)";

        try (Connection connection = DriverManager.getConnection("jdbc:h2:~/porter", "", "");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(drop);
            statement.executeUpdate(ddl);
            int i = statement.executeUpdate(sql);
            assert i == 4;
        } catch (SQLException ignored) {
        }
    }

    @Test
    public void testNewReader() {
        String table = "mock_table";
        createH2Table(table);
        Session session = new Session();
        Context context = new Context();

        SrcConnection srcConn = ((SrcConnection.Builder) new SrcConnection.Builder()
                .type("demo")
                .username("")
                .password("")
                .schema("PUBLIC")
                .table(table))
                .build();

        SinkConnection sinkConnection = ((SinkConnection.Builder) new SinkConnection.Builder()
                .type("demo")
                .username("")
                .password("")
                .schema("PUBLIC")
                .table(table + "_tmp"))
                .properties(
                        SinkConnection.Properties.builder()
                                .drop(true)
                                .create(true)
                                .writeMode(PREPARE_BATCH_MODE)
                                .build())
                .build();

        context.setSrcConnection(srcConn);
        context.setSinkConnections(List.of(sinkConnection));
        session.start(context);

        try (Connection connection = DriverManager.getConnection("jdbc:h2:~/porter", "", "");
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM \"PUBLIC\".\"mock_table_tmp\"");
            int columnCount = resultSet.getMetaData().getColumnCount();
            while (resultSet.next()) {
                StringBuilder res = new StringBuilder();
                for (int i = 0; i < columnCount; i++) {
                    res.append(resultSet.getString(i + 1)).append(" ");
                }
                logger.log(Level.INFO, res.toString());
            }
            assert columnCount == 4;
        } catch (SQLException ignored) {
        }
    }
}
