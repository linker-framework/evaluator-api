package com.onkiup.linker.evaluator.api;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.onkiup.linker.evaluator.common.EvaluationError;
import com.onkiup.linker.evaluator.common.MemoryContext;
import com.onkiup.linker.evaluator.utils.ContextIterator;
import com.onkiup.linker.parser.token.PartialToken;
import com.onkiup.linker.util.SafeConsumer;
import com.onkiup.linker.util.SafeFunction;
import com.onkiup.linker.util.TypeUtils;

/**
 * Common interface for all linker connectors
 * @param <I> supported identity type
 */
public interface EvaluationContext<I> extends AutoCloseable, Iterable<EvaluationContext<?>> {

  /**
   * Returns stored value by its key
   * @param key lookup key
   * @return optional with stored value or empty optional if no such key present
   */
  Optional<?> resolve(I key);

  static <I, O> Optional<O> resolve(Reference<I, O> key) {
    Class referenceIdentityType = TypeUtils.typeParameter(key.getClass(), Reference.class, 0);
    EvaluationContext<?> context = EvaluationContext.get();
    Class contextIdentityType = context.identityType();
    while (!contextIdentityType.isAssignableFrom(referenceIdentityType)) {
      context = context.parent().orElseThrow(() -> new EvaluationError("Unable to resolve reference " + key + ": unsupported identity type"));
      contextIdentityType = context.identityType();
    }

    return (Optional<O>) ((EvaluationContext<I>)context).resolve(key.identity());
  }

  /**
   * Stores a value and associates it with the key
   * @param key lookup key for the value
   * @param value the value to store
   * @param modifiable if true, then this value should appear as constant to the evaluatee
   * @param override instructs the context to override previously stored value even if it was marked as constant
   */
  void store(I key, Object value, boolean modifiable, boolean override);

  /**
   * Returns all stored in this context values
   * @return a map with keys and values stored in the context
   */
  Map<I, Object> members();

  static Map<Object, Object> allMembers() {
    Map result = new HashMap();
    EvaluationContext.get().trace()
        .map(EvaluationContext::members)
        .forEach(result::putAll);

    return result;
  }

  /**
   * @return optional of parent context (empty optional for root context)
   */
  Optional<EvaluationContext<?>> parent();

  default Stream<EvaluationContext<?>> trace() {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED + Spliterator.DISTINCT), false);
  }

  /**
   * @return Rule Evaluator that created this context
   */
  Optional<RuleEvaluator<?, ?>> owner();

  void parent(EvaluationContext context);

  boolean containsKey(I key);

  default void store(I key, Object value) {
    store(key, value, true, false);
  }

  default void store(I key, Object value, boolean modifiable) {
    store(key, value, modifiable, false);
  }

  default void override(I key, Object value, boolean modifiable) {
    store(key, value, modifiable, true);
  }

  default <I> void subcontext(Class<I> keyType, SafeConsumer<EvaluationContext<I>> code) {
    try(EvaluationContext<I> sub = new MemoryContext<>(this)) {
      EvaluationContext.push(sub);
      code.accept(sub);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static <I> void isolated(Class<I> keyType, SafeConsumer<EvaluationContext<I>> code)  {
    EvaluationContext.get().subcontext(keyType, code);
  }

  default <I, X> X subcontext(Class<I> keyType, SafeFunction<EvaluationContext<I>, X> code) {
    try (EvaluationContext<I> sub = new MemoryContext<>(this)) {
      EvaluationContext.push(sub);
      return code.apply(sub);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static <I, X> X isolated(Class<I> keyType, SafeFunction<EvaluationContext<I>, X> code) {
    return EvaluationContext.get().subcontext(keyType, code);
  }

  // --------------------------------------
  // Static members and values
  // --------------------------------------

  class Values {
    private static final ThreadLocal<Deque<EvaluationContext<?>>> stack = new ThreadLocal<>();
    private static final ThreadLocal<RuleEvaluator<?, ?>> currentToken = new ThreadLocal<>();
  }

  static Deque<EvaluationContext<?>> stack() {
    if (Values.stack.get() == null) {
      Values.stack.set(new LinkedList<>());
    }
    return Values.stack.get();
  }

  static void push(EvaluationContext<?> context) {
    stack().push(context);
  }

  static EvaluationContext<?> get() {
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


  static RuleEvaluator<?, ?> currentToken() {
    return Values.currentToken.get();
  }

  static void currentToken(RuleEvaluator<?, ?> token) {
    Values.currentToken.set(token);
  }

  static <I, X> X newContext(Class<I> keyType, SafeFunction<EvaluationContext<I>, X> code) {
    return EvaluationContext.get().subcontext(keyType, code);
  }

  static <I> void newContext(Class<I> keyType, SafeConsumer<EvaluationContext<I>> code) {
    EvaluationContext.get().subcontext(keyType, code);
  }

  default StackTraceElement asStackTraceElement() {
    RuleEvaluator<?, ?> owner = owner().orElse(null);
    RuleEvaluator<?, ?> parentOwner = parent().flatMap(EvaluationContext::owner).orElse(null);
    if (owner == null || parentOwner == null) {
      return null;
    } else {
      try {
        PartialToken<?> ownerMeta = owner.metadata().get();
        PartialToken<?> parentMeta = parentOwner.metadata().get();
        // TODO: validate the stacktraces, write docs on them and adjust this accordingly
        return new StackTraceElement(ownerMeta.location().name(), "..", parentMeta.location().name(), parentMeta.location().line());
      } catch (Exception e) {
        return new StackTraceElement(owner.getClass().getSimpleName(), "..", "??", 0);
      }
    }
  }

  default Class<I> identityType() {
    return (Class<I>)TypeUtils.typeParameter(getClass(), EvaluationContext.class, 0);
  }

  /**
   * Returns an iterator over elements of type {@code T}.
   *
   * @return an Iterator.
   */
  @Override
  default Iterator<EvaluationContext<?>> iterator() {
    return new ContextIterator(this);
  }

  default Class keyType() {
    return TypeUtils.typeParameter(getClass(), EvaluationContext.class, 0);
  }
}
