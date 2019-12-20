package com.onkiup.linker.evaluator.api;

import com.onkiup.linker.parser.Extension;
import com.onkiup.linker.parser.Rule;

/**
 * Common interface for language element evaluators
 * @param <X> type of language elements to which this evaluator can be applied
 * @param <O> expected type for evaluation result
 */
public interface Evaluator<X extends Rule, O> extends Extension<X> {
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
}
