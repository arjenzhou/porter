package de.xab.porter.transfer.reader;

import de.xab.porter.api.Column;
import de.xab.porter.api.Result;
import de.xab.porter.api.annoation.Inject;
import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.api.dataconnection.SrcConnection;
import de.xab.porter.common.util.Strings;
import de.xab.porter.transfer.channel.Channel;
import de.xab.porter.transfer.connector.Connector;
import de.xab.porter.transfer.exception.ConnectionException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * abstract implementation of reader
 */
public abstract class AbstractReader<T> implements Reader<T> {
    protected T connection;
    private Connector<?> connector;
    private List<Channel> channels;

    @Override
    public long read(String sql) {
        SrcConnection srcConnection = (SrcConnection) connector.getDataConnection();
        SrcConnection.Properties properties = srcConnection.getProperties();
        //keep insertion order
        Map<String, Column> tableMetaData = new LinkedHashMap<>();
        //avoid unnecessary meta query while not creating tables
        if (properties.isReadTableMeta() && Strings.notNullOrBlank(srcConnection.getTable())) {
            tableMetaData = getTableMetaData();
        }
        return doRead(tableMetaData, sql);
    }

    @Override
    public abstract List<String> split();

    /**
     * read data from source
     */
    protected abstract long doRead(Map<String, Column> columnMap, String sql);

    /**
     * read metadata of source
     */
    protected abstract Map<String, Column> getTableMetaData();

    /**
     * read keyword quote from data source
     */
    protected abstract String getIdentifierQuote();

    @Override
    public void pushToChannel(Result<?> result) {
        this.getChannels().forEach(channel -> channel.push(result));
    }

    @Override
    public List<Channel> getChannels() {
        return this.channels;
    }

    @Override
    public void setChannels(List<Channel> channels) {
        this.channels = channels;
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

    @Override
    public Connector<?> getConnector() {
        return connector;
    }

    @Override
    @Inject
    public void setConnector(Connector<?> connector) {
        this.connector = connector;
    }
}