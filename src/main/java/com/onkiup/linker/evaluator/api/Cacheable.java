package com.onkiup.linker.evaluator.api;

import java.util.Map;
import java.util.WeakHashMap;

public interface Cacheable<R> {

  class Cache {
    private static Map<Cacheable, Object> values = new WeakHashMap<>();

    protected static Map<Cacheable, Object> values() {
      return values;
    }
  }

  default boolean present() {
    return Cache.values().containsKey(this);
  }

  default void cache(R value) {
    Cache.values().put(this, value);
  }

  default R cached() {
    return (R)Cache.values().get(this);
  }
}
