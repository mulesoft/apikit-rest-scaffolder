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
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.ConfigurationGroup;
import org.mule.tools.apikit.model.HttpListenerConfig;
import org.mule.tools.apikit.model.HttpListenerConnection;
import org.mule.tools.apikit.model.ScaffolderResource;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ResourceGeneratorTest {

  public static final String EXPECTED_YAML_0 = "http:\n" +
      "  port: customPort\n" +
      "  host: customHost\n" +
      "myProperty: myNewHost\n" +
      "lalala: 123\n";
  public static final String EXPECTED_YAML_1 = "http:\n" +
      "  port: '8081'\n" +
      "  host: 0.0.0.0\n";
  public static final String DEV_CONFIGURATION_YAML = "dev-configuration.yaml";
  public static final String UAT_CONFIGURATION_YAML = "uat-configuration.yaml";
  public static final String PROD_CONFIGURATION_YAML = "prod-configuration.yaml";
  public static final String BASE_PATH = "src/test/resources/org.mule.tools.apikit.output.resources/";
  public static final String PREFFIX_FULL = "expected-full";
  public static final String SLASH = "/";

  @Test
  public void testNoGeneration() {
    ScaffoldingConfiguration.Builder scaffoldingConfigurationBuilder = ScaffoldingConfiguration.builder();
    scaffoldingConfigurationBuilder.withConfigurationGroup(null);
    assertNull(ResourcesGenerator.generate(scaffoldingConfigurationBuilder.build()));
  }

  @Test
  public void testGenerationYAMLFull() throws IOException {
    ScaffoldingConfiguration scaffoldingConfiguration = buildScaffoldingConfiguration("yaml/configuration-yaml-full.json", "123");
    List<ScaffolderResource> generatedResources = ResourcesGenerator.generate(scaffoldingConfiguration);
    commonAssertResources(generatedResources, 3);
  }

  @Test
  public void testGenerationPropertiesFull() throws IOException {
    ScaffoldingConfiguration scaffoldingConfiguration =
        buildScaffoldingConfiguration("properties/configuration-properties-full.json", "123");
    List<ScaffolderResource> generatedResources = ResourcesGenerator.generate(scaffoldingConfiguration);
    commonAssertResources(generatedResources, 3);
  }

  @Test
  public void testReplaceReferencesToProperties() {
    ScaffoldingConfiguration config = buildScaffoldingConfiguration("yaml/configuration-yaml-full.json", null);
    List<ApikitMainFlowContainer> apikitMainFlowContainers = new ArrayList<>();
    ApikitMainFlowContainer api = new ApikitMainFlowContainer(null, null, null, null);
    HttpListenerConnection httpListenerConnection =
        new HttpListenerConnection.Builder("myHost", "myPort", "HTTP")
            .build();
    HttpListenerConfig httpListenerConfig = new HttpListenerConfig("listenerConfig", "/", httpListenerConnection);
    api.setHttpListenerConfig(httpListenerConfig);
    apikitMainFlowContainers.add(api);
    ResourcesGenerator.replaceReferencesToProperties(config, apikitMainFlowContainers);
    for (ApikitMainFlowContainer apiElement : apikitMainFlowContainers) {
      HttpListenerConfig listenerConfig = apiElement.getHttpListenerConfig();
      assertEquals("${http.host}", listenerConfig.getHost());
      assertEquals("${http.port}", listenerConfig.getPort());
    }
  }

  private void commonAssertResources(List<ScaffolderResource> generatedResources, int expectedGeneratedResourcesSize)
      throws IOException {
    assertEquals(generatedResources.size(), expectedGeneratedResourcesSize);
    for (ScaffolderResource resource : generatedResources) {
      String environment = getEnvironment(resource.getName());
      String extension = getExtension(resource.getName());
      String expectedFile = getExpectedFile(extension, environment, PREFFIX_FULL);
      commonAssertResource(resource, environment + "-configuration" + extension, expectedFile);
    }
  }

  private String getExpectedFile(String extension, String environment, String preffix) throws IOException {
    String pathname = BASE_PATH + getFolder(extension) + SLASH + preffix + "-" + environment + extension;
    return IOUtils.toString(new FileInputStream(new File(pathname)));
  }

  private String getFolder(String extension) {
    return extension.replace(".", "");
  }

  private String getEnvironment(String name) {
    return getConfiguration(name, 0).split("-")[0];
  }

  private String getExtension(String name) {
    return getConfiguration(name, 1).split("-")[0];
  }

  private String getConfiguration(String name, int i) {
    return name.split("configuration")[i];
  }

  private ScaffoldingConfiguration buildScaffoldingConfiguration(String file, String apiAutodiscoveryId) {
    ScaffoldingConfiguration.Builder scaffoldingConfigurationBuilder = ScaffoldingConfiguration.builder();
    ObjectMapper mapper = new ObjectMapper();
    ConfigurationGroup configurationGroup;
    File configurationGroupFile = new File(BASE_PATH + file);
    try {
      configurationGroup = mapper.readValue(configurationGroupFile, ConfigurationGroup.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    scaffoldingConfigurationBuilder.withConfigurationGroup(configurationGroup);
    if (apiAutodiscoveryId != null) {
      scaffoldingConfigurationBuilder.withApiAutodiscoveryId(apiAutodiscoveryId);
    }
    ScaffoldingConfiguration scaffoldingConfiguration = scaffoldingConfigurationBuilder.build();
    return scaffoldingConfiguration;
  }

  private void commonAssertResource(ScaffolderResource resource, String name, String expectedValue) throws IOException {
    assertEquals(resource.getName(), name);
    String generatedValue = IOUtils.toString(resource.getContent());
    assertEquals(expectedValue, generatedValue);
  }
}
