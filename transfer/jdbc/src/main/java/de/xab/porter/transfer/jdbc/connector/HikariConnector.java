package de.xab.porter.transfer.jdbc.connector;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.api.exception.PorterException;
import de.xab.porter.common.util.Loggers;
import de.xab.porter.transfer.connector.Connector;
import de.xab.porter.transfer.exception.ConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC connector using Hikari connection pool
 */
public class HikariConnector implements Connector<Connection> {
    private final Logger logger = Loggers.getLogger("HIKARI");
    private DataConnection dataConnection;
    private HikariDataSource hikariDataSource;
    private Connection connection;

    @Override
    public Connection connect(Object... args) throws ConnectionException {
        this.dataConnection = (DataConnection) args[0];
        String jdbcUrl = (String) args[1];
        try {
            logger.log(Level.INFO, String.format("connecting to %s %s...",
                    this.dataConnection.getType(), this.dataConnection.getUrl()));
            this.hikariDataSource = genDataSource(this.dataConnection, jdbcUrl);
            this.connection = hikariDataSource.getConnection();
            logger.log(Level.INFO, String.format("connected to %s %s...",
                    this.dataConnection.getType(), this.dataConnection.getUrl()));
            return this.connection;
        } catch (HikariPool.PoolInitializationException | SQLException exception) {
            throw new ConnectionException(String.format("connect to %s %s failed",
                    this.dataConnection.getType(), this.dataConnection.getUrl()), exception);
        }
    }

    @Override
    public void close() {
        logger.log(Level.INFO, String.format("closing connection to %s...", this.dataConnection.getUrl()));
        try {
            Connection connection = this.connection;
            if (connection != null && closed()) {
                connection.close();
            }
            HikariDataSource datasource = this.hikariDataSource;
            if (datasource != null) {
                datasource.close();
            }
        } catch (SQLException e) {
            throw new PorterException("connection close failed", e);
        }
    }

    @Override
    public boolean closed() {
        boolean closed;
        try {
            closed = this.connection.isClosed();
        } catch (SQLException e) {
            throw new IllegalStateException("check JDBC connection failed", e);
        }
        return closed;
    }

    @Override
    public DataConnection getDataConnection() {
        return dataConnection;
    }

    /**
     * generate HikariDatasource for JDBC sources
     */
    private HikariDataSource genDataSource(DataConnection dataConnection, String jdbcUrl) {
        Properties props = new Properties();
        try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("hikari.properties")) {
            props.load(resourceAsStream);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "hikari property file not found");
        }
        HikariConfig hikariConfig = new HikariConfig(props);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(dataConnection.getUsername());
        hikariConfig.setPassword(dataConnection.getPassword());
        hikariConfig.setCatalog(dataConnection.getCatalog());
        hikariConfig.setSchema(dataConnection.getSchema());
        return new HikariDataSource(hikariConfig);
    }
}