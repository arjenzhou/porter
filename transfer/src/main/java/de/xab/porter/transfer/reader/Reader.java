package de.xab.porter.transfer.reader;

import de.xab.porter.api.Result;
import de.xab.porter.api.task.Context;
import de.xab.porter.transfer.channel.Channel;
import de.xab.porter.transfer.datasource.DataSource;

import java.util.List;

public interface Reader extends DataSource {
    List<Reader> split(Object connection, Context context);

    void read(Object Connection, Context context);

    void pushToChannel(Result<?> result);

    List<Channel> getChannels();

    void setChannels(List<Channel> channels);
}