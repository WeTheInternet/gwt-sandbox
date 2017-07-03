package com.google.gwt.dev.codeserver;

/**
 * The approach taken to do the compile.
 */
public enum CompileStrategy {
  FULL("full"), // Compiled all the source.
  INCREMENTAL("incremental"), // Only recompiled the source files that changed.
  SKIPPED("skipped"); // Did not compile anything since nothing changed

  final String jsonName;

  CompileStrategy(String jsonName) {
    this.jsonName = jsonName;
  }

  /**
   * The string to use for serialization.
   */
  public String getJsonName() {
    return jsonName;
  }
}
