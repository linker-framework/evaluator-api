package com.onkiup.linker.evaluator.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onkiup.linker.evaluator.api.Evaluator;
import com.onkiup.linker.parser.Rule;

public class MemoryContext extends AbstractContext {
  private static final Logger logger = LoggerFactory.getLogger(MemoryContext.class);

  private final ConcurrentHashMap<String, Object> values = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Object> constants = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Collection<Consumer>> listeners = new ConcurrentHashMap<>();

  public MemoryContext(EvaluationContext parent, Evaluator owner) {
    super(parent, owner);
  }

  public MemoryContext(EvaluationContext parent) {
    super(parent, EvaluationContext.currentToken());
  }

  public MemoryContext() {
    super(null, null);
  }

  @Override
  public Optional<?> resolve(String key) {
    Object result;
    key = key.toLowerCase();
    logger.debug("Resolving context key '{}'", key);
    if (constants.containsKey(key)) {
      result = constants.get(key);
      logger.debug("Resolved '{}' as constant {}", key, result);
      return Optional.ofNullable(result);
    }

    if (values.containsKey(key)) {
      result = values.get(key);
      logger.debug("Resolved '{}' as value {}", key, result);
      return Optional.ofNullable(result);
    }

    String finalKey = key;
    return parent().flatMap(p -> {
      logger.debug("Asking parent {} to resolve {}", p, finalKey);
      return p.resolve(finalKey);
    });
  }

  @Override
  public void store(String key, Object value, boolean modifiable, boolean override) {
    key = key.toLowerCase();
    ConcurrentHashMap<String, Object> target = modifiable ? values : constants;

    if (constants.containsKey(key) && !override) {
      throw new EvaluationError(creator(), "Unable to override existing context constant '" + key + "'");
    }

    if (value == null) {
      target.remove(key);
    } else {
       target.put(key, value);
    }
    if (listeners.containsKey(key)) {
      // notifying all listeners about the change
      listeners.get(key).forEach(listener -> listener.accept(value));
    }
  }

  public void registerMethod(String name, Class definer, String method) {
    try {
      store(name, definer.getDeclaredMethod(method));
    } catch (Exception e) {
      throw new RuntimeException("Failed to register method " +
          definer.getName() + "::" + method + " as " + name, e);
    }
  }

  @Override
  public Map<String, Object> getMembers() {
    Map<String, Object> result = new HashMap<>();
    parent().map(EvaluationContext::getMembers).ifPresent(result::putAll);
    result.putAll(values);
    result.putAll(constants);
    return result;
  }

}

