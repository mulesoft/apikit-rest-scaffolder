/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.resources;

import org.mule.tools.apikit.model.CommonProperties;
import org.mule.tools.apikit.model.Configuration;

import java.util.HashMap;
import java.util.Map;

public class CommonPropertiesGenerator extends PropertiesGenerator {

  public static final String API_ID_KEY = "api.id";
  public static final String HTTP_PORT_KEY = "http.port";
  public static final String HTTP_HOST_KEY = "http.host";
  public static final String HTTP_PORT_SHORT_KEY = "port";
  public static final String HTTP_HOST_SHORT_KEY = "host";
  public static final String HTTP_HOST_VALUE = "0.0.0.0";
  public static final String HTTP_PORT_VALUE = "8081";
  public static final String HTTP_KEY = "http";
  public static final Map<String, String> SUB_MAP_HTTP = new HashMap<String, String>() {

    {
      put(HTTP_HOST_SHORT_KEY, HTTP_HOST_VALUE);
      put(HTTP_PORT_SHORT_KEY, HTTP_PORT_VALUE);
    }
  };

  public static String fill(Configuration configuration, String apiId) {
    String payload = createHTTPProperties(configuration);
    if (apiId != null) {
      Map<String, String> subMapAPIAutodiscovery = new HashMap<>();
      subMapAPIAutodiscovery.put(API_ID_KEY, apiId);
      payload = payload.concat(createYamlFormatValueSingleLevel(API_ID_KEY, apiId));
    }
    return payload;
  }

  private static String createHTTPProperties(Configuration configuration) {
    Map<String, String> subMapHTTP = SUB_MAP_HTTP;
    final CommonProperties commonProperties = configuration.getCommonProperties();
    if (commonProperties != null && commonProperties.getHttp() != null) {
      subMapHTTP = new HashMap<>();
      subMapHTTP.put(HTTP_HOST_SHORT_KEY, commonProperties.getHttp().getHost());
      subMapHTTP.put(HTTP_PORT_SHORT_KEY, commonProperties.getHttp().getPort());
    }
    return createYamlFormatValue(HTTP_KEY, subMapHTTP);
  }
}
