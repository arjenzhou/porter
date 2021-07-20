package de.xab.porter.api.task;

import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.api.dataconnection.SrcConnection;

import java.util.List;

public class Context {
    private SrcConnection srcConnection;
    private List<SinkConnection> sinkConnections;
    private Properties properties = new Properties();

    public SrcConnection getSrcConnection() {
        return srcConnection;
    }

    public void setSrcConnection(SrcConnection srcConnection) {
        this.srcConnection = srcConnection;
    }

    public List<SinkConnection> getSinkConnections() {
        return sinkConnections;
    }

    public void setSinkConnections(List<SinkConnection> sinkConnections) {
        this.sinkConnections = sinkConnections;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
