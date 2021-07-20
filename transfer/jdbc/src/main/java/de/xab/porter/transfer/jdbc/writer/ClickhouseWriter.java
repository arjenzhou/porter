package de.xab.porter.transfer.jdbc.writer;

import de.xab.porter.api.Relation;
import de.xab.porter.api.Result;
import de.xab.porter.api.dataconnection.SinkConnection;

import java.sql.Connection;
import java.util.Map;


public class ClickhouseWriter extends JDBCWriter {

    /**
     * using MergeTree engine if enable create table.
     * NOTE: this engine DOES NOT have unique constraints
     */
    @Override
    protected String getConstraints(Map<Short, String> primaryKeyMap) {
        StringBuilder constraintsBuilder = new StringBuilder("\n)\tENGINE = MergeTree()\n");
        if (primaryKeyMap != null && !primaryKeyMap.isEmpty()) {
            String primaryKeys = String.join(", ", primaryKeyMap.values());
            constraintsBuilder.append("\tORDER BY (").append(primaryKeys).append(")\n");
            constraintsBuilder.append("\tPRIMARY KEY (").append(primaryKeys).append(")\n");
        } else {
            constraintsBuilder.append("\tORDER BY tuple()");
        }
        return constraintsBuilder.toString();
    }

    @Override
    protected void writeInDefaultMode(SinkConnection sinkConnection, Connection jdbcConnection, Result<?> data) {
        writeInPrepareBatchMode(jdbcConnection, ((Relation) data.getResult()), sinkConnection.getProperties());
    }
}
