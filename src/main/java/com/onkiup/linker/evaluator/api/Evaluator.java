package com.onkiup.linker.evaluator.api;

import com.onkiup.linker.util.TypeUtils;

/**
 * Common interface for representing operands
 * @param <O> expected type for evaluation result
 */
public interface Evaluator<O> {
  /**
   * A stub method for evaluator implementations
   * @return evaluation result
   */
  O evaluate();

  /**
   * Evaluation entry point. Can return cached results instead of performing actual evaluation
   * @return evaluation result
   */
  default O value() {
    return evaluate();
  }

  default Class<O> resultType() {
    return (Class<O>)TypeUtils.typeParameter(getClass(), Evaluator.class, 0);
  }
}
