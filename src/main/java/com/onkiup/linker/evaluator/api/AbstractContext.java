package com.onkiup.linker.evaluator.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Common features for evaluation contexts
 */
public abstract class AbstractContext implements EvaluationContext {

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
  private Evaluator owner;
  /**
   * Creator token represents a language construct that caused
   * creation of this context, i.g. a function invocation
   */
  private Invoker invoker;
  /**
   * Contains modifiable context values (variables)
   */
  private final Map<String, Object> values = new ConcurrentHashMap<>();
  /**
   * Contains unmodifiable context values (constants)
   */
  private final Map<String, Object> constants = new ConcurrentHashMap<>();

  public AbstractContext(EvaluationContext parent) {
    this(parent, null, null);
  }

  public AbstractContext(EvaluationContext parent, Evaluator owner) {
    this(parent, owner, null);
  }
  
  public AbstractContext(EvaluationContext parent, Evaluator owner, Invoker invoker) {
    this.parent = parent;
    this.owner = owner;
    this.invoker = invoker;
  }

  @Override
  public Optional<Evaluator> owner() {
    return Optional.ofNullable(owner);
  }

  @Override
  public Optional<Invoker> invoker() {
    return Optional.ofNullable(invoker);
  }

  @Override
  public Optional<EvaluationContext> parent() {
    return Optional.ofNullable(parent);
  }

  @Override
  public void parent(EvaluationContext parent) {
    this.parent = parent;
  }

  @Override
  public Optional<?> resolve(String key) {
    if (values.containsKey(key)) {
      return Optional.ofNullable(values.get(key));
    } else if (constants.containsKey(key)) {
      return Optional.ofNullable(constants.get(key));
    }
    return parent().flatMap(p -> p.resolve(key));
  }

  @Override
  public boolean containsKey(String key) {
    return values.containsKey(key) || constants.containsKey(key);
  }

  @Override
  public void store(String key, Object value, boolean modifiable, boolean override) {
    if (constants.containsKey(key)) {
      if (!override) {
        throw new EvaluationError(invoker().orElse(null), "Unable to override constant `" + key + "`");
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
  public Map<String,Object> getMembers() {
    HashMap<String, Object> result = new HashMap<>();
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
      throw new EvaluationError(
          invoker().orElse(null), "Context corruption detected -- popped " + all.size() + " contexts, none of them was " + this, nsee);
    }
  }

}
