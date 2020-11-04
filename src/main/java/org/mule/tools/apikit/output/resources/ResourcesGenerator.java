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
import org.mule.tools.apikit.model.HttpListenerConfig;
import org.mule.tools.apikit.model.HttpListenerConnection;
import org.mule.tools.apikit.model.ScaffolderResource;
import org.mule.tools.apikit.model.ScaffoldingAccessories;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResourcesGenerator {

  public static final String FILE_NAME_SEPARATOR = "-configuration.";
  public static final String SLASH = "/";

  public static List<ScaffolderResource> generate(ScaffoldingConfiguration scaffoldingConfiguration) {
    ScaffoldingAccessories scaffoldingAccessories = scaffoldingConfiguration.getScaffoldingAccessories();
    if (scaffoldingAccessories.getProperties() != null) {
      String extension = scaffoldingAccessories.getProperties().getFormat();
      Map<String, Map<String, Object>> files = scaffoldingAccessories.getProperties().getFiles();
      List<ScaffolderResource> resources = new ArrayList<>();
      for (Map.Entry<String, Map<String, Object>> properties : files.entrySet()) {
        String environment = properties.getKey();
        String fileName = environment.concat(FILE_NAME_SEPARATOR).concat(extension);
        String payload =
            PropertyGenerator.generate(properties.getValue(), scaffoldingAccessories.getApiId(), extension);
        resources.add(new ScaffolderResource(SLASH, fileName, IOUtils.toInputStream(payload)));
      }
      return resources;
    }
    return null;
  }

}
