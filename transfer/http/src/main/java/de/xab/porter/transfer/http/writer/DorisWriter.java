package de.xab.porter.transfer.http.writer;

import de.xab.porter.api.Column;
import de.xab.porter.api.Relation;
import de.xab.porter.api.Result;
import de.xab.porter.api.dataconnection.DataConnection;
import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.api.exception.NotSupportedException;
import de.xab.porter.common.util.Https;
import de.xab.porter.common.util.Loggers;
import de.xab.porter.transfer.http.connector.HttpConnector;
import de.xab.porter.transfer.http.entity.doris.DorisResponseBody;
import de.xab.porter.transfer.writer.AbstractWriter;
import okhttp3.Credentials;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DorisWriter extends AbstractWriter<Void> {
    private final Logger logger = Loggers.getLogger(this.getClass());
    private Map<String, String> header;
    private String url;

    /**
     * [
     * {'column1': 'value1', 'column2': 'value2'},
     * {'column1': 'value3', 'column2': 'value4'},
     * ]
     */
    @Override
    protected void doWrite(Result<?> data) {
        SinkConnection sinkConnection = (SinkConnection) getConnector().getDataConnection();
        SinkConnection.Environments environments = sinkConnection.getEnvironments();
        String tableIdentifier = environments.getTableIdentifier();
        Relation relation = (Relation) data.getResult();
        List<Column> meta = relation.getMeta();
        List<Map<String, Object>> json = ((Relation) data.getResult()).getData().stream().map(row -> {
            Map<String, Object> rowMap = new HashMap<>(row.size());
            for (int i = 0; i < row.size(); i++) {
                rowMap.put(meta.get(i).getName(), row.get(i));
            }
            return rowMap;
        }).collect(Collectors.toList());
        DorisResponseBody dorisResponseBody = Https.put(url, header, json, DorisResponseBody.class);
        if (-1 == dorisResponseBody.getTxnId()) {
            throw new IllegalStateException(String.format("insert failed: %s", dorisResponseBody.getMessage()));
        }
        Long rows = dorisResponseBody.getNumberLoadedRows();
        logger.info(String.format("wrote %d rows to table %s", rows, tableIdentifier));
    }

    /**
     * Doris does not get an HTTP connection, thus combining an url and using a connection pool to write data.
     */
    @Override
    public Void connect(DataConnection dataConnection) {
        HttpConnector dorisConnector = (HttpConnector) getConnector();
        SinkConnection sinkConnection = (SinkConnection) dataConnection;
        dorisConnector.connect(sinkConnection);
        this.header = new HashMap<>();
        String basic = Credentials.basic(dataConnection.getUsername(), dataConnection.getPassword());
        header.put("Authorization", basic);
        header.put("Expect", "100-continue");
        header.put("format", "json");
        header.put("strip_outer_array", "true");
        String schema = dataConnection.getCatalog() == null ? dataConnection.getSchema() : dataConnection.getCatalog();
        this.url = String.format("http://%s/api/%s/%s/_stream_load",
                sinkConnection.getUrl(),
                schema,
                sinkConnection.getTable());
        return null;
    }

    @Override
    protected String getIdentifierQuote() {
        return "`";
    }

    @Override
    public void createTable(Result<?> data) {
        throw new NotSupportedException("do not support creating table");
    }
}