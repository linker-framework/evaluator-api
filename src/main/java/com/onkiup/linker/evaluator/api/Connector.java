package com.onkiup.linker.evaluator.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.onkiup.linker.parser.NonParseable;
import com.onkiup.linker.parser.ParserContext;
import com.onkiup.linker.parser.TokenGrammar;
import com.onkiup.linker.util.TypeUtils;

/**
 * Foreign Function Interface connector 
 * Used by Invokers to invoke non-rule based functions
 * A connected object (usually host language method) should be put into the context, after which
 * evaluator would transform it into target argument by doing something like:
 * Invoker connectorInvoker = Connector.to(connectedMethod).as(Invoker.class);
 */
public interface Connector<X, R> extends NonParseable {

  /**
   * Holds references to all defined connectors
   */
  class Metadata {
    /**
     * Connector definitions
     */
    private static Map<Class, Class<? extends Connector>> connectorTypes = null;
    /**
     * Connector instances per objects they connect to
     */
    private static final Map<Object, Connector> connectors = Collections.synchronizedMap(new HashMap<>());
    /**
     * Connected objects per their connectors
     */
    private static final Map<Connector, Object> connected = Collections.synchronizedMap(new HashMap<>());

    /**
     * Resolves connectors according to their targets
     * @param target target object for which this method should return a connector
     * @param <X> type of the target
     * @param <R> type of the connector
     * @return existing or new connector that can handle the given target
     */
    static <X, R> Connector<X, R> connector(X target) {
      if (target == null) {
        return null;
      }

      synchronized(Connector.class) {
        if (connectorTypes == null) {
          connectorTypes = ParserContext.get().subClasses(Connector.class)
            .filter(TokenGrammar::isConcrete)
            .collect(Collectors.toMap(
                 connector -> TypeUtils.typeParameter(connector, Connector.class, 0),
                  connector -> connector
            ));
        }
      }

      synchronized(connectors) {
        if (!connectors.containsKey(target)) {
          if (!connectorTypes.containsKey(target.getClass())) {
            throw new IllegalStateException("Unable to connect '" + target.getClass().getCanonicalName() + "' -- no connectors found");
          }
          Class connectorType = connectorTypes.get(target.getClass());

          try {
            synchronized(connected) {
              Connector<X, R> result = (Connector<X, R>) connectorType.newInstance();
              connectors.put(target, result);
              connected.put(result, target);
              return result;
            }
          } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate connector '"+ connectorType.getCanonicalName() + "'", e);
          }
        }
        return connectors.get(target);
      }
    }
  }

  /**
   * Resolves connectors according to their targets
   * @param target target object for which this method should return a connector
   * @param <X> type of the target
   * @param <R> type of the connector
   * @return existing or new connector that can handle the given target
   */
  public static <X, R> Connector<X, R> to(X target) {
    return Metadata.connector(target);
  }

  /**
   * @return the connected object
   */
  default X base() {
    synchronized(Metadata.connected) {
      return (X) Metadata.connected.get(this);
    }
  }

  /**
   * @return types of arguments accepted by connected method with varargs represented as last argument of array type
   */
  Class[] arguments();

  /**
   * @return the return type of the connected method 
   */
  Class<R> returnType();

  /**
   * Invokes connected method
   * @param arguments arguments to pass to connected method
   * @return return value of the connecteed method 
   */
  R invoke(Object... arguments);

}
