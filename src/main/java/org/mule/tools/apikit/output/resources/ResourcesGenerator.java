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
import org.mule.tools.apikit.model.HttpListenerConfig;
import org.mule.tools.apikit.model.HttpListenerConnection;
import org.mule.tools.apikit.model.ScaffolderResource;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ResourcesGenerator {

  public static final String FILE_NAME_SEPARATOR = "-configuration.";
  public static final String SLASH = "/";
  public static final String HTTP_HOST_REFERENCE = "${http.host}";
  public static final String HTTP_PORT_REFERENCE = "${http.port}";

  public static List<ScaffolderResource> generate(ScaffoldingConfiguration scaffoldingConfiguration) {
    if (scaffoldingConfiguration != null && scaffoldingConfiguration.getConfigurationGroup() != null) {
      ConfigurationGroup configurationGroup = scaffoldingConfiguration.getConfigurationGroup();
      List<ScaffolderResource> resources = new ArrayList<>();
      String extension = configurationGroup.getExtension();
      for (Configuration configuration : configurationGroup.getConfigurations()) {
        String fileName = getEnvironment(configuration).concat(FILE_NAME_SEPARATOR).concat(extension);
        String payload = PropertyGenerator.generate(configuration, scaffoldingConfiguration.getApiAutodiscoveryID(), extension);
        resources.add(new ScaffolderResource(SLASH, fileName, IOUtils.toInputStream(payload)));
      }
      return resources;
    }
    return null;
  }

  private static String getEnvironment(Configuration configuration) {
    if (StringUtils.isEmpty(configuration.getEnvironment())) {
      throw new RuntimeException("must specify an environment");
    }
    return configuration.getEnvironment();
  }


  public static void replaceReferencesToProperties(ScaffoldingConfiguration config, List<ApikitMainFlowContainer> includedApis) {
    if (config != null && config.getConfigurationGroup() != null) {
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
