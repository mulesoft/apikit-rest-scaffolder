/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.resources;

import org.apache.commons.lang.StringUtils;
import org.mule.tools.apikit.model.Configuration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class PropertyGenerator {

  public static final String YAML = "yaml";
  public static final String PROPERTIES = "properties";
  public static final String QUOTES = "\'";
  public static final String LINE_BREAK = "\n";
  public static final String YAML_SEPARATOR = ": ";
  public static final String PROPERTIES_SEPARATOR = "=";


  public static String generate(Configuration configuration, String apiAutodiscoveryId, String extension) {
    FileProcessor fileProcessor = pickProcessor(extension);
    String payload = fileProcessor.processCommon(configuration, apiAutodiscoveryId);
    payload = safeConcat(payload, fileProcessor.processCustom(configuration));
    return payload;
  }

  private static String safeConcat(String payload, String customValues) {
    if (StringUtils.isNotEmpty(payload) && StringUtils.isNotEmpty(customValues)) {
      return payload.concat(customValues);
    }
    return payload;
  }

  private static FileProcessor pickProcessor(String extension) {
    switch (extension.toLowerCase()) {
      case "yaml":
        return new YamlFileProcessor();
      case "properties":
        return new PropertiesFileProcessor();
      default:
        throw new RuntimeException("Invalid extension, please provide yaml or properties");
    }
  }


}
