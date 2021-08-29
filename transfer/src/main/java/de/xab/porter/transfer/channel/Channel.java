package de.xab.porter.transfer.channel;

import de.xab.porter.api.Result;

import java.util.function.Consumer;

/**
 * a bridge that connects reader and writer, a channel connect with one writer and more reader
 * <p>
 * reader writes data to all channels registered to, when writer notice the channel is ready to read, then it write data
 * to sink
 */
public interface Channel {
    void push(Result<?> result);

    Result<?> pop();

    /**
     * notify writer data in channel is ready
     */
    void notifyWriter();

    /**
     * register a listener for channel, eligible channel wil notify writer to read data from it
     */
    void setOnReadListener(Consumer<Result<?>> listener);
}
