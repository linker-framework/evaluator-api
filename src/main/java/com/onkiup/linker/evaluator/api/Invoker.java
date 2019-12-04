package com.onkiup.linker.evaluator.api;

import com.onkiup.linker.parser.Extension;
import com.onkiup.linker.parser.Rule;

public interface Invoker<X extends Rule, R> extends Extension<X>  {
  R invoke(Evaluator... arguments);
}
