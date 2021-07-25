package de.xab.porter.common.util;

/**
 * String utils
 */
public final class Strings {
    private Strings() {
    }

    public static boolean notNullOrBlank(String str) {
        return str != null && !str.isBlank();
    }
}
