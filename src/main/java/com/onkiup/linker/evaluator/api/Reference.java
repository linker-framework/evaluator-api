package com.onkiup.linker.evaluator.api;

import java.util.Optional;

import com.onkiup.linker.evaluator.common.EvaluationError;
import com.onkiup.linker.evaluator.utils.NoContextPresent;
import com.onkiup.linker.parser.NonParseable;

/**
 * An extension to Evaluator interface that allows to pass named arguments into Invokers
 * @param <I> identity type
 */
public interface Reference<I, O> extends Evaluator<O>, NonParseable {
  I identity();

  default Optional<O> resolve() throws NoContextPresent {
    return EvaluationContext.resolve(this);
  }

  @Override
  default Class<O> resultType() {
    Class<O> result = (Class<O>)resolve().map(Object::getClass).orElse(null);
    if (result == null) {
      return (Class<O>)Object.class;
    }
    return result;
  }

  @Override
  default O evaluate() {
    return EvaluationContext.resolve(this).orElseThrow(() -> new EvaluationError("Unable to resolve reference " + identity()));
  }

  static <I> Reference<I, ?> to(I identity) {
    return new Impl(identity);
  }

  public static final class Impl<I, O> implements Reference<I, O> {
    private final I identity;

    public Impl(I identity) {
      this.identity = identity;
    }

    @Override
    public I identity() {
      return identity;
    }

    @Override
    public Class<O> resultType() {
      return null;
    }
  }
}
