/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.resources;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.Configuration;
import org.mule.tools.apikit.model.ConfigurationGroup;
import org.mule.tools.apikit.model.CustomConfiguration;
import org.mule.tools.apikit.model.HttpListenerConfig;
import org.mule.tools.apikit.model.HttpListenerConnection;
import org.mule.tools.apikit.model.ScaffolderResource;

import java.util.ArrayList;
import java.util.List;

public class ResourcesGenerator {

  public static final String FILE_NAME_SEPARATOR = "-configuration.";
  public static final String SLASH = "/";
  public static final String HTTP_HOST_REFERENCE = "${http.host}";
  public static final String HTTP_PORT_REFERENCE = "${http.port}";

  public static List<ScaffolderResource> generate(CustomConfiguration customConfiguration) {
    if (customConfiguration != null && customConfiguration.getConfigurationGroup().isPresent()) {
      ConfigurationGroup configurationGroup = customConfiguration.getConfigurationGroup().get();
      List<ScaffolderResource> resources = new ArrayList<>();
      String extension = configurationGroup.getExtension();
      for (Configuration configuration : configurationGroup.getConfigurations()) {
        String fileName = configuration.getEnvironment().concat(FILE_NAME_SEPARATOR).concat(extension);
        String payload =
            CommonPropertiesGenerator.fill(configuration, customConfiguration.getApiAutodiscoveryID().orElse(null), extension);
        payload = safeConcat(payload, CustomPropertiesGenerator.fill(extension, configuration.getProperties()));
        resources.add(new ScaffolderResource(SLASH, fileName, IOUtils.toInputStream(payload)));
      }
      return resources;
    }
    return null;
  }

  private static String safeConcat(String payload, String customValues) {
    if (StringUtils.isNotEmpty(payload) && StringUtils.isNotEmpty(customValues)) {
      return payload.concat(customValues);
    }
    return payload;
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


}
