package de.xab.porter.transfer.writer;

import de.xab.porter.api.Result;
import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.transfer.channel.Channel;
import de.xab.porter.transfer.connection.Connectable;

/**
 * writer can write data to datasource
 */
public interface Writer extends Connectable {
    /**
     * write data to sink data source, may contains operations before do write
     */
    void write(Object connection, Object dataSource, DataConnection dataConnection, Result<?> data);

    /**
     * create table for sink table
     */
    void createTable(DataConnection dataConnection, Object connection, Result<?> data);

    /**
     * drop out-dated table before create sink table
     */
    void dropTable(DataConnection dataConnection, Object connection);

    Channel getChannel();

    void setChannel(Channel channel);
}
