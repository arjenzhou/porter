package de.xab.porter.transfer.jdbc.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.api.exception.PorterException;
import de.xab.porter.common.util.Loggers;
import de.xab.porter.transfer.connection.Connectable;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.xab.porter.common.constant.Constant.*;

/**
 * a connector with JDBC data source
 */
public interface JDBCConnector extends Connectable {
    Logger LOGGER = Loggers.getLogger("de.xab.porter.transfer.jdbc.datasource.JDBCDataSource");

    @Override
    default java.sql.Connection connect(DataConnection dataConnection, Object dataSource) {
        java.sql.Connection connection;
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        try {
            LOGGER.log(Level.INFO, String.format("connecting to %s %s...",
                    dataConnection.getType(), dataConnection.getUrl()));
            connection = hikariDataSource.getConnection();
        } catch (SQLException exception) {
            throw new PorterException(String.format("connect to %s %s failed",
                    dataConnection.getType(), dataConnection.getUrl()), exception);
        }
        return connection;
    }

    @Override
    default void close(Object connection, Object dataSource) {
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        LOGGER.log(Level.INFO, String.format("closing connection to %s...", hikariDataSource.getJdbcUrl()));
        try {
            if (connection != null && closed(connection)) {
                ((java.sql.Connection) connection).close();
            }
            hikariDataSource.close();
        } catch (SQLException e) {
            throw new PorterException("connection close failed", e);
        }
    }

    @Override
    default boolean closed(Object connection) {
        boolean closed;
        try {
            closed = ((java.sql.Connection) connection).isClosed();
        } catch (SQLException e) {
            throw new PorterException("JDBC connection close failed", e);
        }
        return closed;
    }

    @Override
    default HikariDataSource getDataSource(DataConnection dataConnection) {
        HikariConfig hikariConfig = new HikariConfig();
        String jdbcURL = getJDBCUrl(dataConnection);
        hikariConfig.setJdbcUrl(jdbcURL);
        hikariConfig.setUsername(dataConnection.getUsername());
        hikariConfig.setPassword(dataConnection.getPassword());
        hikariConfig.setCatalog(dataConnection.getCatalog());
        hikariConfig.setSchema(dataConnection.getSchema());
        hikariConfig.setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);
        hikariConfig.setValidationTimeout(DEFAULT_VALIDATION_TIMEOUT);
        hikariConfig.setMaxLifetime(DEFAULT_MAX_LIFE_TIME);
        hikariConfig.setIdleTimeout(DEFAULT_IDLE_TIMEOUT);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setKeepaliveTime(DEFAULT_KEEP_ALIVE_TIME);
        return new HikariDataSource(hikariConfig);
    }

    /**
     * get JDBC url for JDBC connection
     */
    default String getJDBCUrl(DataConnection dataConnection) {
        String schema = dataConnection.getCatalog() == null ? dataConnection.getSchema() : dataConnection.getCatalog();
        return String.format("jdbc:%s://%s/%s", getType(), dataConnection.getUrl(), schema);
    }
}
