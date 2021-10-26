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
    List<String> split();

    /**
     * read data from reader
     */
    long read(String sql);

    /**
     * push data to channels registered to
     */
    void pushToChannel(Result<?> result);

    List<Channel> getChannels();

    void setChannels(List<Channel> channels);
}