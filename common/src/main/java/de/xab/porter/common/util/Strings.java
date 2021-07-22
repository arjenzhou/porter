package de.xab.porter.common.util;

/**
 * String utils
 */
public final class Strings {
  private Strings() {}

  public static boolean notNullOrEmpty(String str) {
    return str != null && str.length() != 0;
  }
}
