/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.TestUtils.*;
import static org.mule.tools.apikit.model.MuleConfigBuilder.fromDoc;

import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.HttpListenerConfig;
import org.mule.tools.apikit.model.MainFlow;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.junit.Test;

public class MuleConfigParserTest {

  @Test
  public void testCreation() throws Exception {
    InputStream resourceAsStream = getResourceAsStream("testGetEntries/leagues-flow-config.xml");

    String apiLocation = "leagues.raml";
    List<HttpListenerConfig> domainHttpListenerConfigs = new ArrayList<>();
    List<MuleConfig> muleConfigs = new ArrayList<>();
    muleConfigs.add(fromDoc(getDocumentFromStream(resourceAsStream)));

    MuleConfigParser muleConfigParser = new MuleConfigParser(new APIFactory(domainHttpListenerConfigs), apiLocation, muleConfigs);

    Set<ResourceActionMimeTypeTriplet> set = muleConfigParser.getEntries();
    assertNotNull(set);
    assertEquals(6, set.size());

    Set<ApikitMainFlowContainer> apis = muleConfigParser.getIncludedApis();
    assertNotNull(apis);
    assertEquals(1, apis.size());
    ApikitMainFlowContainer api = apis.iterator().next();
    assertEquals("leagues.raml", api.getApiFilePath());
    assertEquals("leagues", api.getId());
    assertNotNull(api.getHttpListenerConfig());
    assertEquals("/", api.getHttpListenerConfig().getBasePath());
    assertEquals("localhost", api.getHttpListenerConfig().getHost());
    assertEquals("${serverPort}", api.getHttpListenerConfig().getPort());
    assertEquals("HTTP_Listener_Configuration", api.getHttpListenerConfig().getName());
    assertEquals("/api/*", api.getPath());
  }

  @Test
  public void testParseMultipleXmls() throws JDOMException, IOException {
    InputStream xmlWithFlows = getResourceAsStream("testGetEntries/leagues-flow-config.xml");
    InputStream xmlWithoutFlows = getResourceAsStream("testGetEntries/leagues-without-flows.xml");
    Document documentWithFlows = getDocumentFromStream(xmlWithFlows);
    Document documentWithoutFlows = getDocumentFromStream(xmlWithoutFlows);

    MuleConfigParser muleConfigParser = new MuleConfigParser(new APIFactory(emptyList()), "", emptyList());

    MuleConfig muleConfigWithFlows = fromDoc(documentWithFlows);
    muleConfigParser.parseConfig(muleConfigWithFlows);
    muleConfigParser.parseApis(muleConfigWithFlows, "leagues.raml");
    muleConfigParser.parseFlows(singletonList(muleConfigWithFlows));

    MuleConfig muleConfigWithoutFlows = fromDoc(documentWithoutFlows);
    muleConfigParser.parseConfig(muleConfigWithoutFlows);
    muleConfigParser.parseApis(muleConfigWithoutFlows, "api.raml");
    muleConfigParser.parseFlows(singletonList(muleConfigWithoutFlows));

    assertEquals(6, muleConfigParser.getEntries().size());
    assertEquals(2, muleConfigParser.getApikitConfigs().size());
    assertEquals(2, muleConfigParser.getIncludedApis().size());
  }

  @Test
  public void testSeparateConfigsOrders() throws Exception {
    String api = getResourceAsString("separate-config/simple.xml");
    String config = getResourceAsString("separate-config/global.xml");
    String ramlPath = "separate-config/simple.raml";

    List<MuleConfig> muleConfigList = asList(fromDoc(getDocumentFromStream(new ByteArrayInputStream(api.getBytes()))),
                                             fromDoc(getDocumentFromStream(new ByteArrayInputStream(config.getBytes()))));
    MuleConfigParser muleConfigParser = new MuleConfigParser(new APIFactory(emptyList()), ramlPath, muleConfigList);

    assertEquals(2, muleConfigParser.getEntries().size());
    assertEquals(1, muleConfigParser.getIncludedApis().size());
    assertEquals(1, muleConfigParser.getApikitConfigs().size());

    List<MuleConfig> muleConfigListReverse = asList(fromDoc(getDocumentFromStream(new ByteArrayInputStream(config.getBytes()))),
                                                    fromDoc(getDocumentFromStream(new ByteArrayInputStream(api.getBytes()))));
    muleConfigParser = new MuleConfigParser(new APIFactory(emptyList()), ramlPath, muleConfigListReverse);

    assertEquals(2, muleConfigParser.getEntries().size());
    assertEquals(1, muleConfigParser.getIncludedApis().size());
    assertEquals(1, muleConfigParser.getApikitConfigs().size());
  }

  @Test
  public void testScaffoldingFlowWithChoiceElememnt() throws Exception {
    String api = getResourceAsString("scaffolder-with-choice-element/simple.xml");
    String ramlPath = "scaffolder-with-choice-element/simple.raml";

    List<MuleConfig> muleConfigList = singletonList(
                                                    fromDoc(getDocumentFromStream(new ByteArrayInputStream(api.getBytes()))));
    MuleConfigParser muleConfigParser = new MuleConfigParser(new APIFactory(emptyList()), ramlPath, muleConfigList);

    assertEquals(1, muleConfigList.size());
    assertTrue(muleConfigList.get(0).getFlows().stream().anyMatch(flow -> flow instanceof MainFlow));
    assertEquals(1, muleConfigParser.getEntries().size());
    assertEquals(1, muleConfigParser.getIncludedApis().size());
    assertEquals(1, muleConfigParser.getApikitConfigs().size());
  }

  @Test
  public void testScaffoldingFlowWithChoiceElementNoRouter() throws Exception {
    String api = getResourceAsString("scaffolder-with-choice-element/simple2.xml");
    String ramlPath = "scaffolder-with-choice-element/simple.raml";

    List<MuleConfig> muleConfigList = singletonList(
                                                    fromDoc(getDocumentFromStream(new ByteArrayInputStream(api.getBytes()))));

    assertFalse(muleConfigList.get(0).getFlows().stream().anyMatch(flow -> flow instanceof MainFlow));

  }
}
