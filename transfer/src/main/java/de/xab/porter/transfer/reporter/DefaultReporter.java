package de.xab.porter.transfer.reporter;

import de.xab.porter.api.Result;
import de.xab.porter.common.util.Loggers;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultReporter implements Reporter {
    private final Logger logger = Loggers.getLogger("REPORTER");

    @Override
    public <T> void report(T t) {
        Result<?> result = (Result<?>) t;
        logger.log(Level.INFO, String.format("wrote %sth batch", result.getSequenceNum()));
    }
}
