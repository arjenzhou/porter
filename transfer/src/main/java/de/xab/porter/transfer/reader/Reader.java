package de.xab.porter.transfer.reader;

import de.xab.porter.api.Result;
import de.xab.porter.transfer.channel.Channel;
import de.xab.porter.transfer.connector.Connectable;

import java.util.List;

/**
 * a reader can read data from data source
 */
public interface Reader<T> extends Connectable<T> {
    /**
     * split source table into pieces
     */
    List<Reader<T>> split();

    /**
     * read data from reader
     */
    void read();

    /**
     * push data to channels registered to
     */
    void pushToChannel(Result<?> result);

    List<Channel> getChannels();

    void setChannels(List<Channel> channels);
}