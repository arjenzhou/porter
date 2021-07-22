package de.xab.porter.transfer.reader;

import de.xab.porter.api.Result;
import de.xab.porter.api.task.Context;
import de.xab.porter.transfer.channel.Channel;
import de.xab.porter.transfer.connection.Connectable;
import java.util.List;

/**
 * a reader can read data from data source
 */
public interface Reader extends Connectable {
  /**
   * split source table into pieces
   */
  List<Reader> split(Object connection, Context context);

  /**
   * read data from reader
   */
  void read(Object connection, Context context);

  /**
   * push data to channels registered to
   */
  void pushToChannel(Result<?> result);

  List<Channel> getChannels();

  void setChannels(List<Channel> channels);
}
