package com.onkiup.linker.evaluator.utils;

import com.onkiup.linker.evaluator.common.EvaluationError;
import com.onkiup.linker.parser.Extension;
import com.onkiup.linker.parser.ParserLocation;
import com.onkiup.linker.parser.Rule;

public class NoContextPresent extends EvaluationError {
  public NoContextPresent() {
    super("No evaluation context is present");
  }

  public NoContextPresent(String message) {
    super(message);
  }

  public NoContextPresent(String message, Throwable cause) {
    super(message, cause);
  }

  public NoContextPresent(
      ParserLocation location, String message, Throwable cause) {
    super(location, message, cause);
  }

  public NoContextPresent(Rule source, String message, Throwable cause) {
    super(source, message, cause);
  }

  public NoContextPresent(Extension source, String message, Throwable cause) {
    super(source, message, cause);
  }

  public NoContextPresent(ParserLocation location, String message) {
    super(location, message);
  }

  public NoContextPresent(Rule source, String message) {
    super(source, message);
  }

  public NoContextPresent(Extension source, String message) {
    super(source, message);
  }
}
