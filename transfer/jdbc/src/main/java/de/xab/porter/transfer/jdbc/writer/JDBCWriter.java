package de.xab.porter.transfer.jdbc.writer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import de.xab.porter.api.Column;
import de.xab.porter.api.Relation;
import de.xab.porter.api.Result;
import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.api.exception.PorterException;
import de.xab.porter.common.util.Jsons;
import de.xab.porter.common.util.Loggers;
import de.xab.porter.transfer.exception.ConnectionException;
import de.xab.porter.transfer.writer.AbstractWriter;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static de.xab.porter.api.dataconnection.SinkConnection.Properties.*;
import static de.xab.porter.common.util.Strings.notNullOrBlank;

/**
 * common JDBC writer
 */
public class JDBCWriter extends AbstractWriter {
    protected Connection connection;
    protected HikariDataSource dataSource;
    private final Logger logger = Loggers.getLogger(this.getClass());

    @Override
    public void createTable(Result<?> data) {
        SinkConnection.Properties properties = this.sinkConnection.getProperties();
        String tableIdentifier = getTableIdentifier();
        String quote = properties.getQuote();
        List<Column> meta = ((Relation) data.getResult()).getMeta();
        logger.log(Level.FINE, String.format("meta of table %s is: \n%s", tableIdentifier, Jsons.toJson(meta)));
        String ddl = getCreateDDL(tableIdentifier, quote, meta);
        logger.log(Level.INFO, String.format("create table %s: \n\n%s\n", tableIdentifier, ddl));
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(ddl);
        } catch (SQLException e) {
            logger.log(Level.INFO, String.format("table %s create failed", tableIdentifier));
            throw new PorterException("create table failed", e);
        }
    }

    @Override
    protected void doWrite(Result<?> data) {
        Relation relation = (Relation) data.getResult();
        SinkConnection.Properties properties = sinkConnection.getProperties();
        switch (properties.getWriteMode()) {
            case PREPARE_BATCH_MODE:
                writeInPrepareBatchMode(relation);
                break;
            case STATEMENT_BATCH_MODE:
                writeInStatementBatchMode(relation);
                break;
            case STATEMENT_VALUES_MODE:
                writeInValueMode(relation);
                break;
            default:
                writeInDefaultMode(data);
        }
    }

    /**
     * write data with one insert SQL and multi rows
     */
    protected void writeInValueMode(Relation relation) {
        SinkConnection.Properties properties = this.sinkConnection.getProperties();
        String tableIdentifier = properties.getTableIdentifier();
        StringBuilder sqlBuilder =
                new StringBuilder(String.format("INSERT INTO %s \n", tableIdentifier));
        if (!properties.isAllColumns()) {
            sqlBuilder.append("(").append(relation.getMeta().stream().
                    map(column -> getColumnIdentifier(column.getName(), properties.getQuote())).
                    collect(Collectors.joining(", "))).append(")");
        }
        sqlBuilder.append("VALUES\n");
        sqlBuilder.append(relation.getData().stream().
                map(row -> "(" + row.stream().
                        map(Object::toString).
                        collect(Collectors.joining(", ")) + ")").
                collect(Collectors.joining(", \n")));
        try (Statement stmt = this.connection.createStatement()) {
            //DO NOT DEPEND ON THIS
            //returned by JDBC client, may not accurate
            int rowCount = stmt.executeUpdate(sqlBuilder.toString());
            logger.log(Level.INFO, String.format("wrote %d rows to table %s", rowCount, tableIdentifier));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, sqlBuilder.toString());
            throw new PorterException("write data failed", e);
        }
    }

    /**
     * write data in batch
     */
    protected void writeInStatementBatchMode(Relation relation) {
        SinkConnection.Properties properties = this.sinkConnection.getProperties();
        String tableIdentifier = properties.getTableIdentifier();
        StringBuilder sqlBuilder =
                new StringBuilder(String.format("INSERT INTO %s ", tableIdentifier));
        if (!properties.isAllColumns()) {
            sqlBuilder.append("(").append(relation.getMeta().stream().
                    map(column -> getColumnIdentifier(column.getName(), properties.getQuote())).
                    collect(Collectors.joining(", "))).append(")");
        }
        String prefix = sqlBuilder.append("VALUES(").toString();
        try (Statement statement = this.connection.createStatement()) {
            for (List<?> row : relation.getData()) {
                String insert = prefix + row.stream().
                        map(Object::toString).
                        collect(Collectors.joining(", ")) + ")";
                statement.addBatch(insert);
            }
            int[] result = statement.executeBatch();
            long rowCount = Arrays.stream(result).summaryStatistics().getSum();
            logger.log(Level.INFO, String.format("wrote %d rows to table %s", rowCount, tableIdentifier));
        } catch (SQLException e) {
            throw new PorterException("write data failed", e);
        }
    }

    /**
     * write data in batch with prepared statement
     */
    protected void writeInPrepareBatchMode(Relation relation) {
        SinkConnection.Properties properties = this.sinkConnection.getProperties();
        String tableIdentifier = properties.getTableIdentifier();
        StringBuilder sqlBuilder =
                new StringBuilder(String.format("INSERT INTO %s \n", tableIdentifier));
        if (!properties.isAllColumns()) {
            sqlBuilder.append("(").append(relation.getMeta().stream().
                    map(column -> getColumnIdentifier(column.getName(), properties.getQuote())).
                    collect(Collectors.joining(", "))).append(")\n");
        }
        sqlBuilder.append("VALUES\n");
        sqlBuilder.append("(").append(
                        relation.getMeta().stream().map(row -> "?").collect(Collectors.joining(", "))).
                append(")");
        try (PreparedStatement statement = this.connection.prepareStatement(sqlBuilder.toString())) {
            for (List<?> row : relation.getData()) {
                for (int i = 0; i < row.size(); i++) {
                    statement.setObject(i + 1, row.get(i),
                            relation.getMeta().get(i).getColumnType().getVendorTypeNumber());
                }
                statement.addBatch();
            }
            int[] result = statement.executeBatch();
            long rowCount = Arrays.stream(result).summaryStatistics().getSum();
            logger.log(Level.INFO, String.format("wrote %d rows to table %s", rowCount, tableIdentifier));
        } catch (SQLException e) {
            throw new PorterException("write data failed", e);
        }
    }

    /**
     * write in default mode, each data source has its implements
     */
    protected void writeInDefaultMode(Result<?> data) {
        writeInValueMode((Relation) data.getResult());
    }

    @Override
    public void dropTable() {
        String tableIdentifier = getTableIdentifier();
        try (Statement stmt = connection.createStatement()) {
            String ddl = String.format("DROP TABLE IF EXISTS %s", tableIdentifier);
            logger.log(Level.INFO, String.format("drop table %s: %s", tableIdentifier, ddl));
            stmt.executeUpdate(ddl);
        } catch (SQLException e) {
            throw new PorterException("drop table failed", e);
        }
    }

    @Override
    public String getIdentifierQuote() {
        try {
            return this.connection.getMetaData().getIdentifierQuoteString();
        } catch (SQLException e) {
            throw new PorterException("read quote from JDBC meta data failed", e);
        }
    }

    /**
     * generate create DDL
     */
    protected String getCreateDDL(String tableIdentifier, String quote, List<Column> meta) {
        return getCreate(tableIdentifier)
                + getColumns(meta, quote)
                + getConstraints(sortPrimaryKey(meta, quote))
                + getAfterDDL(tableIdentifier, quote, meta);
    }

    /**
     * generate column part of create DDL
     */
    protected String getColumns(List<Column> meta, String quote) {
        return meta.stream().
                map(column ->
                        "\t" + getColumnIdentifier(column.getName(), quote)
                                + "\t" + getColumnType(column)
                                + "\t"
                                + ((notNullOrBlank(column.getNullable()) && "NO".equals(column.getNullable()))
                                ? "NOT NULL" : "NULL")
                                + (notNullOrBlank(column.getComment())
                                ? ("\tCOMMENT\t'" + column.getComment() + "'") : "")).
                collect(Collectors.joining(", \n"));
    }

    /**
     * generate constraints part of create DDL
     */
    protected String getConstraints(Map<Short, String> primaryKeyMap) {
        if (primaryKeyMap != null && !primaryKeyMap.isEmpty()) {
            return ",\n\tPRIMARY KEY (" + String.join(", ", primaryKeyMap.values()) + ")\n)";
        }
        return "\n)";
    }

    /**
     * generate additional part after create DDL, some data source may override it
     */
    protected String getAfterDDL(String tableIdentifier, String quote, List<Column> meta) {
        return "";
    }

    /**
     * generate create header
     */
    protected String getCreate(String tableIdentifier) {
        return String.format("CREATE TABLE IF NOT EXISTS %s (\n", tableIdentifier);
    }

    /**
     * keep primary key order with origin table/view constraint, sorted by primaryKeySeq
     */
    protected Map<Short, String> sortPrimaryKey(List<Column> meta, String quote) {
        Map<Short, String> primaryKeyMap = new TreeMap<>();
        meta.forEach(column -> {
            short primaryKeySeq = column.getPrimaryKeySeq();
            if (column.isPrimaryKey() && primaryKeySeq != 0) {
                primaryKeyMap.put(primaryKeySeq, getColumnIdentifier(column.getName(), quote));
            }
        });
        return primaryKeyMap;
    }

    /**
     * map {@link java.sql.JDBCType} from {@link ResultSetMetaData#getColumnType}
     */
    protected String getColumnType(Column column) {
        //columnTypeName is more common used than columnType in database type
        String columnTypeName = column.getColumnTypeName();
        switch (column.getColumnType()) {
            case BIT:
            case TINYINT:
            case SMALLINT:
            case INTEGER:
            case BIGINT:
            case CHAR:
            case VARCHAR:
            case BINARY:
            case VARBINARY:
            case LONGVARBINARY:
            case ARRAY:
            case NCHAR:
            case NVARCHAR:
            case LONGNVARCHAR:
                return columnTypeName + "(" + column.getPrecision() + ")";
            case FLOAT:
            case REAL:
            case DOUBLE:
            case NUMERIC:
            case DECIMAL:
                return columnTypeName + "(" + column.getPrecision() + ", " + column.getScale() + ")";
            default:
                return columnTypeName;
        }
    }

    @Override
    public void connect(DataConnection dataConnection) throws ConnectionException {
        this.sinkConnection = (SinkConnection) dataConnection;
        try {
            logger.log(Level.INFO, String.format("connecting to %s %s...",
                    dataConnection.getType(), dataConnection.getUrl()));
            this.dataSource = getDataSource(dataConnection);
            this.connection = this.dataSource.getConnection();
        } catch (SQLException | HikariPool.PoolInitializationException exception) {
            throw new ConnectionException(String.format("connect to %s %s failed",
                    dataConnection.getType(), dataConnection.getUrl()), exception);
        }
        logger.log(Level.INFO, String.format("connected to %s %s...",
                dataConnection.getType(), dataConnection.getUrl()));
    }

    @Override
    public void close() {
        logger.log(Level.INFO, String.format("closing connection to %s...", this.sinkConnection));
        try {
            if (this.connection != null && closed()) {
                this.connection.close();
            }
            if (this.dataSource != null) {
                this.dataSource.close();
            }
        } catch (SQLException e) {
            throw new PorterException("connection close failed", e);
        }
    }

    @Override
    public boolean closed() {
        boolean closed;
        try {
            closed = connection.isClosed();
        } catch (SQLException e) {
            throw new PorterException("JDBC connection close failed", e);
        }
        return closed;
    }

    private HikariDataSource getDataSource(DataConnection dataConnection) {
        Properties props = new Properties();
        try (InputStream resourceAsStream = this.getClass().getClassLoader().
                getResourceAsStream("hikari.properties")) {
            props.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HikariConfig hikariConfig = new HikariConfig(props);
        String jdbcURL = getJDBCUrl(dataConnection);
        hikariConfig.setJdbcUrl(jdbcURL);
        hikariConfig.setUsername(dataConnection.getUsername());
        hikariConfig.setPassword(dataConnection.getPassword());
        hikariConfig.setCatalog(dataConnection.getCatalog());
        hikariConfig.setSchema(dataConnection.getSchema());
        return new HikariDataSource(hikariConfig);
    }

    /**
     * get JDBC url for JDBC connection
     */
    public String getJDBCUrl(DataConnection dataConnection) {
        String schema = dataConnection.getCatalog() == null ? dataConnection.getSchema() : dataConnection.getCatalog();
        return String.format("jdbc:%s://%s/%s", getType(), dataConnection.getUrl(), schema);
    }
}