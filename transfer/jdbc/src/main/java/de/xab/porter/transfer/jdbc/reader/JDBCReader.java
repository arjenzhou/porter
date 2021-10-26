package de.xab.porter.transfer.jdbc.reader;

import de.xab.porter.api.Column;
import de.xab.porter.api.Relation;
import de.xab.porter.api.Result;
import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.api.dataconnection.SrcConnection;
import de.xab.porter.api.exception.PorterException;
import de.xab.porter.common.util.Loggers;
import de.xab.porter.transfer.exception.ConnectionException;
import de.xab.porter.transfer.jdbc.connector.JDBCConnector;
import de.xab.porter.transfer.reader.AbstractReader;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import static de.xab.porter.common.constant.Constant.DEFAULT_BATCH_SIZE;
import static de.xab.porter.common.enums.SequenceEnum.*;
import static de.xab.porter.common.util.Strings.notNullOrBlank;
import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;

/**
 * common JDBC reader
 */
public class JDBCReader extends AbstractReader<Connection> implements JDBCConnector {
    private final Logger logger = Loggers.getLogger(this.getClass());

    @Override
    public long doRead(Map<String, Column> columnMap, String sql) {
        SrcConnection srcConnection = (SrcConnection) getConnector().getDataConnection();
        int batchSize = srcConnection.getProperties().getBatchSize();
        batchSize = batchSize <= 0 ? DEFAULT_BATCH_SIZE : batchSize;
        Statement statement = null;
        ResultSet resultSet = null;
        Instant start = Instant.now();
        long batch = 0L;
        try {
            connection.setReadOnly(true);
            statement = getStatement(batchSize);
            resultSet = statement.executeQuery(sql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            fillResultSetMeta(columnMap, metaData, columnCount);
            List<Column> meta = new ArrayList<>(columnMap.values());
            long seq = 0L;
            Relation relation = new Relation(meta);
            List<List<?>> rows = relation.getData();
            while (resultSet.next()) {
                batch++;
                List<Object> row = new ArrayList<>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    row.add(resultSet.getString(i));
                }
                rows.add(row);
                if (batch % batchSize == 0) {
                    this.pushToChannel(new Result<>(++seq, relation));
                    relation = new Relation(meta);
                    rows = relation.getData();
                }
            }
            //last data container not fill up with batch size
            pushLastBatch(meta, ++seq, relation);
        } catch (SQLException exception) {
            throw new PorterException("read data from JDBC connection failed", exception);
        } finally {
            Instant end = Instant.now();
            long seconds = Duration.between(start, end).toSeconds();
            logger.info(String.format(
                    "%s rows have been read, cost %s second(s)", batch, seconds));
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException exception) {
                logger.warning("close JDBC connection failed");
            }
        }
        return batch;
    }

    @Override
    public Map<String, Column> getTableMetaData() {
        SrcConnection srcConnection = (SrcConnection) getConnector().getDataConnection();
        Connection connection = this.connection;
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

    @Override
    protected String getIdentifierQuote() {
        try {
            return this.connection.getMetaData().getIdentifierQuoteString();
        } catch (SQLException e) {
            throw new PorterException("read quote from JDBC meta data failed", e);
        }
    }

    @Override
    public List<String> split() {
        SrcConnection srcConnection = (SrcConnection) getConnector().getDataConnection();
        SrcConnection.Properties properties = srcConnection.getProperties();
        String sql = srcConnection.getSql();
        int max = 0;
        int min = 0;
        String column;
        try (Statement statement = getStatement(1)) {
            String quote = getIdentifierQuote();
            column = String.format("%s%s%s", quote, properties.getSplitColumn(), quote);
            String maxSql = String.format("SELECT MAX(%s) FROM (%s) AS MAX_TEMP WHERE 1=1", column, sql);
            String minSql = String.format("SELECT MIN(%s) FROM (%s) AS MIN_TEMP WHERE 1=1", column, sql);
            ResultSet maxResultSet = statement.executeQuery(maxSql);
            if (maxResultSet.next()) {
                max = maxResultSet.getInt(1);
            }
            ResultSet minResultSet = statement.executeQuery(minSql);
            if (minResultSet.next()) {
                min = minResultSet.getInt(1);
            }
        } catch (SQLException exception) {
            logger.warning("split failed, reset trunk num to 1. " + exception.getMessage());
            return List.of(sql);
        }
        int range = max - min + 1;
        if (range == 1) {
            logger.info("max equals min, reset trunk num to 1");
            return List.of(sql);
        }
        int trunkNumber = properties.getReaderNumber();
        int step = range / trunkNumber;
        step = range % trunkNumber == 0 ? step : step + 1;
        int start = min;
        logger.info(String.format("%s starts at %s, ends at %s. Step is %s",
                properties.getSplitColumn(), min, max, step));
        // max 17, min 2, trunk 5
        // range 16, step 4
        // [2, 6], [7, 11], [12, 16], [17, 17]
        List<String> sequels = new ArrayList<>();
        for (int i = 0; i < trunkNumber; i++) {
            int left = start;
            int right = left + step;
            if (i == trunkNumber - 1) {
                right = max;
            }
            start = right + 1;
            String splitSql = String.format("SELECT * FROM (%s) as TEMP WHERE %s >= %s AND %s <= %s",
                    sql, column, left, column, right);
            logger.info(splitSql);
            sequels.add(splitSql);
        }
        logger.info("trunk size is " + sequels.size());
        return sequels;
    }

    /**
     * get JDBC statement, and set properties for it
     */
    protected Statement getStatement(int batchSize) throws SQLException {
        Statement statement = this.connection.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
        statement.setFetchSize(batchSize);
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

    private void pushLastBatch(List<Column> meta, long seq, Relation relation) {
        SrcConnection srcConnection = (SrcConnection) getConnector().getDataConnection();
        if (!relation.getData().isEmpty()) {
            //only one batch
            seq = seq
                    == FIRST.getSequenceNum()
                    ? FIRST_AND_LAST.getSequenceNum()
                    : LAST_NOT_EMPTY.getSequenceNum();
            logger.info(String.format("read last %d scrap(s) from %s %s",
                    relation.getData().size(), srcConnection.getType(), srcConnection.getUrl()));
        } else {
            seq = LAST_IS_EMPTY.getSequenceNum();
            relation = new Relation(meta);
        }
        this.pushToChannel(new Result<>(seq, relation));
    }

    @Override
    public Connection connect(DataConnection dataConnection) throws ConnectionException {
        SrcConnection srcConnection = (SrcConnection) dataConnection;
        this.connection = (Connection) getConnector().connect(srcConnection, getJDBCUrl(srcConnection));
        return this.connection;
    }
}