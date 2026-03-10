package de.xab.porter.core.test.fixture;

import de.xab.porter.api.Result;
import de.xab.porter.transfer.writer.AbstractWriter;

public class MockWriter extends AbstractWriter<Object> {
    @Override
    protected String getIdentifierQuote() {
        return "\"";
    }

    @Override
    protected void doWrite(Result<?> data) {
    }

    @Override
    public void createTable(Result<?> data) {
    }
}
