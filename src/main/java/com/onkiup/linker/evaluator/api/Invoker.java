package com.onkiup.linker.evaluator.api;

import com.onkiup.linker.parser.Extension;
import com.onkiup.linker.parser.Rule;

/**
 * Common interface for language element invokers. Invoker instances are intended to be a universal interface
 * between evaluator loop and invokable language structures
 * @param <X> the type of language elements that this invoker can handle
 * @param <R> expected invocation return type
 */
public interface Invoker<X extends Rule, R> extends Extension<X>  {
  /**
   * Invokes language structure with given arguments
   * @param arguments arguments to be passed to the structure
   * @return invocation result as defined by the structure
   */
  R invoke(Evaluator... arguments);
}
