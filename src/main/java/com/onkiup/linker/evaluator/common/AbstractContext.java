package com.onkiup.linker.evaluator.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.onkiup.linker.evaluator.api.EvaluationContext;
import com.onkiup.linker.evaluator.api.RuleEvaluator;

/**
 * Common features for evaluation contexts
 */
public abstract class AbstractContext<I> implements EvaluationContext<I> {

  /**
   * Parent context is a context in which this context was created
   * and it is used to delegate variable resolution for variables
   * external to this context
   */
  private EvaluationContext parent;
  /**
   * Owner token represents a language construct that is associated
   * with this context, i.g. a function definition
   */
  private RuleEvaluator owner;

  /**
   * Contains modifiable context values (variables)
   */
  private final Map<I, Object> values = new ConcurrentHashMap<>();
  /**
   * Contains unmodifiable context values (constants)
   */
  private final Map<I, Object> constants = new ConcurrentHashMap<>();

  public AbstractContext(EvaluationContext parent) {
    this(parent, null);
  }

  public AbstractContext(EvaluationContext parent, RuleEvaluator owner) {
    this.parent = parent;
    this.owner = owner;
  }

  @Override
  public Optional<RuleEvaluator<?, ?>> owner() {
    return Optional.ofNullable(owner);
  }

  @Override
  public Optional<EvaluationContext<?>> parent() {
    return Optional.ofNullable(parent);
  }

  @Override
  public void parent(EvaluationContext parent) {
    this.parent = parent;
  }

  public Optional<?> resolveLocally(I key) {
    if (values.containsKey(key)) {
      return Optional.ofNullable(values.get(key));
    } else if (constants.containsKey(key)) {
      return Optional.ofNullable(constants.get(key));
    }
    return Optional.empty();
  }

  @Override
  public final Optional<?> resolve(I key) {
    Optional<?> result = resolveLocally(key);
    if (!result.isPresent()) {
      Class keyType = keyType();

      result = trace()
          .filter(parent -> parent != this)
          .filter(parent -> parent.keyType().isAssignableFrom(keyType))
          .findFirst()
          .flatMap(parent -> ((EvaluationContext<I>)parent).resolve(key));
    }

    return result;
  }

  @Override
  public boolean containsKey(I key) {
    return values.containsKey(key) || constants.containsKey(key);
  }

  @Override
  public void store(I key, Object value, boolean modifiable, boolean override) {
    if (constants.containsKey(key)) {
      if (!override) {
        throw new EvaluationError("Unable to override constant `" + key + "`");
      } else if (modifiable) {
        constants.remove(key);
      }
    }

    if (!modifiable) {
      constants.put(key, value);
    } else {
      values.put(key, value);
    }
  }

  @Override
  public void remove(I key, boolean c) {
    if (c) {
      constants.remove(key);
    }
    values.remove(key);
  }

  @Override
  public Map<I,Object> members() {
    HashMap<I, Object> result = new HashMap<>();
    result.putAll(values);
    result.putAll(constants);
    return result;
  }

  @Override
  public void close() {
    List<EvaluationContext> all = new ArrayList<>();
    EvaluationContext popped;
    try {
      do {
        popped = EvaluationContext.pop();
        all.add(popped);
      } while (popped != null && popped != this);
    } catch (NoSuchElementException nsee) {
      all.forEach(EvaluationContext::push);
      throw new EvaluationError("Context corruption detected -- popped " + all.size() + " contexts, none of them was " + this, nsee);
    }
  }

}
