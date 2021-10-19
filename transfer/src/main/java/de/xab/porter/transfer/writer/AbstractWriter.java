package de.xab.porter.transfer.writer;

import de.xab.porter.api.Result;
import de.xab.porter.api.annoation.Inject;
import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.transfer.channel.Channel;
import de.xab.porter.transfer.connector.Connector;
import de.xab.porter.transfer.exception.ConnectionException;

import static de.xab.porter.common.enums.SequenceEnum.LAST_IS_EMPTY;
import static de.xab.porter.common.enums.SequenceEnum.isFirst;

/**
 * abstract implementation of reader
 */
public abstract class AbstractWriter<T> implements Writer<T> {
    protected T connection;
    private Connector<?> connector;
    private Channel channel;

    @Override
    public void write(Result<?> data) {
        SinkConnection sinkConnection = (SinkConnection) connector.getDataConnection();
        SinkConnection.Properties properties = sinkConnection.getProperties();
        SinkConnection.Environments environments = sinkConnection.getEnvironments();
        environments.setQuote(environments.getQuote() == null ? getIdentifierQuote() : environments.getQuote());
        environments.setTableIdentifier(getTableIdentifier());

        if (isFirst(data.getSequenceNum())) {
            if (properties.isDrop()) {
                dropTable();
            }
            if (properties.isCreate()) {
                createTable(data);
            }
        }
        if (data.getSequenceNum() != LAST_IS_EMPTY.getSequenceNum()) {
            doWrite(data);
        }
    }

    /**
     * get sink data source's table identifier
     */
    protected String getTableIdentifier() {
        SinkConnection sinkConnection = (SinkConnection) connector.getDataConnection();
        String quote = sinkConnection.getEnvironments().getQuote();
        String schema = sinkConnection.getSchema() == null
                ? sinkConnection.getCatalog() : sinkConnection.getSchema();
        return quote + schema + quote + "." + quote + sinkConnection.getTable() + quote;
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
     * @param data data to be written
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