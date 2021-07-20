package de.xab.porter.transfer.channel;

import de.xab.porter.api.Result;

import java.util.function.Consumer;

public interface Channel {
    void push(Result<?> result);

    Result<?> pop();

    void notifyWriter();

    void setOnReadListener(Consumer<Result<?>> listener);

    void setType(String type);
}
