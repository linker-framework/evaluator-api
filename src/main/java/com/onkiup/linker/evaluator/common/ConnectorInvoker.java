package com.onkiup.linker.evaluator.common;

import java.lang.reflect.Array;

import com.onkiup.linker.evaluator.api.EvaluationError;
import com.onkiup.linker.evaluator.api.Evaluator;
import com.onkiup.linker.evaluator.api.Invoker;
import com.onkiup.linker.evaluator.api.Connector;

/**
 * A class that invokes FFI connectors
 */
public class ConnectorInvoker<T, R> implements Invoker<Connector<T, R>, R> {

  @Override
  public R invoke(Evaluator... arguments) {
    Connector<T, R> connector = base();

    Class[] targetArgumentTypes = connector.arguments();
    Object[] targetArguments = new Object[targetArgumentTypes.length];

    int last = targetArgumentTypes.length - 1;
    for (int i = 0; i < targetArgumentTypes.length; i++) {
      Class targetArgumentType = targetArgumentTypes[i];
      Object argument = null;
      if (i >= arguments.length) {
        if (i == arguments.length && targetArgumentType.isArray()) {
          argument = Array.newInstance(targetArgumentType.getComponentType(), 0);
        } else {
          throw new IllegalStateException("Not enough arguments provided to invoke '" + connector + "'");
        }
      } else if (i == last && arguments.length> i + 1) {
        if (targetArgumentType.isArray()) {
          argument = collectVarArg(targetArgumentType, arguments, i);
        } else {
          throw new IllegalStateException("Too many arguments provided to invoke '"+ connector+"'");
        }
      } else if (arguments[i] != null) {
        argument = arguments[i].to(targetArgumentType);
      }

      targetArguments[i] = argument;
    }

    try {
      return (R) connector.invoke(targetArguments);
    } catch (Exception e) {
      throw new EvaluationError(this, "Failed to invoke connector '"+connector+"'", e);
    }
  }

  private Object collectVarArg(Class targetType, Evaluator[] arguments, int position) {
    if (arguments.length < position) {
      throw new IllegalArgumentException("Position("+position+") is outside of argument array("+ arguments.length+")");
    }

    Object result = Array.newInstance(targetType.getComponentType(), arguments.length - position);
    for (int i = position; i < arguments.length; i++) {
      Evaluator argument = arguments[i];
      Object targetArgument = null;
      if (argument != null) {
        targetArgument = argument.to(targetType);
      }
      Array.set(result, i - position, targetArgument);
    }
    return result;
  }
}

