/*
 * Copyright 2007 Google Inc.
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
package java.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Abstract interface for maps.
 *
 * @param <K> key type.
 * @param <V> value type.
 */
public interface Map<K, V> {

  /**
   * Represents an individual map entry.
   */
  public interface Entry<K, V> {
    boolean equals(Object o);

    K getKey();

    V getValue();

    int hashCode();

    V setValue(V value);
  }

  void clear();

  default V computeIfAbsent(K key, Function<? super K, ? extends V> function) {
    Objects.requireNonNull(function);
    V value = get(key);
    if (value == null) {
      V newValue = function.apply(key);
      if (newValue != null) {
        put(key, newValue);
        return newValue;
      }
    }
    return value;
  }

  default V computeIfPresent(K key,
      BiFunction<? super K, ? super V, ? extends V> function) {
    Objects.requireNonNull(function);
    V existing = get(key);
    if (existing != null) {
      V newValue = function.apply(key, existing);
      if (newValue != null) {
        put(key, newValue);
        return newValue;
      } else {
        remove(key);
        return null;
      }
    } else {
      return null;
    }
  }

  default V compute(K key,
      BiFunction<? super K, ? super V, ? extends V> function) {
    Objects.requireNonNull(function);
    V existing = get(key);
    V newValue = function.apply(key, existing);
    if (newValue == null) {
      if (existing != null || containsKey(key)) {
        remove(key);
      }
      return null;
    } else {
      put(key, newValue);
      return newValue;
    }
}


  boolean containsKey(Object key);

  boolean containsValue(Object value);

  Set<Entry<K, V>> entrySet();

  boolean equals(Object o);

  default void forEach(BiConsumer<? super K, ? super V> consumer) {
    Objects.requireNonNull(consumer);
    for (Map.Entry<K, V> entry : entrySet()) {
        K key;
        V value;
        try {
            key = entry.getKey();
            value = entry.getValue();
        } catch(IllegalStateException e) {
            throw new ConcurrentModificationException(e);
        }
        consumer.accept(key, value);
    }
  }

  V get(Object key);

  default V getOrDefault(Object key, V defaultValue) {
    V v = get(key);
    return v != null || containsKey(key) ? v : defaultValue;
  }

  int hashCode();

  boolean isEmpty();

  Set<K> keySet();

  default V merge(K key, V value,
      BiFunction<? super V, ? super V, ? extends V> function) {
    Objects.requireNonNull(function);
    Objects.requireNonNull(value);
    V existing = get(key);
    V newValue = (existing == null) ? value : function.apply(existing, value);
    if(newValue == null) {
      remove(key);
    } else {
      put(key, newValue);
    }
    return newValue;
  }

  V put(K key, V value);

  void putAll(Map<? extends K, ? extends V> t);

  default V putIfAbsent(K key, V value) {
    V existing = get(key);
    if (existing == null) {
      existing = put(key, value);
    }
    return existing;
  }

  V remove(Object key);

  default boolean remove(Object key, Object value) {
    Object existing = get(key);
    if (Objects.equals(existing, value)) {
      if (!containsKey(key)) {
        assert existing == null : "containsKey does not match behavior of get in "+getClass() +" "+this;
        return false;
      }
    } else {
      return false;
    }
    remove(key);
    return true;
  }

  default boolean replace(K key, V oldValue, V newValue) {
    Object existing = get(key);
    if (Objects.equals(existing, oldValue)) {
      if (!containsKey(key)) {
        assert existing == null : "containsKey does not match behavior of get in "+getClass() +" "+this;
        return false;
      }
    } else {
      return false;
    }
    put(key, newValue);
    return true;
  }

  default V replace(K key, V value) {
    return containsKey(key) ? put(key, value) : null;
  }

  default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
    Objects.requireNonNull(function);
    for (Map.Entry<K, V> entry : entrySet()) {
        K key;
        V value;
        try {
            key = entry.getKey();
            value = entry.getValue();
        } catch(IllegalStateException e) {
            throw new ConcurrentModificationException(e);
        }

        value = function.apply(key, value);

        try {
            entry.setValue(value);
        } catch(IllegalStateException e) {
            throw new ConcurrentModificationException(e);
        }
    }
  }

  int size();

  Collection<V> values();
}
