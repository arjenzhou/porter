package de.xab.porter.transfer.jdbc.connector;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.api.exception.PorterException;
import de.xab.porter.common.util.Loggers;
import de.xab.porter.transfer.connection.Connector;
import de.xab.porter.transfer.exception.ConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface JDBCConnector extends Connector {
    Logger LOGGER = Loggers.getLogger("JDBC");

    /**
     * get JDBC url for JDBC connection
     */
    default String getJDBCUrl(DataConnection dataConnection) {
        String schema = dataConnection.getCatalog() == null ? dataConnection.getSchema() : dataConnection.getCatalog();
        return String.format("jdbc:%s://%s/%s", dataConnection.getType(), dataConnection.getUrl(), schema);
    }

    default void connect(DataConnection dataConnection) throws ConnectionException {
        setDataConnection(dataConnection);
        try {
            LOGGER.log(Level.INFO, String.format("connecting to %s %s...",
                    dataConnection.getType(), dataConnection.getUrl()));
            setDatasource(genDataSource(dataConnection));
            setConnection(getDatasource().getConnection());
        } catch (HikariPool.PoolInitializationException | SQLException exception) {
            throw new ConnectionException(String.format("connect to %s %s failed",
                    dataConnection.getType(), dataConnection.getUrl()), exception);
        }
        LOGGER.log(Level.INFO, String.format("connected to %s %s...",
                dataConnection.getType(), dataConnection.getUrl()));
    }

    default void close() {
        LOGGER.log(Level.INFO, String.format("closing connection to %s...", getDataConnection()));
        try {
            Connection connection = getConnection();
            if (connection != null && closed()) {
                connection.close();
            }
            HikariDataSource datasource = getDatasource();
            if (datasource != null) {
                datasource.close();
            }
        } catch (SQLException e) {
            throw new PorterException("connection close failed", e);
        }
    }

    default boolean closed() {
        boolean closed;
        try {
            closed = getConnection().isClosed();
        } catch (SQLException e) {
            throw new PorterException("check JDBC connection failed", e);
        }
        return closed;
    }

    private HikariDataSource genDataSource(DataConnection dataConnection) {
        Properties props = new Properties();
        try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("hikari.properties")) {
            props.load(resourceAsStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "hikari property file not found");
        }
        HikariConfig hikariConfig = new HikariConfig(props);
        hikariConfig.setJdbcUrl(getJDBCUrl(dataConnection));
        hikariConfig.setUsername(dataConnection.getUsername());
        hikariConfig.setPassword(dataConnection.getPassword());
        hikariConfig.setCatalog(dataConnection.getCatalog());
        hikariConfig.setSchema(dataConnection.getSchema());
        return new HikariDataSource(hikariConfig);
    }

    void setConnection(Connection connection);

    Connection getConnection();

    void setDatasource(HikariDataSource datasource);

    HikariDataSource getDatasource();
}
