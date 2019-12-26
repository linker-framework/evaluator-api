package com.onkiup.linker.evaluator.api;

import com.onkiup.linker.parser.Extension;
import com.onkiup.linker.parser.Rule;

/**
 * An interface for AST invokers
 */
public interface RuleInvoker<R extends Rule, O> extends Invoker<O>, Extension<R> {
}
