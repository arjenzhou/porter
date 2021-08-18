package de.xab.porter.transfer.jdbc.reader;

import com.zaxxer.hikari.HikariDataSource;
import de.xab.porter.api.Column;
import de.xab.porter.api.Relation;
import de.xab.porter.api.Result;
import de.xab.porter.api.dataconnection.SrcConnection;
import de.xab.porter.api.exception.PorterException;
import de.xab.porter.common.util.Loggers;
import de.xab.porter.transfer.jdbc.connector.JDBCConnector;
import de.xab.porter.transfer.reader.AbstractReader;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.xab.porter.common.constant.Constant.DEFAULT_BATCH_SIZE;
import static de.xab.porter.common.enums.SequenceEnum.*;
import static de.xab.porter.common.util.Strings.notNullOrBlank;
import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;

/**
 * common JDBC reader
 */
public class JDBCReader extends AbstractReader implements JDBCConnector {
    protected HikariDataSource datasource;
    protected Connection connection;
    private final Logger logger = Loggers.getLogger(this.getClass());

    @Override
    public void doRead(Map<String, Column> columnMap) {
        SrcConnection.Properties properties = this.srcConnection.getProperties();
        Statement statement = null;
        ResultSet resultSet = null;
        Instant start = Instant.now();
        long batch = 0L;
        try {
            if (!connection.isReadOnly()) {
                connection.setReadOnly(true);
            }
            statement = getStatement();
            resultSet = statement.executeQuery(properties.getSql());
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            fillResultSetMeta(columnMap, metaData, columnCount);
            List<Column> meta = new ArrayList<>(columnMap.values());
            long seq = 0L;
            Relation relation = new Relation(meta);
            while (resultSet.next()) {
                batch++;
                List<Object> row = new ArrayList<>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    row.add(resultSet.getString(i));
                }
                relation.getData().add(row);
                if (batch % DEFAULT_BATCH_SIZE == 0) {
                    logger.log(Level.INFO, String.format("read %d rows from %s %s",
                            batch, this.srcConnection.getType(), this.srcConnection.getUrl()));
                    this.pushToChannel(new Result<>(seq++, relation));
                    relation = new Relation(meta);
                }
            }
            //last data container not fill up with batch size
            pushLastBatch(meta, seq, relation);
        } catch (SQLException exception) {
            throw new PorterException("read data from JDBC connection failed", exception);
        } finally {
            Instant end = Instant.now();
            long seconds = Duration.between(start, end).toSeconds();
            logger.log(Level.INFO, String.format(
                    "read completed. %s rows have been read, cost %s second(s)", batch, seconds));
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException exception) {
                logger.log(Level.WARNING, "close JDBC connection failed");
            }
        }
    }

    @Override
    public Map<String, Column> getTableMetaData() {
        SrcConnection srcConnection = this.srcConnection;
        Connection connection = this.connection;
        //keep insertion order
        Map<String, Column> columnMap = new LinkedHashMap<>();
        try (ResultSet columns = connection.getMetaData().getColumns(srcConnection.getCatalog(),
                srcConnection.getSchema(), srcConnection.getTable(), null);
             ResultSet primaryKeys = connection.getMetaData().getPrimaryKeys(srcConnection.getCatalog(),
                     srcConnection.getSchema(), srcConnection.getTable());
             ResultSet indexInfo = connection.getMetaData().getIndexInfo(srcConnection.getCatalog(),
                     srcConnection.getSchema(), srcConnection.getTable(), false, false)) {

            while (columns.next()) {
                String remarks = columns.getString("REMARKS");
                String nullable = columns.getString("IS_NULLABLE");
                String columnName = columns.getString("COLUMN_NAME");
                Column column = new Column();
                column.setName(columnName);
                column.setComment(remarks);
                column.setNullable(nullable);
                columnMap.putIfAbsent(columnName, column);
            }

            while (primaryKeys.next()) {
                String columnName = primaryKeys.getString("COLUMN_NAME");
                String pkName = primaryKeys.getString("PK_NAME");
                short keySeq = primaryKeys.getShort("KEY_SEQ");
                columnMap.computeIfPresent(columnName, (ignored, column) -> {
                    column.setPrimaryKey(notNullOrBlank(pkName));
                    column.setIndexName(pkName);
                    column.setPrimaryKeySeq(keySeq);
                    return column;
                });
            }

            while (indexInfo.next()) {
                String indexName = indexInfo.getString("INDEX_NAME");
                String columnName = indexInfo.getString("COLUMN_NAME");
                columnMap.computeIfPresent(columnName, (ignored, column) -> {
                    column.setIndexName(indexName);
                    return column;
                });
            }
        } catch (SQLException exception) {
            throw new PorterException("read source meta data from JDBC connection failed", exception);
        }
        return columnMap;
    }

    /**
     * get JDBC statement, and set properties for it
     */
    protected Statement getStatement() throws SQLException {
        Statement statement = this.connection.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
        statement.setFetchSize(DEFAULT_BATCH_SIZE);
        return statement;
    }

    /**
     * merge meta between read meta and read data
     */
    private void fillResultSetMeta(Map<String, Column> columnMap,
                                   ResultSetMetaData metaData, int columnCount) throws SQLException {
        for (int i = 1; i <= columnCount; i++) {
            String name = metaData.getColumnName(i);
            Column column = columnMap.compute(name, (key, oldValue) ->
                    Objects.requireNonNullElseGet(oldValue, () -> new Column(name)));
            if (this.srcConnection.getProperties().isCreate()) {
                int displaySize = metaData.getColumnDisplaySize(i);
                boolean signed = metaData.isSigned(i);
                int precision = metaData.getPrecision(i);
                int scale = metaData.getScale(i);
                int columnType = metaData.getColumnType(i);
                String columnTypeName = metaData.getColumnTypeName(i);
                String className = metaData.getColumnClassName(i);
                column.setClassName(className);
                column.setDisplaySize(displaySize);
                column.setSigned(signed);
                column.setPrecision(precision);
                column.setScale(scale);
                column.setColumnType(JDBCType.valueOf(columnType));
                column.setColumnTypeName(columnTypeName);
                columnMap.put(name, column);
            }
        }
    }

    private void pushLastBatch(List<Column> meta, long seq, Relation relation) {
        if (!relation.getData().isEmpty()) {
            //only one batch
            seq = seq
                    == FIRST.getSequenceNum()
                    ? FIRST_AND_LAST.getSequenceNum()
                    : LAST_NOT_EMPTY.getSequenceNum();
        } else {
            seq = LAST_IS_EMPTY.getSequenceNum();
            relation = new Relation(meta);
        }
        logger.log(Level.INFO, String.format("read last %d scrap(s) from %s %s",
                relation.getData().size(), this.srcConnection.getType(), this.srcConnection.getUrl()));
        this.pushToChannel(new Result<>(seq, relation));
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public void setDatasource(HikariDataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    public HikariDataSource getDatasource() {
        return datasource;
    }
}