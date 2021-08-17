package de.xab.porter.transfer.writer;

import de.xab.porter.api.Result;
import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.common.util.Loggers;
import de.xab.porter.transfer.channel.Channel;

import java.util.logging.Level;
import java.util.logging.Logger;

import static de.xab.porter.common.enums.SequenceEnum.LAST_IS_EMPTY;
import static de.xab.porter.common.enums.SequenceEnum.isFirst;

/**
 * abstract implementation of reader
 */
public abstract class AbstractWriter implements Writer {
    protected SinkConnection sinkConnection;
    private final Logger logger = Loggers.getLogger(this.getClass());
    private Channel channel;
    private String type;

    @Override
    public void write(Result<?> data) {
        SinkConnection.Properties properties = this.sinkConnection.getProperties();
        properties.setQuote(properties.getQuote() == null
                ? getIdentifierQuote() : properties.getQuote());
        properties.setTableIdentifier(getTableIdentifier());

        if (isFirst(data.getSequenceNum())) {
            if (properties.isDrop()) {
                logger.log(Level.FINE, String.format("dropping table %s %s...",
                        this.sinkConnection.getType(), this.sinkConnection.getUrl()));
                dropTable();
            }
            if (properties.isCreate()) {
                logger.log(Level.FINE, String.format("creating table %s %s...",
                        this.sinkConnection.getType(), this.sinkConnection.getUrl()));
                createTable(data);
            }
        }
        logger.log(Level.FINE, String.format("writing data to %s %s...",
                this.sinkConnection.getType(), this.sinkConnection.getUrl()));
        if (data.getSequenceNum() != LAST_IS_EMPTY.getSequenceNum()) {
            doWrite(data);
        }
    }

    /**
     * get sink data source's table identifier
     */
    protected String getTableIdentifier() {
        String quote = this.sinkConnection.getProperties().getQuote();
        return quote + this.sinkConnection.getSchema() + quote + "." + quote + this.sinkConnection.getTable() + quote;
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
    public String getType() {
        return this.type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
}