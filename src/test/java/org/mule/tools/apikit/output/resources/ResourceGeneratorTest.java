/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mule.tools.apikit.model.Properties;
import org.mule.tools.apikit.model.ScaffolderResource;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ResourceGeneratorTest {

  public static final String BASE_PATH = "src/test/resources/org.mule.tools.apikit.output.resources/";
  public static final String PREFFIX_FULL = "expected-full";
  public static final String SLASH = "/";

  @Test
  public void testNoGeneration() {
    ScaffoldingConfiguration.Builder scaffoldingConfigurationBuilder = ScaffoldingConfiguration.builder();
    assertNull(ResourcesGenerator.generate(scaffoldingConfigurationBuilder.build()));
  }

  @Test
  public void testGenerationYAMLFull() throws IOException {
    ScaffoldingConfiguration scaffoldingConfiguration =
        buildScaffoldingConfiguration("yaml/configuration-yaml-full.json", "123", "yaml");
    List<ScaffolderResource> generatedResources = ResourcesGenerator.generate(scaffoldingConfiguration);
    commonAssertResources(generatedResources, 3);
  }

  @Test
  public void testGenerationPropertiesFull() throws IOException {
    ScaffoldingConfiguration scaffoldingConfiguration =
        buildScaffoldingConfiguration("properties/configuration-properties-full.json", "123", "properties");
    List<ScaffolderResource> generatedResources = ResourcesGenerator.generate(scaffoldingConfiguration);
    commonAssertResources(generatedResources, 3);
  }

  @Test(expected = RuntimeException.class)
  public void testGenerationPropertiesNoFormat() {
    ScaffoldingConfiguration scaffoldingConfiguration =
        buildScaffoldingConfiguration(null, null, null);
    try {
      ResourcesGenerator.generate(scaffoldingConfiguration);
    } catch (RuntimeException ex) {
      assertEquals(ex.getMessage(), "format must be present");
      throw ex;
    }
  }

  private void commonAssertResources(List<ScaffolderResource> generatedResources, int expectedGeneratedResourcesSize)
      throws IOException {
    assertEquals(generatedResources.size(), expectedGeneratedResourcesSize);
    for (ScaffolderResource resource : generatedResources) {

      commonAssertResource(resource);
    }
  }

  private String getExpectedFile(String extension, String environment, String preffix) throws IOException {
    String pathname = BASE_PATH + extension.substring(1) + SLASH + preffix + "-" + environment + extension;
    return IOUtils.toString(new FileInputStream(new File(pathname)));
  }


  private String getEnvironment(String name) {
    return name.split("-")[0];
  }

  private String getExtension(String name) {
    return name.substring(name.lastIndexOf("."));
  }

  private ScaffoldingConfiguration buildScaffoldingConfiguration(String file, String apiAutodiscoveryId, String format) {
    ScaffoldingConfiguration.Builder scaffoldingConfigurationBuilder = ScaffoldingConfiguration.builder();
    Map<String, Map<String, Object>> files = new HashMap<>();
    if (format != null) {
      ObjectMapper mapper = new ObjectMapper();
      File configurationGroupFile = new File(BASE_PATH + file);
      try {
        files = mapper.readValue(configurationGroupFile, Map.class);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    Properties properties = new Properties(format, files);
    scaffoldingConfigurationBuilder.withProperties(properties);
    if (apiAutodiscoveryId != null) {
      scaffoldingConfigurationBuilder.withApiId(apiAutodiscoveryId);
    }
    ScaffoldingConfiguration scaffoldingConfiguration = scaffoldingConfigurationBuilder.build();
    return scaffoldingConfiguration;
  }

  private void commonAssertResource(ScaffolderResource resource) throws IOException {
    String environment = getEnvironment(resource.getName());
    String extension = getExtension(resource.getName());
    String expectedFile = getExpectedFile(extension, environment, PREFFIX_FULL);
    String name = environment + "-configuration" + extension;
    assertEquals(resource.getName(), name);
    String generatedValue = IOUtils.toString(resource.getContent());
    assertEquals(expectedFile, generatedValue);
  }
}
