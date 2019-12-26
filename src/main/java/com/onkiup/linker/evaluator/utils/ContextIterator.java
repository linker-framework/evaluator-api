package com.onkiup.linker.evaluator.utils;

import java.util.Iterator;

import com.onkiup.linker.evaluator.api.EvaluationContext;

public class ContextIterator implements Iterator<EvaluationContext<?>> {

  private EvaluationContext<?> context;

  public ContextIterator(EvaluationContext<?> source) {
    this.context = source;
  }

  /**
   * Returns {@code true} if the iteration has more elements.
   * (In other words, returns {@code true} if {@link #next} would
   * return an element rather than throwing an exception.)
   *
   * @return {@code true} if the iteration has more elements
   */
  @Override
  public boolean hasNext() {
    return context != null;
  }

  /**
   * Returns the next element in the iteration.
   *
   * @return the next element in the iteration
   * @throws NoSuchElementException if the iteration has no more elements
   */
  @Override
  public EvaluationContext<?> next() {
    EvaluationContext<?> result = context;
    context = context.parent().orElse(null);
    return result;
  }
}
