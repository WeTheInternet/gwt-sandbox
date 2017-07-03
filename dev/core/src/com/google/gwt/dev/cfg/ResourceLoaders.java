/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.cfg;

import static com.google.gwt.thirdparty.guava.common.base.StandardSystemProperty.JAVA_CLASS_PATH;

import com.google.gwt.thirdparty.guava.common.base.Splitter;
import com.google.gwt.thirdparty.guava.common.collect.Sets;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Creates instances of {@link ResourceLoader}.
 */
public class ResourceLoaders {

  private static class ContextClassLoaderAdapter implements ResourceLoader {
    private final ClassLoader contextClassLoader;

    public ContextClassLoaderAdapter() {
      this.contextClassLoader = Thread.currentThread().getContextClassLoader();
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof ContextClassLoaderAdapter)) {
        return false;
      }
      ContextClassLoaderAdapter otherAdapter = (ContextClassLoaderAdapter) other;
      return contextClassLoader.equals(otherAdapter.contextClassLoader);
    }

    /**
     * Returns the URLs for the system class path.
     */
    @Override
    public List<URL> getClassPath() {
      // URL is very slow in collections, so we'll get set semantics using string keys,
      // then just return a list of the values.
      Map<String, URL> result = new LinkedHashMap<>();
      String java9Modules = System.getProperty("gwt.java9.modules");
      for (ClassLoader candidate = contextClassLoader; candidate != null; candidate = candidate.getParent()) {
        if (candidate instanceof URLClassLoader) {
          for (URL url : ((URLClassLoader) candidate).getURLs()) {
            result.put(url.toExternalForm(), url);
          }
        } else  if (java9Modules != null) {
        // If the user specified java 9 modules to load for classpath,
        // we'll want to do it all through reflection,
        // to avoid losing support for java 8
          try {
            final Class<?> modRef = candidate.loadClass("java.lang.module.ModuleReference");
            final Method getLocation = modRef.getMethod("location");
            // In the case of java 9, the only way to scrape classloaders is this ugly reflection
            // on an internal class (used to be a URLClassLoader).
            // you will need to add `--add-opens java.base/jdk.internal.loader=myJava9ModuleName`
            // to your gwt runtime as a VM argument
            final Class<?> loader = candidate.loadClass("jdk.internal.loader.BuiltinClassLoader");
            // TODO: Don't use classloader for resource loading
            final Method findMod = loader.getDeclaredMethod("findModule", String.class);
            // This is why we have to open the package; just being visible is not enough
            // to be allowed to act reflectively
            findMod.setAccessible(true);

            for (String source : java9Modules.split(",")) {
              System.out.println("Loading java 9 module " + source);
              Object mod = findMod.invoke(candidate, source);
              // Safe to case; we must be in java 9 to get here,
              // so we know that this cast should be safe
              Optional<URI> location = (Optional<URI>) getLocation.invoke(mod);
              if (location.isPresent()) {
                final URL url = location.get().toURL();
                result.put(url.toExternalForm(), url);
              }
            }
          } catch (Exception ignored) {
            if (ignored instanceof InterruptedException) {
              Thread.currentThread().interrupt();
              throw new RuntimeException(ignored);
            }
            ignored.printStackTrace();
          }
        }
      }
      if (result.isEmpty()) {
        List<URL> items = new ArrayList<>();
        LinkedHashSet<String> uniqueClassPathEntries =
            Sets.newLinkedHashSet(Splitter.on(File.pathSeparatorChar).split(JAVA_CLASS_PATH.value()));
        for (String entry : uniqueClassPathEntries) {
          try {
            items.add(Paths.get(entry).toUri().toURL());
          } catch (MalformedURLException e) {

          }
          return items;
        }
      }
      return new ArrayList<>(result.values());
    }

    @Override
    public URL getResource(String resourceName) {
      return contextClassLoader.getResource(resourceName);
    }

    @Override
    public int hashCode() {
      return contextClassLoader.hashCode();
    }
  }

  /**
   * A ResourceLoader that prefixes some directories to another ResourceLoader.
   */
  private static class PrefixLoader implements ResourceLoader {
    private final List<File> path;
    private final List<URL> pathAsUrls = new ArrayList<URL>();
    private final ResourceLoader fallback;

    public PrefixLoader(List<File> path, ResourceLoader fallback) {
      assert path != null;
      this.path = path;
      this.fallback = fallback;
      for (File file : path) {
        try {
          pathAsUrls.add(file.toURI().toURL());
        } catch (MalformedURLException e) {
          throw new RuntimeException("can't create URL for file: " + file);
        }
      }
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof PrefixLoader)) {
        return false;
      }
      PrefixLoader otherLoader = (PrefixLoader) other;
      return path.equals(otherLoader.path) && fallback.equals(otherLoader.fallback);
    }

    @Override
    public List<URL> getClassPath() {
      List<URL> result = new ArrayList<URL>();
      result.addAll(pathAsUrls);
      result.addAll(fallback.getClassPath());
      return result;
    }

    @Override
    public URL getResource(String resourceName) {
      for (File prefix : path) {
        File candidate = new File(prefix, resourceName);
        if (candidate.exists()) {
          try {
            return candidate.toURI().toURL();
          } catch (MalformedURLException e) {
            return null;
          }
        }
      }
      return fallback.getResource(resourceName);
    }

    @Override
    public int hashCode() {
      return path.hashCode() ^ fallback.hashCode();
    }
  }

  /**
   * Creates a ResourceLoader that loads from the given thread's class loader.
   */
  public static ResourceLoader fromContextClassLoader() {
    return new ContextClassLoaderAdapter();
  }

  /**
   * Creates a ResourceLoader that loads from a list of directories and falls back
   * to another ResourceLoader.
   */
  public static ResourceLoader forPathAndFallback(List<File> path, ResourceLoader fallback) {
    return new PrefixLoader(path, fallback);
  }

  private ResourceLoaders() {
  }
}
