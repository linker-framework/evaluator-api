package com.onkiup.linker.evaluator.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onkiup.linker.evaluator.api.EvaluationContext;
import com.onkiup.linker.evaluator.api.RuleEvaluator;

/**
 * Evaluation context that stores its members in memory
 */
public class MemoryContext<I> extends AbstractContext<I> {
  private static final Logger logger = LoggerFactory.getLogger(MemoryContext.class);

  private final ConcurrentHashMap<I, Object> values = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<I, Object> constants = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<I, Collection<Consumer>> listeners = new ConcurrentHashMap<>();

  public MemoryContext(EvaluationContext parent, RuleEvaluator owner) {
    super(parent, owner);
  }

  public MemoryContext(EvaluationContext parent, Map<I, Object> constants) {
    this(parent);
    this.constants.putAll(constants);
  }

  public MemoryContext(EvaluationContext parent) {
    super(parent, EvaluationContext.currentToken());
  }

  public MemoryContext() {
    super(null, null);
  }

  @Override
  public Optional<?> resolveLocally(I key) {
    Object result;
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

    return Optional.empty();
  }

  @Override
  public void store(I key, Object value, boolean modifiable, boolean override) {
    ConcurrentHashMap<I, Object> target = modifiable ? values : constants;

    if (constants.containsKey(key) && !override) {
      throw new EvaluationError("Unable to override existing context constant '" + key + "'");
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

  public void registerMethod(I name, Class definer, String method) {
    try {
      store(name, definer.getDeclaredMethod(method));
    } catch (Exception e) {
      throw new RuntimeException("Failed to register method " +
          definer.getName() + "::" + method + " as " + name, e);
    }
  }

  @Override
  public Map<I, Object> members() {
    Map<I, Object> result = new HashMap<>();
    result.putAll(values);
    result.putAll(constants);
    return result;
  }

}

