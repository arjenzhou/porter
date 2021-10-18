package de.xab.porter.transfer.jdbc.writer;

import de.xab.porter.api.Relation;
import de.xab.porter.api.Result;
import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.api.exception.NotSupportedException;

public class DorisWriter extends JDBCWriter {
    /**
     * Doris uses MySQL JDBC protocol
     */
    @Override
    public String getJDBCUrl(DataConnection dataConnection) {
        String schema = dataConnection.getCatalog() == null ? dataConnection.getSchema() : dataConnection.getCatalog();
        return String.format("jdbc:mysql://%s/%s", dataConnection.getUrl(), schema);
    }

    /**
     * In case of Doris has a complex type of table with different DDL specification,
     * porter will not support create table for it.
     */
    @Override
    public void createTable(Result<?> data) {
        throw new NotSupportedException("do not support creating table");
    }

    /**
     * see {@link DorisWriter#createTable}
     */
    @Override
    public void dropTable() {
        throw new NotSupportedException("do not support dropping table");
    }

    /**
     * Doris (0.14 for now) has an extremely slow speed of inserting with batch mode.
     */
    @Override
    protected void writeInPrepareBatchMode(Relation relation) {
        throw new NotSupportedException("do not support prepared batch mode");
    }

    /**
     * see {@link DorisWriter#writeInPrepareBatchMode}
     */
    @Override
    protected void writeInStatementBatchMode(Relation relation) {
        throw new NotSupportedException("do not support statement batch mode");
    }
}
