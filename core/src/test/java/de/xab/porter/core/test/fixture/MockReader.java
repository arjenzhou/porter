package de.xab.porter.core.test.fixture;

import de.xab.porter.api.Column;
import de.xab.porter.transfer.reader.AbstractReader;

import java.util.List;
import java.util.Map;

public class MockReader extends AbstractReader<Object> {
    @Override
    public List<String> split() {
        return List.of("SELECT 1");
    }

    @Override
    protected long doRead(Map<String, Column> columnMap, String sql) {
        return 0L;
    }

    @Override
    protected Map<String, Column> getTableMetaData() {
        return Map.of();
    }

    @Override
    protected String getIdentifierQuote() {
        return "\"";
    }
}
