package de.xab.porter.transfer.writer;

import de.xab.porter.api.Result;
import de.xab.porter.transfer.channel.Channel;
import de.xab.porter.transfer.connector.Connectable;

/**
 * writer can write data to datasource
 */
public interface Writer<T> extends Connectable<T> {
    /**
     * write data to sink data source, may contains operations before do write
     */
    void write(Result<?> data);

    /**
     * create table for sink table
     */
    void createTable(Result<?> data);

    /**
     * drop out-dated table before create sink table
     */
    void dropTable();

    Channel getChannel();

    void setChannel(Channel channel);
}
