package com.onkiup.linker.evaluator.common;

import com.onkiup.linker.evaluator.api.EvaluationContext;
import com.onkiup.linker.evaluator.api.Reference;
import com.onkiup.linker.evaluator.api.RuleEvaluator;

public class ParametrizedContext<K> extends MemoryContext<K> {
  private Reference<K, Class>[] parameters;

  public ParametrizedContext(EvaluationContext parent, RuleEvaluator<?, ?> owner, Reference<K, Class>[] parameters) {
    super(parent, owner);
    this.parameters = parameters;
  }
}
