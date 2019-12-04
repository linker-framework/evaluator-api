package com.onkiup.linker.evaluator.api;

import com.onkiup.linker.parser.Extension;
import com.onkiup.linker.parser.Rule;

public interface Lister<I extends Rule, O> extends Evaluator<I, O[]> {
}

