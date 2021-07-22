package de.xab.porter.transfer.writer;

import de.xab.porter.api.Result;
import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.api.exception.PorterException;
import de.xab.porter.common.util.Loggers;
import de.xab.porter.transfer.channel.Channel;
import de.xab.porter.transfer.connection.Connectable;

import java.util.logging.Level;
import java.util.logging.Logger;

import static de.xab.porter.common.enums.SequenceEnum.*;

/**
 * abstract implementation of reader
 */
public abstract class AbstractWriter implements Writer, Connectable {
    private final Logger logger = Loggers.getLogger(this.getClass());
    private Channel channel;
    private String type;

    @Override
    public void write(Object connection, Object dataSource, DataConnection dataConnection, Result<?> data) {
        SinkConnection sinkConnection = (SinkConnection) dataConnection;
        SinkConnection.Properties properties = sinkConnection.getProperties();
        try {
            properties.setQuote(properties.getQuote() == null
                    ? getIdentifierQuote(connection) : properties.getQuote());
            properties.setTableIdentifier(getTableIdentifier(sinkConnection));

            if (isFirst(data.getSequenceNum())) {
                if (properties.isDrop()) {
                    logger.log(Level.FINE, String.format("dropping table %s %s...",
                            sinkConnection.getType(), sinkConnection.getUrl()));
                    dropTable(sinkConnection, connection);
                }
                if (properties.isCreate()) {
                    logger.log(Level.FINE, String.format("creating table %s %s...",
                            sinkConnection.getType(), sinkConnection.getUrl()));
                    createTable(sinkConnection, connection, data);
                }
            }
            logger.log(Level.FINE, String.format("writing data to %s %s...",
                    sinkConnection.getType(), sinkConnection.getUrl()));
            if (data.getSequenceNum() != LAST_IS_EMPTY.getSequenceNum()) {
                doWrite(sinkConnection, connection, data);
            }
            if (isLast(data.getSequenceNum())) {
                close(connection, dataSource);
            }
        } catch (PorterException exception) {
            close(connection, dataSource);
        }
    }

    /**
     * get sink data source's table identifier
     */
    protected String getTableIdentifier(SinkConnection sinkConnection) {
        String quote = sinkConnection.getProperties().getQuote();
        return quote + sinkConnection.getSchema() + quote + "." + quote + sinkConnection.getTable() + quote;
    }

    /**
     * get sink table column's identifier
     */
    protected String getColumnIdentifier(String columnName, String quote) {
        return quote + columnName + quote;
    }

    /**
     * read keyword quote from data source
     */
    protected abstract String getIdentifierQuote(Object connection);

    /**
     * write data to sink data source
     *
     * @param dataConnection connection properties of sink
     * @param connection     connection of sink
     * @param data           data to be write
     */
    protected abstract void doWrite(DataConnection dataConnection, Object connection, Result<?> data);

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
}