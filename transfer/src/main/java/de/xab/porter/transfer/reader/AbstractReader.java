package de.xab.porter.transfer.reader;

import de.xab.porter.api.Column;
import de.xab.porter.api.Result;
import de.xab.porter.api.annoation.Inject;
import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.api.dataconnection.SrcConnection;
import de.xab.porter.common.util.Loggers;
import de.xab.porter.transfer.channel.Channel;
import de.xab.porter.transfer.connector.Connector;
import de.xab.porter.transfer.exception.ConnectionException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * abstract implementation of reader
 */
public abstract class AbstractReader<T> implements Reader<T> {
    protected T connection;
    private Connector<?> connector;
    private List<Channel> channels;

    @Override
    public void read() {
        SrcConnection srcConnection = (SrcConnection) connector.getDataConnection();
        SrcConnection.Properties properties = srcConnection.getProperties();
        Map<String, Column> tableMetaData = new LinkedHashMap<>();
        if (properties.isTable() && properties.isCreate()) {
            tableMetaData = getTableMetaData();
        }
        initProperties(tableMetaData);
        doRead(tableMetaData);
    }

    @Override
    public List<Reader<T>> split() {
        SrcConnection srcConnection = (SrcConnection) connector.getDataConnection();
        SrcConnection.Properties properties = srcConnection.getProperties();
        //todo
        return null;
    }

    /**
     * read data from source
     */
    protected abstract void doRead(Map<String, Column> columnMap);

    /**
     * read meta data of source
     */
    protected abstract Map<String, Column> getTableMetaData();

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

    /**
     * add src conn properties by source's meta
     */
    protected void initProperties(Map<String, Column> tableMeta) {
        SrcConnection srcConnection = (SrcConnection) connector.getDataConnection();
        SrcConnection.Properties properties = srcConnection.getProperties();
        if (properties.isTable()) {
            String columns;
            if (!tableMeta.isEmpty()) {
                columns = tableMeta.values().stream().
                        map(Column::getName).
                        collect(Collectors.joining(", "));
            } else {
                columns = "*";
            }
            String sql = "SELECT " + columns + " FROM "
                    + srcConnection.getSchema() + "." + srcConnection.getTable();
            properties.setSql(sql);
        }
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