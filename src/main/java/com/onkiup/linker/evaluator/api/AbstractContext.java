package com.onkiup.linker.evaluator.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.onkiup.linker.evaluator.api.Evaluator;

public abstract class AbstractContext implements EvaluationContext {

  private EvaluationContext parent;
  private Evaluator owner;
  private Evaluator creator;
  private final Map<String, Object> values = new ConcurrentHashMap<>();
  private final Map<String, Object> constants = new ConcurrentHashMap<>();

  public AbstractContext(EvaluationContext parent, Evaluator owner) {
    this(parent, owner, (Evaluator)null);
  }
  
  public AbstractContext(EvaluationContext parent, Evaluator owner, Evaluator creator) {
    this.parent = parent;
    this.owner = owner;
    this.creator = creator;
  }

  @Override
  public Evaluator owner() {
    return owner;
  }

  @Override
  public Evaluator creator() {
    return creator;
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
        throw new EvaluationError(creator(), "Unable to override constant `" + key + "`");
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
      throw new EvaluationError(creator(), "Context corruption detected -- popped " + all.size() + " contexts, none of them was " + this, nsee);
    }
  }

}
