/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.mule.tools.apikit.model.CommonProperties;
import org.mule.tools.apikit.model.Configuration;
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
  protected String processCommon(Configuration configuration, String apiAutodiscoveryId) {
    String payload = createHTTPProperties(configuration);
    boolean hasApiAutodiscoveryIdForEnvironment = hasApiAutodiscoveryIdForEnvironment(configuration.getCommonProperties());
    if (apiAutodiscoveryId != null || hasApiAutodiscoveryIdForEnvironment) {
      String apiAutodiscoveryIdValue =
          getApiAutodiscoveryID(configuration.getCommonProperties(), apiAutodiscoveryId, hasApiAutodiscoveryIdForEnvironment);
      payload = payload.concat(createYamlFormat(API_ID_KEY, apiAutodiscoveryIdValue));
    }
    return payload;
  }

  @Override
  protected String processCustom(Configuration configuration) {
    Map<String, Object> properties = configuration.getProperties();
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
    if (value instanceof String) {
      customPayload =
          customPayload.concat(key).concat(getSeparator())
              .concat(String.valueOf(value))
              .concat(LINE_BREAK);
    }
    if (value instanceof HashMap) {
      ObjectMapper oMapper = new ObjectMapper();
      Map<String, String> map = oMapper.convertValue(value, Map.class);
      for (Map.Entry<String, String> mapValue : map.entrySet()) {
        mapValue.setValue(String.valueOf(mapValue.getValue()));
      }
      customPayload = createYamlFormat(key, map);
    }
    return customPayload;
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

  private String createHTTPProperties(Configuration configuration) {
    Map<String, String> subMapHTTP = SUB_MAP_HTTP;
    final CommonProperties commonProperties = configuration.getCommonProperties();
    if (commonProperties != null && commonProperties.getHttp() != null) {
      subMapHTTP = new HashMap<>();
      subMapHTTP.put(HTTP_HOST_SHORT_KEY, commonProperties.getHttp().getHost());
      subMapHTTP.put(HTTP_PORT_SHORT_KEY, commonProperties.getHttp().getPort());
    }
    return createYamlFormat(HTTP_KEY, subMapHTTP);
  }

}
