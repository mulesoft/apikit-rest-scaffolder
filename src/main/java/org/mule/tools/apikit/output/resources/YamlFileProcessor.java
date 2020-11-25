/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class YamlFileProcessor extends FileProcessor {

  public static final String LINE_BREAK = "\n";
  public static final String YAML_SEPARATOR = ": ";
  public static final String HTTP_PORT_SHORT_KEY = "port";
  public static final String HTTP_HOST_SHORT_KEY = "host";
  public static final Map<String, String> SUB_MAP_HTTP = new HashMap<String, String>() {

    {
      put(HTTP_HOST_SHORT_KEY, HTTP_HOST_VALUE);
      put(HTTP_PORT_SHORT_KEY, HTTP_PORT_VALUE);
    }
  };

  @Override
  protected String processCommon(Map<String, Object> configuration, String apiAutodiscoveryId) {
    String payload = createHTTPProperties(configuration);
    boolean hasApiAutodiscoveryIdForEnvironment = hasApiAutodiscoveryIdForEnvironment(configuration);
    if (apiAutodiscoveryId != null && !hasApiAutodiscoveryIdForEnvironment) {
      payload = payload.concat(createYamlFormat(API_ID_KEY, apiAutodiscoveryId));
    }
    return payload;
  }

  @Override
  protected String processCustom(Map<String, Object> properties) {
    if (properties != null && CollectionUtils.isNotEmpty(properties.keySet())) {
      String customPayload = "";
      for (String key : properties.keySet()) {
        customPayload = customPayload.concat(createCustomPayload(properties.get(key), key));
      }
      return customPayload;
    }
    return null;
  }

  protected String getSeparator() {
    return YAML_SEPARATOR;
  }

  protected static <T extends Object> String createYamlFormat(String mapName, T value) {
    Map<String, T> map = new HashMap<>();
    map.put(mapName, value);
    return createYaml(map);
  }

  private <T> String createCustomPayload(T value, String key) {
    String customPayload = "";
    if (value instanceof Integer) {
      value = (T) (value.toString());
    }
    if (value instanceof String) {
      customPayload =
          customPayload.concat(key).concat(getSeparator())
              .concat(quotify(value.toString()))
              .concat(LINE_BREAK);
    }
    if (value instanceof HashMap) {
      ObjectMapper oMapper = new ObjectMapper();
      Map<String, String> map = oMapper.convertValue(value, Map.class);
      for (Map.Entry<String, String> mapValue : map.entrySet()) {
        mapValue.setValue(String.valueOf(mapValue.getValue()));
      }
      customPayload = customPayload.concat(createYamlFormat(key, map));
    }
    return customPayload;
  }

  private String quotify(Object value) {
    return "\'".concat(value.toString()).concat("\'");
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

  private String createHTTPProperties(Map<String, Object> properties) {
    Map<String, String> subMapHTTP = SUB_MAP_HTTP;
    if (properties != null && properties.get("http") == null) {
      return createYamlFormat(HTTP_KEY, subMapHTTP);
    }
    return StringUtils.EMPTY;
  }

}
