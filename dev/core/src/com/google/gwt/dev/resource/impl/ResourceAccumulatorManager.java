/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.resource.impl;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gwt.thirdparty.guava.common.annotations.VisibleForTesting;
import com.google.gwt.thirdparty.guava.common.collect.ImmutableMap;
import com.google.gwt.thirdparty.guava.common.collect.Maps;

/**
 * Manages {@link ResourceAccumulator}s for DirectoryClassPathEntry + PathPrefixSet pairs.
 * <p>
 * ResourceAccumulators consume native resources and so require very strict lifecycle management but
 * ClassPathEntry and PathPrefixSet lifecycle management is very loose. This makes it difficult to
 * release ResourceAccumulator at the proper time. This manager class uses weak references to
 * ClassPathEntry and PathPrefixSet instances to lazily discover when ResourceAccumulator instances
 * become eligible for destruction.
 */
public class ResourceAccumulatorManager {

  /**
   * A hash key that is a combination of a DirectoryClassPathEntry and PathPrefixSet which also
   * takes special care not to block the garbage collection of either.
   */
  private static class DirectoryAndPathPrefix {

    private final WeakReference<DirectoryClassPathEntry> directoryClassPathEntryRef;
    private final WeakReference<PathPrefixSet> pathPrefixSetRef;
    private int hashCode;

    public DirectoryAndPathPrefix(DirectoryClassPathEntry directoryClassPathEntry,
        PathPrefixSet pathPrefixSet) {
      this.directoryClassPathEntryRef = new WeakReference<>(directoryClassPathEntry);
      this.pathPrefixSetRef = new WeakReference<PathPrefixSet>(pathPrefixSet);
      hashCode = Objects.hash(directoryClassPathEntry, pathPrefixSet);
    }

    @Override
    public boolean equals(Object object) {
      if (object instanceof DirectoryAndPathPrefix) {
        DirectoryAndPathPrefix other = (DirectoryAndPathPrefix) object;
        return directoryClassPathEntryRef.get() == other.directoryClassPathEntryRef.get()
            && pathPrefixSetRef.get() == other.pathPrefixSetRef.get();
      }
      return false;
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    /**
     * If either the instance has been destroyed then it is no longer possible for a caller to
     * request the accumulated sources for the combination. This means the combination is
     * old and tracking can be stopped.
     */
    public boolean isOld() {
      return directoryClassPathEntryRef.get() == null || pathPrefixSetRef.get() == null;
    }
  }

  private static Map<DirectoryAndPathPrefix, ResourceAccumulator> resourceAccumulators = Maps
      .newHashMap();

  static {
    // Keep the resources fresh
    new Thread() {
      @Override
      public void run() {
        while (true) {
          try {
            refreshResources();
            Thread.sleep(10);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      {
        setName("ResourceAccumulatorManager");
        setDaemon(true);
      }
    }.start();
  }

  public static synchronized Map<AbstractResource, ResourceResolution> getResources(
      DirectoryClassPathEntry directoryClassPathEntry, PathPrefixSet pathPrefixSet)
      throws IOException {
    DirectoryAndPathPrefix directoryAndPathPrefix =
        new DirectoryAndPathPrefix(directoryClassPathEntry, pathPrefixSet);

    ResourceAccumulator resourceAccumulator = resourceAccumulators.get(directoryAndPathPrefix);
    if (resourceAccumulator == null) {
      Path path = directoryClassPathEntry.getDirectory().toPath();
      resourceAccumulator = new ResourceAccumulator(path, pathPrefixSet);
      resourceAccumulators.put(directoryAndPathPrefix, resourceAccumulator);
    }
    resourceAccumulator.refreshResources();
    return ImmutableMap.copyOf(resourceAccumulator.getResources());
  }

  static synchronized void refreshResources() throws IOException {
    Iterator<Entry<DirectoryAndPathPrefix, ResourceAccumulator>> entriesIterator =
        resourceAccumulators.entrySet().iterator();
    while (entriesIterator.hasNext()) {
      Entry<DirectoryAndPathPrefix, ResourceAccumulator> entry = entriesIterator.next();
      DirectoryAndPathPrefix directoryAndPathPrefix = entry.getKey();
      ResourceAccumulator resourceAccumulator = entry.getValue();
      if (directoryAndPathPrefix.isOld()) {
        resourceAccumulator.shutdown();
        entriesIterator.remove();
      } else if (resourceAccumulator.isWatchServiceActive()) {
        resourceAccumulator.refreshResources();
      }
    }
  }

  private static final Lock lock = new ReentrantLock();

  /**
   * Checks if the file monitors across all Gwt source directories
   * have detected any changes, and runs either `fresh` or `stale` callbacks.
   *
   * While other (unfortunately) static methods in this class use synchronized,
   * we need to be able to lock the freshness check without
   * interfering / considering / deadlocking other threads
   * who may (do) take the monitor on ResourceAccumulatorManager.class
   * (due to synchronized static method) while we are running *your* callbacks,
   * such as... recompiling / refreshing the state
   * @param fresh
   * @param stale
   */
  public static void checkCompileFreshness(Runnable fresh, Runnable stale) {
    checkCompileFreshness(fresh, stale, false);
  }
  public static void checkCompileFreshness(final Runnable fresh, final Runnable stale, final boolean runFreshAfterStale) {

    lock.lock();
    // We need to hold the lock longer than the call of this method;
    // since we are sending callbacks to code that is free (encouraged) to
    // send their callbacks off and return early as well,
    // we need to defer the release of the lock, so that
    // all subsequent requesters must wait in this method
    // until these callbacks have finished.
    final Runnable ifFresh = new Runnable() {
      @Override
      public void run() {
        // when we are fresh, we unlock and then we run;
        // no need to make other threads wait for fresh.run()
        lock.unlock();
        fresh.run();
      }
    };
    final Runnable ifStale = new Runnable() {
      @Override
      public void run() {
        // When stale, we want to run first, unlock later.
        try {
          stale.run(); // If stale doesn't block, we might wind up serving bad requests...
          // Prevent any other thread from considering the compile fresh until ifStale has run successfully
          for (ResourceAccumulator accumulator : resourceAccumulators.values()) {
            // TODO: Be able to delete this clearing code
            // by actually correcting the state during recompilation
            accumulator.getStale().clear();
          }
        } finally {
          // Once we unlock, all waiting threads should get the all clear to run fresh callback
          lock.unlock();
          if (runFreshAfterStale) {
            fresh.run();
          }
        }
      }
    };

    boolean isFresh = true;
    try {

      Iterator<Entry<DirectoryAndPathPrefix, ResourceAccumulator>> entriesIterator =
          resourceAccumulators.entrySet().iterator();
      // TODO: also monitor jar files...  just directories is good enough for now.
      while (entriesIterator.hasNext()) {
        Entry<DirectoryAndPathPrefix, ResourceAccumulator> entry = entriesIterator.next();
        DirectoryAndPathPrefix directoryAndPathPrefix = entry.getKey();
        if (directoryAndPathPrefix.isOld()) {
          isFresh = false;
          break;
        }
        if (!entry.getValue().isWatchServiceActive()) {
          isFresh = false;
          break;
        }
        if (!entry.getValue().getStale().isEmpty()) {
          isFresh = false;
          break;
        }
      }

    } finally {
      // performs unlocks
      if (isFresh) {
        ifFresh.run();
      } else {
        ifStale.run();
      }
    }
  }

  @VisibleForTesting
  static int getActiveListenerCount() throws IOException {
    refreshResources();

    return resourceAccumulators.size();
  }

  @VisibleForTesting
  static boolean isListening(DirectoryClassPathEntry directoryClassPathEntry,
      PathPrefixSet pathPrefixSet) {
    return resourceAccumulators.containsKey(
        new DirectoryAndPathPrefix(directoryClassPathEntry, pathPrefixSet));
  }
}
