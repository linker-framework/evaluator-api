package com.onkiup.linker.evaluator.api;

import com.onkiup.linker.evaluator.common.EvaluationError;

/**
 * An extension to Evaluator interface that allows to pass named arguments into Invokers
 * @param <I> identity type
 */
public interface Reference<I, O> extends Evaluator<O> {
  I identity();

  @Override
  default O evaluate() {
    return EvaluationContext.resolve(this).orElseThrow(() -> new EvaluationError("Unable to resolve reference " + this));
  }
}
