package de.xab.porter.core;

import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.api.dataconnection.SrcConnection;
import de.xab.porter.api.task.Context;
import de.xab.porter.common.spi.ExtensionLoader;
import de.xab.porter.common.util.Loggers;
import de.xab.porter.transfer.channel.Channel;
import de.xab.porter.transfer.exception.ConnectionException;
import de.xab.porter.transfer.reader.Reader;
import de.xab.porter.transfer.reporter.Reporter;
import de.xab.porter.transfer.writer.Writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * atomic unit of a transmission action, may split up by {@link Session}
 */
public class Task {
    private final Logger logger = Loggers.getLogger(this.getClass());
    private final Context context;
    private Reader<?> reader;
    private List<Map.Entry<? extends Writer<?>, Channel>> writers;

    public Task(Context context) {
        this.context = context;
    }

    public void init() {
        SrcConnection srcConnection = context.getSrcConnection();
        this.reader = ExtensionLoader.getExtensionLoader(Reader.class).
                loadExtension(srcConnection.getConnectorType(), srcConnection.getType());
        this.reader.setChannels(new ArrayList<>());
        register();
        //todo split
    }

    /**
     * construct relations among reader, writer and its channel, define the action when channel is ready to write
     */
    public void register() {
        List<SinkConnection> sinkConnections = context.getSinkConnections();
        Reporter reporter = ExtensionLoader.getExtensionLoader(Reporter.class).
                loadExtension(null, "default");
        writers = sinkConnections.stream().
                map(sink -> {
                    Writer<?> writer = ExtensionLoader.getExtensionLoader(Writer.class).
                            loadExtension(sink.getConnectorType(), sink.getType());
                    try {
                        writer.connect(sink);
                    } catch (ConnectionException e) {
                        logger.log(Level.WARNING, "writer connection failed" + e.getCause());
                        writer.close();
                    }
                    Channel channel = ExtensionLoader.getExtensionLoader(Channel.class).
                            loadExtension(null, this.context.getProperties().getChannel());
                    channel.setOnReadListener(data -> {
                        writer.write(data);
                        reporter.report(data);
                    });
                    reader.getChannels().add(channel);
                    return Map.entry(writer, channel);
                }).collect(Collectors.toList());
        registerProperties();
    }

    /**
     * init source behavior by sinks properties
     */
    private void registerProperties() {
        SrcConnection srcConnection = context.getSrcConnection();
        SrcConnection.Properties srcConnectionProperties = srcConnection.getProperties();
        List<SinkConnection> sinkConnections = context.getSinkConnections();
        srcConnectionProperties.setCreate(sinkConnections.stream().
                map(SinkConnection::getProperties).
                anyMatch(SinkConnection.Properties::isCreate));
    }

    /**
     * start a transmission task
     */
    public void start() {
        try {
            reader.connect(context.getSrcConnection());
            reader.read();
        } catch (ConnectionException e) {
            logger.log(Level.WARNING, "reader connection failed " + e.getCause());
        } finally {
            reader.close();
            writers.forEach(writer -> writer.getKey().close());
        }
    }
}