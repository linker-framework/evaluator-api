package com.onkiup.linker.evaluator.api;

import java.net.URL;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PropertyFileContextTest {

  private PropertyFileContext subject;

  @Before
  public void setUp() throws Exception {
    URL source = getClass().getResource("/PropertyFileContextTest.properties");
    subject = new PropertyFileContext(null, source);
  }

  @Test
  public void resolve() {
    Optional a = subject.resolve("a");
    Assert.assertTrue(a.isPresent());
    Assert.assertEquals("test", a.get());
  }

  @Test
  public void containsKey() {
    Assert.assertTrue(subject.containsKey("a"));
    Assert.assertFalse(subject.containsKey("sdhfglsdf"));
  }
}
