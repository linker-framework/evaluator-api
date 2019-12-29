package com.onkiup.linker.evaluator.common;

import org.junit.Assert;
import org.junit.Test;

import com.onkiup.linker.evaluator.api.Connector;
import com.onkiup.linker.evaluator.api.Invoker;
import com.onkiup.linker.evaluator.api.RuleEvaluator;
import com.onkiup.linker.parser.NonParseable;

public class JavaConnectorTest {

  public class VariantRule implements NonParseable {

  }

  public static class VariantRuleEvaluator implements RuleEvaluator<VariantRule,String> {
    @Override
    public String evaluate() {
      return "a";
    }
  }

  public static String testMethod(String source) {
    return source;
  }

  @Test
  public void invoke() throws Exception {
    Connector connector = Connector.to(JavaConnectorTest.class.getMethod("testMethod", String.class));
    Object result = connector.as(Invoker.class).invoke(() -> "a");
    Assert.assertEquals("a", result);

    result = connector.as(Invoker.class).invoke("a");
    Assert.assertEquals("a", result);

    result = connector.as(Invoker.class).invoke(new VariantRule());
    Assert.assertEquals("a", result);
  }
}
