package de.xab.porter.core.test;

import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.api.dataconnection.SrcConnection;
import de.xab.porter.api.task.Context;
import de.xab.porter.core.Session;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TaskTest {
    @Test
    public void startShouldFailFastWhenSinkConnectionFails() {
        Context context = new Context();
        SrcConnection srcConnection = ((SrcConnection.Builder) new SrcConnection.Builder().
                connectorType("mock").
                type("mock").
                url("source")).
                sql("SELECT 1").
                properties(SrcConnection.Properties.builder().build()).
                build();
        SinkConnection sinkConnection = ((SinkConnection.Builder) new SinkConnection.Builder().
                connectorType("mock").
                type("mock").
                url("fail")).
                properties(SinkConnection.Properties.builder().build()).
                build();
        context.setSrcConnection(srcConnection);
        context.setSinkConnections(List.of(sinkConnection));

        assertThrows(IllegalStateException.class, () -> new Session().start(context));
    }
}
