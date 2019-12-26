package com.onkiup.linker.evaluator.api;

import com.onkiup.linker.parser.Extension;
import com.onkiup.linker.parser.Rule;

/**
 * An evaluator for grammar rules
 * @param <X> type of rules that this evaluator can handle
 * @param <O> expected evaluation result type
 */
public interface RuleEvaluator<X extends Rule, O> extends Evaluator<O>, Extension<X> {
}
