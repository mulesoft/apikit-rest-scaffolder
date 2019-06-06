/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.RAMLFilesParser;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.output.GenerationModel;
import org.mule.tools.apikit.output.GenerationStrategy;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class GenerationStrategyTest {

  private GenerationStrategy generationStrategy;
  private APIFactory apiFactory;

  @Before
  public void setUp() {
    generationStrategy = new GenerationStrategy();
    apiFactory = new APIFactory();
  }

  @Test
  public void testAllEmptyGenerate() {
    Map<ResourceActionMimeTypeTriplet, GenerationModel> ramlEntries = new HashMap<>();
    Set<ApikitMainFlowContainer> includedApis = Sets.newHashSet();
    Set<ResourceActionMimeTypeTriplet> flowsEntries = Sets.newHashSet();
    List<GenerationModel> generate = generationStrategy.generate(ramlEntries, includedApis, flowsEntries);

    assertEquals(0, generate.size());
  }

  @Test
  public void testNotEmptyRamlGenerate() {
    final ApikitMainFlowContainer fromRAMLFile =
        apiFactory.createAPIBindingInboundEndpoint("sample.raml", "http://localhost:8080", "/api/*", null);
    MuleConfigParser mule = mock(MuleConfigParser.class);
    Map<ResourceActionMimeTypeTriplet, GenerationModel> ramlEntries = new HashMap<>();
    ramlEntries.put(new ResourceActionMimeTypeTriplet(fromRAMLFile, "pet", "post"), mock(GenerationModel.class));
    List<GenerationModel> generate = generationStrategy.generate(ramlEntries, mule.getIncludedApis(), mule.getEntries());

    assertEquals(1, generate.size());
  }

  @Test
  public void testExistingAPIKitFlow() {
    final ApikitMainFlowContainer api =
        apiFactory.createAPIBindingInboundEndpoint("sample.raml", "http://localhost:8080",
                                                   "/api/*", null);
    Set<ApikitMainFlowContainer> includedApis = Sets.newHashSet(api);
    Set<ResourceActionMimeTypeTriplet> flowsEntries = Sets.newHashSet(new ResourceActionMimeTypeTriplet(api, "/pet", "GET"));
    Map<ResourceActionMimeTypeTriplet, GenerationModel> ramlEntries = new HashMap<>();
    ramlEntries.put(new ResourceActionMimeTypeTriplet(api, "/pet", "GET"), mock(GenerationModel.class));
    List<GenerationModel> generate = generationStrategy.generate(ramlEntries, includedApis, flowsEntries);

    assertEquals(0, generate.size());
  }

  @Test
  public void testNonExistingAPIKitFlow() {
    RAMLFilesParser raml = mock(RAMLFilesParser.class);
    final ApikitMainFlowContainer api =
        apiFactory.createAPIBindingInboundEndpoint("sample.raml", "http://localhost:8080", "/api/*", null);
    Set<ApikitMainFlowContainer> includedApis = Sets.newHashSet(api);
    Set<ResourceActionMimeTypeTriplet> muleFlowEntries = Sets.newHashSet(new ResourceActionMimeTypeTriplet(api, "/pet", "GET"));
    ApikitMainFlowContainer fromRAMLFile =
        apiFactory.createAPIBindingInboundEndpoint("sample.raml", "http://localhost:8080", "/api/*", null);
    Map<ResourceActionMimeTypeTriplet, GenerationModel> ramlEntries = new HashMap<>();
    ramlEntries.put(new ResourceActionMimeTypeTriplet(fromRAMLFile, "/pet", "GET"), mock(GenerationModel.class));
    ramlEntries.put(new ResourceActionMimeTypeTriplet(fromRAMLFile, "/pet", "POST"), mock(GenerationModel.class));

    when(raml.getEntries()).thenReturn(ramlEntries);

    List<GenerationModel> generate = generationStrategy.generate(ramlEntries, includedApis, muleFlowEntries);
    assertEquals(1, generate.size());
  }
}
