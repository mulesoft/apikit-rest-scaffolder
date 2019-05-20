/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.junit.Test;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.HttpListener4xConfig;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class MuleConfigParserTest {

  @Test
  public void testCreation() {
    final InputStream resourceAsStream =
        MuleConfigParser.class.getClassLoader().getResourceAsStream(
                                                                    "testGetEntries/leagues-flow-config.xml");
    
    String apiLocation = "leagues.raml";
    List<HttpListener4xConfig> domainHttpListenerConfigs = new ArrayList<>();
    List<InputStream> content = Arrays.asList(resourceAsStream);

    MuleConfigParser muleConfigParser = new MuleConfigParser(new APIFactory(domainHttpListenerConfigs));
    muleConfigParser.parse(apiLocation, content);

    Set<ResourceActionMimeTypeTriplet> set = muleConfigParser.getEntries();
    assertNotNull(set);
    assertEquals(6, set.size());

    Set<API> apis = muleConfigParser.getIncludedApis();
    assertNotNull(apis);
    assertEquals(1, apis.size());
    API api = apis.iterator().next();
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
    final InputStream xmlWithFlows =
        MuleConfigParser.class.getClassLoader().getResourceAsStream(
                                                                    "testGetEntries/leagues-flow-config.xml");
    final InputStream xmlWithoutFlows =
        MuleConfigParser.class.getClassLoader().getResourceAsStream(
                                                                    "testGetEntries/leagues-without-flows.xml");

    Document documentWithFlows = getDocument(xmlWithFlows);
    Document documentWithoutFlows = getDocument(xmlWithoutFlows);

    File fileWithFlows = new File("leagues.xml");
    File fileWithoutFlows = new File("api.xml");

    HashSet<String> ramlNames = new HashSet<>();
    ramlNames.add("leagues.raml");
    ramlNames.add("api.raml");

    MuleConfigParser muleConfigParser = new MuleConfigParser(new APIFactory());

    muleConfigParser.parseConfigs(documentWithFlows);
    muleConfigParser.parseApis(fileWithFlows, documentWithFlows, "leagues.raml");
    muleConfigParser.parseFlows(Collections.singleton(documentWithFlows));

    muleConfigParser.parseConfigs(documentWithoutFlows);
    muleConfigParser.parseApis(fileWithoutFlows, documentWithoutFlows, "api.raml");
    muleConfigParser.parseFlows(Collections.singleton(documentWithoutFlows));

    assertEquals(6, muleConfigParser.getEntries().size());
    assertEquals(2, muleConfigParser.getApikitConfigs().size());
    assertEquals(2, muleConfigParser.getIncludedApis().size());
  }

  @Test
  public void testSeparateConfigsOrders() throws IOException {
    final URL api =
        MuleConfigParser.class.getClassLoader().getResource("separate-config/simple.xml");
    final URL config =
        MuleConfigParser.class.getClassLoader().getResource("separate-config/global.xml");

    List<InputStream> streams = Arrays.asList(api.openStream(), config.openStream());
    String ramlPath = "separate-config/simple.raml";

    MuleConfigParser muleConfigParser = new MuleConfigParser(new APIFactory());
    muleConfigParser.parse(ramlPath, streams);

    assertEquals(2, muleConfigParser.getEntries().size());
    assertEquals(1, muleConfigParser.getIncludedApis().size());
    assertEquals(1, muleConfigParser.getApikitConfigs().size());

    List<InputStream> myStreamsRevers = Arrays.asList(config.openStream(), api.openStream());
    muleConfigParser = new MuleConfigParser(new APIFactory());
    muleConfigParser.parse(ramlPath, myStreamsRevers);

    assertEquals(2, muleConfigParser.getEntries().size());
    assertEquals(1, muleConfigParser.getIncludedApis().size());
    assertEquals(1, muleConfigParser.getApikitConfigs().size());
  }


  private Document getDocument(InputStream xmlWithFlows) throws JDOMException, IOException {
    SAXBuilder saxBuilder = new SAXBuilder(XMLReaders.NONVALIDATING);
    Document document = saxBuilder.build(xmlWithFlows);
    xmlWithFlows.close();
    return document;
  }


}
