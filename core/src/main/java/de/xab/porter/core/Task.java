package de.xab.porter.core;

import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.api.dataconnection.SrcConnection;
import de.xab.porter.api.exception.PorterException;
import de.xab.porter.api.task.Context;
import de.xab.porter.common.spi.ExtensionLoader;
import de.xab.porter.transfer.channel.Channel;
import de.xab.porter.transfer.reader.Reader;
import de.xab.porter.transfer.writer.Writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Task {
    private Context context;
    private Reader reader;
    private List<Map.Entry<Writer, Channel>> writers;

    public Task(Context context) {
        this.context = context;
    }

    public void init() {
        SrcConnection srcConnection = context.getSrcConnection();
        this.reader = ExtensionLoader.getExtensionLoader().loadExtension(srcConnection.getType(), Reader.class);
        this.reader.setChannels(new ArrayList<>());
        register();
        //todo split
    }

//    public List<Reader> split() {
//        Object connection = reader.connect(context.getSrcConnection(), reader.getDataSource(context.getSrcConnection()));
//        List<Reader> readers = this.reader.split(connection, context);
//        return readers;
//    }

    public void register() {
        List<SinkConnection> sinkConnections = context.getSinkConnections();
        this.writers = sinkConnections.stream()
                .map(sink -> {
                    Writer writer = ExtensionLoader.getExtensionLoader().loadExtension(sink.getType(), Writer.class);
                    Object dataSource = writer.getDataSource(sink);
                    Object connection = writer.connect(sink, writer.getDataSource(sink));
                    Channel channel = ExtensionLoader.getExtensionLoader()
                            .loadExtension(this.context.getProperties().getChannel(), Channel.class);
                    channel.setOnReadListener(data -> writer.write(connection, dataSource, sink, data));
                    reader.getChannels().add(channel);
                    return Map.entry(writer, channel);
                }).collect(Collectors.toList());
        registerProperties();
    }

    private void registerProperties() {
        SrcConnection srcConnection = context.getSrcConnection();
        SrcConnection.Properties srcConnectionProperties = srcConnection.getProperties();
        List<SinkConnection> sinkConnections = context.getSinkConnections();
        srcConnectionProperties.setCreate(
                sinkConnections.stream().map(SinkConnection::getProperties)
                        .anyMatch(SinkConnection.Properties::isCreate));
    }

    public void start() {
        Object dataSource = reader.getDataSource(context.getSrcConnection());
        Object connection = null;
        try {
            connection = reader.connect(context.getSrcConnection(), dataSource);
            reader.read(connection, context);
        } catch (Exception e) {
            throw new PorterException("reader start failed", e);
        } finally {
            reader.close(connection, dataSource);
        }
    }
}
