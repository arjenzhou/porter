package de.xab.porter.core;

import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.api.dataconnection.SrcConnection;
import de.xab.porter.api.task.Context;
import de.xab.porter.api.task.Properties;
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
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * atomic unit of a transmission action, may split up by {@link Session}
 */
public class Task {
    private final Logger logger = Loggers.getLogger(this.getClass());
    private final Context context;
    private Map<? extends Reader<?>, String> readers;
    private List<Map.Entry<? extends Writer<?>, Channel>> writers;

    public Task(Context context) {
        this.context = context;
    }

    public void init() {
        SrcConnection srcConnection = context.getSrcConnection();
        Reader<?> splitReader = ExtensionLoader.getExtensionLoader(Reader.class).
                loadExtension(srcConnection.getConnectorType(), srcConnection.getType());
        List<String> sequels;
        if (srcConnection.getProperties().isSplit()) {
            try {
                splitReader.connect(srcConnection);
                sequels = splitReader.split();
            } catch (ConnectionException e) {
                sequels = List.of(srcConnection.getSql());
                logger.warning("reader connection failed " + e.getCause());
            } finally {
                splitReader.close();
            }
            this.readers = sequels.stream().
                    map(sql -> Map.<Reader<?>, String>entry(
                            ExtensionLoader.getExtensionLoader(Reader.class).
                                    loadExtension(srcConnection.getConnectorType(), srcConnection.getType()), sql)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            this.readers = Map.of(splitReader, srcConnection.getSql());
        }
        this.readers.forEach((reader, sql) -> reader.setChannels(new ArrayList<>()));
        registerChannel();
    }

    /**
     * construct relations among reader, writer and its channel, define the action when channel is ready to write
     */
    public void registerChannel() {
        Properties properties = context.getProperties();
        List<SinkConnection> sinkConnections = context.getSinkConnections();
        Reporter reporter = ExtensionLoader.getExtensionLoader(Reporter.class).
                loadExtension(null, properties.getReporter());
        writers = sinkConnections.stream().
                map(sink -> {
                    Writer<?> writer = ExtensionLoader.getExtensionLoader(Writer.class).
                            loadExtension(sink.getConnectorType(), sink.getType());
                    try {
                        writer.connect(sink);
                    } catch (ConnectionException e) {
                        logger.severe("writer connection failed" + e.getCause());
                        writer.close();
                    }
                    Channel channel = ExtensionLoader.getExtensionLoader(Channel.class).
                            loadExtension(null, this.context.getProperties().getChannel());
                    channel.setOnReadListener(data -> {
                        writer.write(data);
                        reporter.report(data);
                    });
                    readers.forEach((reader, sql) -> reader.getChannels().add(channel));
                    return Map.entry(writer, channel);
                }).collect(Collectors.toList());
    }

    /**
     * start a transmission task
     */
    public void start() {
        CountDownLatch countDownLatch = new CountDownLatch(readers.size());
        ExecutorService executorService = Executors.newFixedThreadPool(readers.size());
        List<Future<Long>> futures = new ArrayList<>(readers.size());
        readers.forEach((reader, sql) -> {
            Future<Long> future = executorService.submit(() -> {
                long rows = 0L;
                try {
                    reader.connect(context.getSrcConnection());
                    rows = reader.read(sql);
                } catch (ConnectionException e) {
                    logger.severe("reader connection failed " + e.getCause());
                } finally {
                    reader.close();
                    countDownLatch.countDown();
                }
                return rows;
            });
            futures.add(future);
        });

        try {
            countDownLatch.await();
            long totalRows = futures.stream().mapToLong(future -> {
                try {
                    return future.get();
                } catch (InterruptedException e) {
                    logger.severe("future is interrupted, " + e.getMessage());
                } catch (ExecutionException e) {
                    logger.severe("future execution failed, " + e.getMessage());
                }
                return 0L;
            }).sum();
            logger.info("task execution over, total read " + totalRows + " rows.");
        } catch (InterruptedException e) {
            logger.warning("reader interrupted");
        } finally {
            writers.forEach(writer -> writer.getKey().close());
            executorService.shutdown();
        }
    }
}