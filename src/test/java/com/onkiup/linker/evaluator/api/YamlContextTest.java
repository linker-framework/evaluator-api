package com.onkiup.linker.evaluator.api;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class YamlContextTest {

  private YamlContext subject;

  @Before
  public void setUp() throws Exception {
    URL source =  getClass().getResource("/YamlContextTest.yaml");
    subject = new YamlContext(null, new InputStreamReader(source.openStream()));
  }

  @Test
  public void resolve() {
    Optional a = subject.resolve("a");
    Assert.assertNotNull(a);
    Assert.assertTrue(a.isPresent());
    Assert.assertTrue(a.get() instanceof Map);
  }

  @Test
  public void containsKey() {
    Assert.assertTrue(subject.containsKey("fn"));
    Assert.assertTrue(subject.containsKey("a"));
    Assert.assertFalse(subject.containsKey("zzz"));
  }
}
