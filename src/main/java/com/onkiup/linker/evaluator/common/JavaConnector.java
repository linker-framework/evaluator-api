package com.onkiup.linker.evaluator.common;

import java.lang.reflect.Method;
import com.onkiup.linker.evaluator.api.Connector;

/**
 * FFI connector to Java methods
 */
public class JavaConnector<R> implements Connector<Method, R> {

  @Override
  public Class[] arguments() {
    Method base = base();
    return base.getParameterTypes();
  }

  @Override
  public R invoke(Object[] arguments) {
    Method target = base();
    try {
      return (R) target.invoke(null, arguments);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Class<R> returnType() {
    return (Class<R>) base().getReturnType();
  }
}

