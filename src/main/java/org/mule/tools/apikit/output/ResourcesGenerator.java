/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import org.apache.commons.io.IOUtils;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.Configuration;
import org.mule.tools.apikit.model.ConfigurationGroup;
import org.mule.tools.apikit.model.CustomConfiguration;
import org.mule.tools.apikit.model.HttpListenerConfig;
import org.mule.tools.apikit.model.HttpListenerConnection;
import org.mule.tools.apikit.model.ScaffolderResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResourcesGenerator {

  public static final String YAML = "yaml";
  public static final String PROPERTIES = "properties";
  public static final String YAML_SEPARATOR = ": ";
  public static final String PROPERTIES_SEPARATOR = "=";
  public static final String FILE_NAME_SEPARATOR = "-configuration.";
  public static final String LINE_BREAK = "\n";
  public static final String QUOTES = "\"";
  public static final String API_ID_KEY = "api.id";
  public static final String HTTP_PORT_KEY = "http.port";
  public static final String HTTP_HOST_KEY = "http.host";
  public static final String HTTP_HOST_VALUE = "0.0.0.0";
  public static final String HTTP_PORT_VALUE = "8081";
  public static final String SLASH = "/";
  public static final String HTTP_HOST_REFERENCE = "${http.host}";
  public static final String HTTP_PORT_REFERENCE = "${http.port}";

  public static List<ScaffolderResource> generate(CustomConfiguration customConfiguration) {
    if (customConfiguration != null && customConfiguration.getConfigurationGroup().isPresent()) {
      ConfigurationGroup configurationGroup = customConfiguration.getConfigurationGroup().get();
      List<ScaffolderResource> resources = new ArrayList<>();
      String extension = configurationGroup.getExtension();
      for (Configuration configuration : configurationGroup.getConfigurations()) {
        String separator = getSeparator(extension);
        String fileName = configuration.getEnvironment().concat(FILE_NAME_SEPARATOR).concat(extension);
        String payload = fillCommonProperties(customConfiguration, extension, separator);
        payload = fillCustomProperties(extension, configuration.getProperties(), separator, payload);
        resources.add(new ScaffolderResource(SLASH, fileName, IOUtils.toInputStream(payload)));
      }
      return resources;
    }
    return null;
  }

  public static void replaceReferencesToProperties(CustomConfiguration config, List<ApikitMainFlowContainer> includedApis) {
    if (config != null && config.getConfigurationGroup().isPresent()) {
      for (ApikitMainFlowContainer api : includedApis) {
        HttpListenerConfig existingHttpConfig = api.getHttpListenerConfig();
        HttpListenerConnection httpListenerConnection =
            new HttpListenerConnection.Builder(HTTP_HOST_REFERENCE, HTTP_PORT_REFERENCE, existingHttpConfig.getProtocol())
                .build();
        api.setHttpListenerConfig(new HttpListenerConfig(existingHttpConfig.getName(), existingHttpConfig.getBasePath(),
                                                         httpListenerConnection));
        includedApis.set(includedApis.indexOf(api), api);
      }
    }
  }

  private static String getSeparator(String extension) {
    switch (extension.toLowerCase()) {
      case YAML:
        return YAML_SEPARATOR;
      case PROPERTIES:
        return PROPERTIES_SEPARATOR;
      default:
        throw new RuntimeException("Invalid extension, please provide yaml or properties");
    }
  }

  private static String fillCommonProperties(CustomConfiguration customConfiguration, String extension, String separator) {
    String payload = HTTP_HOST_KEY.concat(separator).concat(createValue(HTTP_HOST_VALUE, extension)).concat(LINE_BREAK);
    payload = payload.concat(HTTP_PORT_KEY).concat(separator).concat(createValue(HTTP_PORT_VALUE, extension)).concat(LINE_BREAK);
    if (customConfiguration.getApiAutodiscoveryID().isPresent()) {
      payload = payload.concat(API_ID_KEY).concat(separator)
          .concat(createValue(customConfiguration.getApiAutodiscoveryID().get(), extension)).concat(LINE_BREAK);
    }
    return payload;
  }

  private static String fillCustomProperties(String extension, Map<String, String> properties, String separator, String payload) {
    for (String key : properties.keySet()) {
      payload =
          payload.concat(key).concat(separator).concat(createValue(properties.get(key), extension))
              .concat(LINE_BREAK);
    }
    return payload;
  }

  private static String createValue(String value, String extension) {
    if (extension.equalsIgnoreCase(YAML)) {
      value = QUOTES + value + QUOTES;
    }
    return value;
  }
}
