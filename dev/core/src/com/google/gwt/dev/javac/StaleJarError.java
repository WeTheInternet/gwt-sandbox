package com.google.gwt.dev.javac;

/**
 * Thrown when a compilation fails because an underlying jar input stream was closed.
 *
 * This can happen to the recompiler when source jars change;
 * by throwing this more specific exception,
 * we can detect when this occurs, and automatically discard our classloader
 * and restart a fresh compilation.
 *
 * Created by James X. Nelson (james @wetheinter.net) on 1/3/17.
 */
public class StaleJarError extends Error {
    private final String location;
    private final String typeName;

    public StaleJarError(String typeName, String location) {
        super("Jar file read error for " + typeName + " from " + location);
        this.typeName = typeName;
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public String getTypeName() {
        return typeName;
    }
}
