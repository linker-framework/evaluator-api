package com.onkiup.linker.evaluator.api;

import com.onkiup.linker.util.TypeUtils;

/**
 * Common interface for language element invokers. Invoker instances are intended to be a universal interface
 * between evaluator loop and invokable language structures
 *
 * A note on SAIL weirdness: SAIL (the first language implemented for Linker-*, supports a weird operation that
 * can be described as "named parameter resolving", which is different from more traditional positional parameter
 * resolving operation (also supported by SAIL). In order to support this, all named arguments need to be stored
 * in a subcontext of invocable's creation context and then passed to the invoker as positional arguments.
 *
 * @param <X> the type of language elements that this invoker can handle
 * @param <R> expected invocation return type
 */
public interface Invoker<R>  {

  R execute(Evaluator... parameters);

  /**
   * Invokes language structure with given arguments
   * @param arguments arguments to be passed to the structure
   * @return invocation result as defined by the structure
   */
  default R invoke(Evaluator... arguments) {
    if (this instanceof Cacheable && ((Cacheable)this).present()) {
      return (R)((Cacheable)this).cached();
    }
    return execute(arguments);
  }

  default Class<R> resultType() {
    return (Class<R>)TypeUtils.typeParameter(getClass(), Invoker.class, 0);
  }
}
