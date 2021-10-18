package de.xab.porter.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * logger utils
 */
public final class Loggers {
    private static final Map<String, Logger> LOGGER_HOLDER = new HashMap<>();

    private Loggers() {
    }

    public static Logger getLogger(String name) {
        return LOGGER_HOLDER.computeIfAbsent(name, logger -> Logger.getLogger(name));
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }
}
