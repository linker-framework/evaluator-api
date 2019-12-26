package com.onkiup.linker.evaluator.common;

import java.io.Reader;
import java.util.Map;
import java.util.Optional;

import org.yaml.snakeyaml.Yaml;

import com.onkiup.linker.evaluator.api.EvaluationContext;

public class YamlContext extends AbstractContext<String> {

  Map<String, Object> values;

  public YamlContext(EvaluationContext parent, Reader source) {
    super(parent);
    Object data = new Yaml().load(source);
    if (data instanceof Map) {
      values = (Map<String,Object>)data;
    } else {
      throw new RuntimeException("Invalid top-level element in Yaml source: expected a map");
    }
  }


  @Override
  public Optional<?> resolveLocally(String key) {
    if (!values.containsKey(key)) {
      return Optional.empty();
    }

    return Optional.of(values.get(key));
  }

  @Override
  public boolean containsKey(String key) {
    return values.containsKey(key);
  }
}
