package de.xab.porter.transfer.channel;

import de.xab.porter.api.Result;
import de.xab.porter.api.exception.PorterException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * default channel
 */
public class DefaultChannel implements Channel {
    private final BlockingQueue<Result<?>> resultQueue = new LinkedBlockingQueue<>();
    private Consumer<Result<?>> onReadListener;

    public void push(Result<?> result) {
        try {
            resultQueue.put(result);
        } catch (InterruptedException e) {
            throw new PorterException("push data to channel failed", e);
        }
        notifyWriter();
    }

    public Result<?> pop() {
        try {
            return resultQueue.take();
        } catch (InterruptedException e) {
            throw new PorterException("consume data from channel failed", e);
        }
    }

    @Override
    public void notifyWriter() {
        onReadListener.accept(pop());
    }

    @Override
    public void setOnReadListener(Consumer<Result<?>> listener) {
        this.onReadListener = listener;
    }
}