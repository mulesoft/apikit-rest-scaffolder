/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.resources;

import java.util.Map;

public class PropertyGenerator {

  public static String generate(Map<String, Object> configuration, String apiAutodiscoveryId, String extension) {
    FileProcessor fileProcessor = pickProcessor(extension);
    String payload = fileProcessor.processCommon(configuration, apiAutodiscoveryId);
    payload = safeConcat(payload, fileProcessor.processCustom(configuration));
    return payload;
  }

  private static String safeConcat(String payload, String customValues) {
    if (payload != null && customValues != null) {
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
