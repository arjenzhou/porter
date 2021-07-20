package de.xab.porter.transfer.writer;

import de.xab.porter.api.Result;
import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.transfer.channel.Channel;
import de.xab.porter.transfer.datasource.DataSource;

public interface Writer extends DataSource {
    void write(Object connection, Object dataSource, DataConnection dataConnection, Result<?> data);

    void createTable(DataConnection dataConnection, Object connection, Result<?> data);

    void dropTable(DataConnection dataConnection, Object connection);

    Channel getChannel();

    void setChannel(Channel channel);
}
