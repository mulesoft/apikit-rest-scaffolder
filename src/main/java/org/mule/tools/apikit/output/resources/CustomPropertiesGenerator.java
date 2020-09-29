/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

public class CustomPropertiesGenerator extends PropertiesGenerator {

  public static String fill(String extension, Map<String, String> properties) {
    if (properties != null && CollectionUtils.isNotEmpty(properties.keySet())) {
      String customPayload = "";
      for (String key : properties.keySet()) {
        customPayload = customPayload.concat(createCustomPayload(properties.get(key), key, extension));
      }
      return customPayload;
    }
    return null;
  }

  private static <T> String createCustomPayload(T value, String key, String extension) {
    String customPayload = "";
    if (value instanceof String) {
      customPayload =
          customPayload.concat(key).concat(getSeparator(extension))
              .concat(createValueFromString(String.valueOf(value), extension))
              .concat(LINE_BREAK);
    }
    if (value instanceof HashMap) {
      ObjectMapper oMapper = new ObjectMapper();
      Map<String, String> map = oMapper.convertValue(value, Map.class);
      for (Map.Entry<String, String> mapValue : map.entrySet()) {
        mapValue.setValue(String.valueOf(mapValue.getValue()));
      }
      customPayload = createYamlFormatValue(key, map);
    }
    return customPayload;
  }

}
