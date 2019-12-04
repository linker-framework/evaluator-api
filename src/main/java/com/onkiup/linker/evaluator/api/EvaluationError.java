package com.onkiup.linker.evaluator.api;

import com.onkiup.linker.parser.Extension;
import com.onkiup.linker.parser.ParserLocation;
import com.onkiup.linker.parser.Rule;

public class EvaluationError extends RuntimeException {

  private ParserLocation location;

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

  public EvaluationError(String message) {
    this(ParserLocation.ZERO, message);
  }

  public EvaluationError(String message, Throwable cause) {
    this(ParserLocation.ZERO, message);
  }
  
  public ParserLocation location() {
    return location;
  }
}

