package com.onkiup.linker.evaluator.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

/**
 * A context that loads its members from a properties file
 */
public class PropertyFileContext extends AbstractContext {

  private URL configuration;
  private Properties properties;

  public PropertyFileContext(EvaluationContext parent, URL configuration) {
    super(parent);
    this.configuration = configuration;
    try (InputStream is = configuration.openStream()) {
      properties = new Properties();
      properties.load(is);
    } catch (IOException e) {
      throw new EvaluationError("Failed to load properties from " + configuration, e);
    }
  }

  @Override
  public Optional<?> resolve(String key) {
    if (super.containsKey(key)) {
      return super.resolve(key);
    }

    if (properties.containsKey(key)) {
      String value = properties.getProperty(key);
      try {
        // PropertyContext will be primarily used to supply URL locations for unimplemented SAIL function
        // endpoints
        URL url = new URL(value);
        store(key, url, false, true);
        return Optional.ofNullable(new URL(value));
      } catch (MalformedURLException e) {
        store(key, value, false, true);
        return Optional.ofNullable(value);
      }
    }
    return parent().flatMap(p -> p.resolve(key));
  }

  @Override
  public boolean containsKey(String key) {
    return properties.containsKey(key);
  }
}

/*
module(
  imports(com.appian.a, com.appian.fn),
  com.lisa.properties: fn.with(
     data: io.read("configuration.properties"),
     a.unwrap(data)
  )
)
 */
