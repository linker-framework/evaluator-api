package com.onkiup.linker.evaluator.common;

import java.lang.reflect.Array;

import com.onkiup.linker.evaluator.api.Cacheable;
import com.onkiup.linker.evaluator.api.Connector;
import com.onkiup.linker.evaluator.api.Evaluator;
import com.onkiup.linker.evaluator.api.Invoker;
import com.onkiup.linker.evaluator.api.RuleEvaluator;
import com.onkiup.linker.parser.Extension;
import com.onkiup.linker.parser.Rule;
import com.onkiup.linker.util.TypeUtils;

/**
 * A class that invokes FFI connectors
 */
public class ConnectorInvoker<T, R> implements Invoker<R>, Extension<Connector<T, R>> {

  @Override
  public R execute(Evaluator... arguments) {
    Connector<T, R> connector = base();

    Class[] targetArgumentTypes = connector.parameters();
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
        if (Evaluator.class.isAssignableFrom(targetArgumentType)) {
          argument = arguments[i];
        } else {
          argument = TypeUtils.convert(targetArgumentType, arguments[i].value());
        }
      }

      targetArguments[i] = argument;
    }

    try {
      if (connector instanceof Cacheable && ((Cacheable)connector).present()) {
        return ((Cacheable<R>)connector).cached();
      }

      return base().invoke(targetArguments);
    } catch (Exception e) {
      throw new EvaluationError(this, "Failed to invoke connector '"+connector+"'", e);
    }
  }

  @Override
  public R invoke(Evaluator... arguments) {
    return execute(arguments);
  }

  private Object collectVarArg(Class targetType, Evaluator[] arguments, int position) {
    if (arguments.length < position) {
      throw new IllegalArgumentException("Position("+position+") is outside of argument array("+ arguments.length+")");
    }

    Class componentType = targetType.getComponentType();
    Object result = Array.newInstance(componentType, arguments.length - position);
    for (int i = position; i < arguments.length; i++) {
      Evaluator argument = arguments[i];
      Object targetArgument = null;
      if (argument != null) {
        if (Evaluator.class.isAssignableFrom(targetType.getComponentType())) {
          targetArgument = argument;
        } else if (Rule.class.isAssignableFrom(componentType) && argument instanceof RuleEvaluator && componentType.isInstance(((RuleEvaluator)argument).base())) {
          targetArgument = ((RuleEvaluator)argument).base();
        } else {
          targetArgument = TypeUtils.convert(targetType.getComponentType(), argument.value());
        }
      }
      Array.set(result, i - position, targetArgument);
    }
    return result;
  }
}

