package de.xab.porter.transfer.reader;

import de.xab.porter.api.Column;
import de.xab.porter.api.Result;
import de.xab.porter.api.dataconnection.SrcConnection;
import de.xab.porter.common.util.Loggers;
import de.xab.porter.transfer.channel.Channel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * abstract implementation of reader
 */
public abstract class AbstractReader implements Reader {
    protected SrcConnection srcConnection;
    private final Logger logger = Loggers.getLogger(this.getClass());
    private String type;
    private List<Channel> channels;

    @Override
    public void read() {
        SrcConnection srcConnection = this.srcConnection;
        SrcConnection.Properties properties = srcConnection.getProperties();
        Map<String, Column> tableMetaData = new LinkedHashMap<>();
        if (properties.isTable() && properties.isCreate()) {
            logger.log(Level.INFO, String.format("reading table metadata of %s %s...",
                    srcConnection.getType(), srcConnection.getUrl()));
            tableMetaData = getTableMetaData();
        }
        initProperties(tableMetaData);
        logger.log(Level.FINE, String.format("%s, reading table data from %s %s...",
                properties.getSql(), srcConnection.getType(), srcConnection.getUrl()));
        doRead(tableMetaData);
    }

    @Override
    public List<Reader> split() {
        SrcConnection srcConnection = this.srcConnection;
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

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    /**
     * add src conn properties by source's meta
     */
    protected void initProperties(Map<String, Column> tableMeta) {
        SrcConnection.Properties properties = this.srcConnection.getProperties();
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
                    + this.srcConnection.getSchema() + "." + this.srcConnection.getTable();
            properties.setSql(sql);
        }
    }
}