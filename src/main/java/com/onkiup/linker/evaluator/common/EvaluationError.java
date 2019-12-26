package com.onkiup.linker.evaluator.common;

import com.onkiup.linker.evaluator.api.EvaluationContext;
import com.onkiup.linker.parser.Extension;
import com.onkiup.linker.parser.ParserLocation;
import com.onkiup.linker.parser.Rule;

/**
 * Exception wrapper for evaluation-time errors
 */
public class EvaluationError extends RuntimeException {

  /**
   * Locaion in evaluated code that caused this exception
   */
  private ParserLocation location;

  public EvaluationError(String message) {
    this(message, null);
  }

  public EvaluationError(String message, Throwable cause) {
    this(EvaluationContext.currentToken().location(), message, cause);
  }

  public EvaluationError(ParserLocation location, String message, Throwable cause) {
    super(message, cause);
    this.location = location;
  }

  public EvaluationError(Rule source, String message, Throwable cause) {
    this(source == null ? ParserLocation.ZERO : source.location(), message, cause);
  }

  public EvaluationError(Extension source, String message, Throwable cause) {
    this(source == null ? ParserLocation.ZERO : source.base().location(), message, cause);
  }

  public EvaluationError(ParserLocation location, String message) {
    super(message);
    this.location = location;
  }

  public EvaluationError(Rule source, String message) {
    this(source == null ? ParserLocation.ZERO : source.location(), message);
  }

  public EvaluationError(Extension source, String message) {
    this(source == null ? ParserLocation.ZERO : source.base().location(), message);
  }

  /**
   * @return location in evaluated code that caused this exception
   */
  public ParserLocation location() {
    return location;
  }

  /**
   * Iterates over cause chain of this exception and tests if any of its elements are of provided type
   * @param test type to test against
   * @return true if any element from the cause chain can be assigned to a variable of given type
   */
  public boolean isCausedBy(Class<? extends Throwable> test) {
    for (Throwable cause = getCause(); cause != null; cause = cause.getCause()) {
      if (test.isAssignableFrom(cause.getClass())) {
        return true;
      }
    }
    return false;
  }
}

