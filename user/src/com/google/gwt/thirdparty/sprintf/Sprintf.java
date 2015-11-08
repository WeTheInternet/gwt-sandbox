package com.google.gwt.thirdparty.sprintf;

/**
 * This class is used for emulation of String.format.
 * In JVM runtimes, we call String.format,
 * and in GWT runtimes, String.format calls us.
 **
 * The js sprintf library is not 100% compatible w/ the java apis, but it's good enough for 99% use cases,
 * and anything awry can be fixed by modifying our copy of the js library.
 *
 * The original script is open source online @
 * https://raw.githubusercontent.com/alexei/sprintf.js/master/src/sprintf.js
 */
public final class Sprintf {

  private Sprintf(){}

  public static String format(String format, Object... args) {
    return String.format(format, args);
  }

}

