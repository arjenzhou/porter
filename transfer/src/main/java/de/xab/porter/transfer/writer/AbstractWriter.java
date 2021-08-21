package de.xab.porter.transfer.writer;

import de.xab.porter.api.Result;
import de.xab.porter.api.annoation.Inject;
import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.common.util.Loggers;
import de.xab.porter.transfer.channel.Channel;
import de.xab.porter.transfer.connector.Connector;
import de.xab.porter.transfer.exception.ConnectionException;

import java.util.logging.Level;
import java.util.logging.Logger;

import static de.xab.porter.common.enums.SequenceEnum.LAST_IS_EMPTY;
import static de.xab.porter.common.enums.SequenceEnum.isFirst;

/**
 * abstract implementation of reader
 */
public abstract class AbstractWriter<T> implements Writer<T> {
    protected T connection;
    private Connector<?> connector;
    private final Logger logger = Loggers.getLogger(this.getClass());
    private Channel channel;

    @Override
    public void write(Result<?> data) {
        SinkConnection sinkConnection = (SinkConnection) connector.getDataConnection();
        SinkConnection.Properties properties = sinkConnection.getProperties();
        properties.setQuote(properties.getQuote() == null
                ? getIdentifierQuote() : properties.getQuote());
        properties.setTableIdentifier(getTableIdentifier());

        if (isFirst(data.getSequenceNum())) {
            if (properties.isDrop()) {
                logger.log(Level.FINE, String.format("dropping table %s %s...",
                        sinkConnection.getType(), sinkConnection.getUrl()));
                dropTable();
            }
            if (properties.isCreate()) {
                logger.log(Level.FINE, String.format("creating table %s %s...",
                        sinkConnection.getType(), sinkConnection.getUrl()));
                createTable(data);
            }
        }
        logger.log(Level.FINE, String.format("writing data to %s %s...",
                sinkConnection.getType(), sinkConnection.getUrl()));
        if (data.getSequenceNum() != LAST_IS_EMPTY.getSequenceNum()) {
            doWrite(data);
        }
    }

    /**
     * get sink data source's table identifier
     */
    protected String getTableIdentifier() {
        SinkConnection sinkConnection = (SinkConnection) connector.getDataConnection();
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
    protected abstract String getIdentifierQuote();

    /**
     * write data to sink data source
     *
     * @param data data to be write
     */
    protected abstract void doWrite(Result<?> data);

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T connect(DataConnection dataConnection) throws ConnectionException {
        this.connection = (T) connector.connect(dataConnection);
        return this.connection;
    }

    @Override
    public void close() {
        connector.close();
    }

    @Override
    public boolean closed() {
        return connector.closed();
    }

    @Inject
    @Override
    public void setConnector(Connector<?> connector) {
        this.connector = connector;
    }

    @Override
    public Connector<?> getConnector() {
        return connector;
    }
}