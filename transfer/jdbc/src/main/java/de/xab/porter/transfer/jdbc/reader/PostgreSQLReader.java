package de.xab.porter.transfer.jdbc.reader;

import java.sql.SQLException;
import java.sql.Statement;

import static de.xab.porter.common.constant.Constant.DEFAULT_BATCH_SIZE;
import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;

public class PostgreSQLReader extends JDBCReader {
    @Override
    protected Statement getStatement() throws SQLException {
        this.connection.setAutoCommit(false);
        Statement statement = this.connection.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
        statement.setFetchSize(DEFAULT_BATCH_SIZE);
        return statement;
    }
}
