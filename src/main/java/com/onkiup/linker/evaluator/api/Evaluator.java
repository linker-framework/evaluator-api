package com.onkiup.linker.evaluator.api;

import com.onkiup.linker.parser.Extension;
import com.onkiup.linker.parser.Rule;

public interface Evaluator<X extends Rule, O> extends Extension<X> {
  O evaluate();
  default O value() {
    return evaluate();
  }
}
