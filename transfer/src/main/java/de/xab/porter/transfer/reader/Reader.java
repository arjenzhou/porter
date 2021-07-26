package de.xab.porter.transfer.reader;

import de.xab.porter.api.Result;
import de.xab.porter.transfer.channel.Channel;
import de.xab.porter.transfer.connection.Connector;

import java.util.List;

/**
 * a reader can read data from data source
 */
public interface Reader extends Connector {
    /**
     * split source table into pieces
     */
    List<Reader> split();

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