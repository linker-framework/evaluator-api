package com.onkiup.linker.evaluator.api;

import static org.junit.Assert.*;

import org.junit.Test;

public class EvaluatorTest {

  @Test
  public void resultType() {
    class StringEvaluator implements Evaluator<String> {
      @Override
      public String evaluate() {
        return "";
      }

      @Override
      public Class<String> resultType() {
        return String.class;
      }
    }
    StringEvaluator evaluator = new StringEvaluator();
    assertEquals(String.class, evaluator.resultType());
  }
}
