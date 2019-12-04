package com.onkiup.linker.evaluator.api;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import com.onkiup.linker.evaluator.api.Evaluator;
import com.onkiup.linker.parser.token.PartialToken;
import com.onkiup.linker.util.SafeConsumer;
import com.onkiup.linker.util.SafeFunction;

public interface EvaluationContext extends AutoCloseable {

  /**
   * Returns stored value by its key
   * @param key lookup key
   * @return optional with stored value or empty optional if no such key present
   */
  Optional<?> resolve(String key);

  /**
   * Stores a value and associates it with the key
   * @param key lookup key for the value
   * @param value the value to store
   * @param modifiable if true, then this value should appear as constant to the evaluatee
   * @param override instructs the context to override previously stored value even if it was marked as constant
   */
  void store(String key, Object value, boolean modifiable, boolean override);

  /**
   * Returns all stored values
   * @return a map with keys and values stored in the context
   */
  Map<String, Object> getMembers();

  /**
   * @return optional of parent context (empty optional for root context)
   */
  Optional<EvaluationContext> parent();

  /**
   * @return Rule Evaluator that created this context
   */
  Evaluator owner();

  /**
   * @return Rule Evaluator that injected this context into the stack
   */
  Invoker invoker();

  void parent(EvaluationContext context);
  boolean containsKey(String key);

  default void store(String key, Object value) {
    store(key, value, true, false);
  }

  default void store(String key, Object value, boolean modifiable) {
    store(key, value, modifiable, false);
  }

  default void override(String key, Object value, boolean modifiable) {
    store(key, value, modifiable, true);
  }

  default void subcontext(SafeConsumer<EvaluationContext> code) {
    try(EvaluationContext sub = new MemoryContext(this)) {
      EvaluationContext.push(sub);
      code.accept(sub);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  default  <X> X subcontext(SafeFunction<EvaluationContext, X> code) {
    try (EvaluationContext sub = new MemoryContext(this)) {
      EvaluationContext.push(sub);
      return code.apply(sub);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  // --------------------------------------
  // Static members and values
  // --------------------------------------

  class Values {
    private static final ThreadLocal<Deque<EvaluationContext>> stack = new ThreadLocal<>();
    private static final ThreadLocal<Evaluator<?, ?>> currentToken = new ThreadLocal<>();
  }

  static Deque<EvaluationContext> stack() {
    if (Values.stack.get() == null) {
      Values.stack.set(new LinkedList<>());
    }
    return Values.stack.get();
  }

  static void push(EvaluationContext context) {
    stack().push(context);
  }

  static EvaluationContext get() {
    try {
      return stack().peek();
    } catch (Exception e) {
      throw new EvaluationError("No context is present", e);
    }
  }

  static EvaluationContext pop() {
    try {
      return EvaluationContext.stack().pop();
    } catch (Exception e) {
      throw new EvaluationError("Failed to pop context", e);
    }
  }


  static Evaluator<?, ?> currentToken() {
    return Values.currentToken.get();
  }

  static void currentToken(Evaluator<?, ?> token) {
    Values.currentToken.set(token);
  }

  static <X> X newContext(SafeFunction<EvaluationContext, X> code) {
    return EvaluationContext.get().subcontext(code);
  }

  static void newContext(SafeConsumer<EvaluationContext> code) {
    EvaluationContext.get().subcontext(code);
  }

  default StackTraceElement asStackTraceElement() {
    Evaluator<?, ?> owner = owner();
    Invoker<?, ?> invoker = invoker();
    if (owner == null) {
      return null;
    } else {
      try {
        PartialToken ownerMeta = owner.metadata().get();
        PartialToken invokerMeta = invoker.metadata().get();
        return new StackTraceElement(owner.getClass().getSimpleName(), "..", meta.location().name(),
            meta.location().line());
      } catch (Exception e) {
        return new StackTraceElement(owner.getClass().getSimpleName(), "..", "??", 0);
      }
    }
  }
}
