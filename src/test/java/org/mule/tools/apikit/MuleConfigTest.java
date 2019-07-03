/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.Resource;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.Flow;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleConfigBuilder;
import org.mule.tools.apikit.output.GenerationModel;
import org.mule.tools.apikit.output.scopes.APIKitFlowScope;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.apikit.model.ActionType.GET;

public class MuleConfigTest {

  SAXBuilder builder;

  @Before
  public void setUp() {
    builder = new SAXBuilder();
  }

  @Test
  public void createsMuleConfig() throws Exception {
    String path = "src/test/resources/test-mule-config/api.xml";
    InputStream input = new FileInputStream(path);

    MuleConfig muleConfig = MuleConfigBuilder.fromStream(input);
    assertEquals(muleConfig.getFlows().size(), 8);
    assertEquals(muleConfig.getHttpListenerConfigs().size(), 1);
    input.close();
  }

  @Test
  public void createsMuleConfigWithFlowsAndConfigs() throws Exception {
    String path = "src/test/resources/test-mule-config/leagues-flow-config.xml";
    InputStream input = new FileInputStream(path);
    MuleConfig muleConfig = MuleConfigBuilder.fromStream(input);
    assertEquals(muleConfig.getFlows().size(), 7);
    assertEquals(muleConfig.getHttpListenerConfigs().size(), 1);
    input.close();
  }

  @Test
  public void createsMuleConfigWithFlowsWithoutConfig() throws Exception {
    String path = "src/test/resources/test-mule-config/mule-config-without-apikit-config.xml";
    InputStream input = new FileInputStream(path);

    MuleConfig muleConfig = MuleConfigBuilder.fromStream(input);
    assertEquals(muleConfig.getFlows().size(), 4);
    assertEquals(muleConfig.getHttpListenerConfigs().size(), 0);
    input.close();
  }

  @Test
  public void createsMuleConfigWithConfigWithoutFlow() throws Exception {
    String path = "src/test/resources/test-mule-config/config-without-flows.xml";
    InputStream input = new FileInputStream(path);

    MuleConfig muleConfig = MuleConfigBuilder.fromStream(input);
    assertEquals(muleConfig.getFlows().size(), 0);
    assertEquals(muleConfig.getHttpListenerConfigs().size(), 1);
    input.close();
  }

  @Test
  public void deserializationReturnsSameContent() throws Exception {
    String path = "src/test/resources/test-mule-config/api.xml";
    InputStream fileAsInputStream = new FileInputStream(path);
    MuleConfig muleConfig = MuleConfigBuilder.fromStream(fileAsInputStream);

    String originalFileAsString = FileUtils.readFileToString(new File(path));
    String muleConfigContentAsString = IOUtils.toString(muleConfig.getContent());
    Diff diff = XMLUnit.compareXML(originalFileAsString, muleConfigContentAsString);
    assertTrue(diff.identical());
  }

  @Test
  public void buildContentReturnsSameContent() throws Exception {
    String path = "src/test/resources/test-mule-config/api.xml";
    XMLUnit.setIgnoreWhitespace(true);

    InputStream fileAsInputStream = new FileInputStream(path);
    MuleConfig muleConfig = MuleConfigBuilder.fromStream(fileAsInputStream);

    String originalFileAsString = FileUtils.readFileToString(new File(path));
    String generatedString = new XMLOutputter().outputString(muleConfig.getContentAsDocument());
    Diff diff = XMLUnit.compareXML(originalFileAsString, generatedString);
    assertTrue(diff.identical());
  }

  @Test
  public void addNewFlow() throws Exception {
    String path = "src/test/resources/test-mule-config/api.xml";
    InputStream fileAsInputStream = new FileInputStream(path);
    MuleConfig muleConfig = MuleConfigBuilder.fromStream(fileAsInputStream);

    ApikitMainFlowContainer api = mock(ApikitMainFlowContainer.class);
    APIKitConfig fakeApikitConfig = mock(APIKitConfig.class);
    Action action = mock(Action.class);
    Resource resource = mock(Resource.class);

    when(action.getType()).thenReturn(GET);
    when(api.getId()).thenReturn("file");
    when(api.getPath()).thenReturn("/api/*");
    when(api.getConfig()).thenReturn(fakeApikitConfig);
    when(resource.getResolvedUri(anyString())).thenReturn("/new-customers");
    when(fakeApikitConfig.getName()).thenReturn("api-config");

    GenerationModel generationModel = new GenerationModel(api, "v1", resource, action);
    Element flowElement = new APIKitFlowScope(generationModel).generate();
    Flow flow = new Flow(flowElement);

    muleConfig.addFlow(flow);
    Document generatedContent = muleConfig.buildContent();

    // Verify flow doesn't exist in the original MuleConfig
    Document originalContent = new SAXBuilder().build(muleConfig.getContent());
    Element flowShouldNotExist =
        findElementByAttribute(originalContent.getRootElement().getContent(), "name", "new-customers:api-config");
    assertTrue(flowShouldNotExist == null);

    // Verify the new flow is in the updated MuleConfig
    Element newFlowInMuleConfig =
        findElementByAttribute(generatedContent.getRootElement().getContent(), "name", "new-customers:api-config");
    assertTrue(newFlowInMuleConfig != null);
  }

  private Element findElementByAttribute(List<Content> contentList, String attributeName, String attributeValue) {
    for (Content content : contentList) {
      if ((content instanceof Element) && ((Element) content).getAttribute(attributeName).toString().contains(attributeValue)) {
        return (Element) content;
      }
    }
    return null;
  }

}
