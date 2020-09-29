/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.resources;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class PropertiesGenerator {

  public static final String YAML = "yaml";
  public static final String PROPERTIES = "properties";
  public static final String QUOTES = "\'";
  public static final String LINE_BREAK = "\n";
  public static final String YAML_SEPARATOR = ": ";
  public static final String PROPERTIES_SEPARATOR = "=";

  protected static String createYamlFormatValueSingleLevel(String mapName, String value) {
    Map<String, String> map = new HashMap<>();
    map.put(mapName, value);
    return createYaml(map);

  }

  private static <T extends Map> String createYaml(T map) {
    DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    options.setPrettyFlow(true);
    Yaml yaml = new Yaml(options);
    StringWriter writer = new StringWriter();
    yaml.dump(map, writer);
    return writer.toString();
  }

  protected static String createYamlFormatValue(String mapName, Map<String, String> subMap) {
    Map<String, Map<String, String>> map = new HashMap<>();
    map.put(mapName, subMap);
    return createYaml(map);
  }

  protected static String createValueFromString(String source, String extension) {
    if (extension.equalsIgnoreCase(YAML)) {
      return source;
    }
    if (extension.equalsIgnoreCase(PROPERTIES)) {
      return String.valueOf(source);
    }
    return null;
  }

  protected static String getSeparator(String extension) {
    switch (extension.toLowerCase()) {
      case YAML:
        return YAML_SEPARATOR;
      case PROPERTIES:
        return PROPERTIES_SEPARATOR;
      default:
        throw new RuntimeException("Invalid extension, please provide yaml or properties");
    }
  }

}
