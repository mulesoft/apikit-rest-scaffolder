/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.mule.tools.apikit.input.parsers.APIKitConfigParser;
import org.mule.tools.apikit.input.parsers.HttpListenerConfigParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MuleConfigBuilder {

  private final List<HttpListenerConfig> httpListenerConfigs = new ArrayList<>();
  private final Map<String, APIKitConfig> apiKitConfigs = new HashMap<>();
  private final List<Flow> flows = new ArrayList<>();
  private final List<Test> tests = new ArrayList<>();

  public void addHttpListenerConfig(HttpListenerConfig config) {
    httpListenerConfigs.add(config);
  }

  public void addApiKitConfig(APIKitConfig config) {
    apiKitConfigs.put(config.getName(), config);
  }

  public void addFlow(Flow flow) {
    flows.add(flow);
  }

  public void addTest(Test test) {
    tests.add(test);
  }

  public MuleConfig build() {
    return new MuleConfig(httpListenerConfigs, apiKitConfigs, flows, tests);
  }

  public static MuleConfig fromDoc(Document muleConfigContent) {
    HttpListenerConfigParser httpConfigParser = new HttpListenerConfigParser();
    APIKitConfigParser apiKitConfigParser = new APIKitConfigParser();

    List<HttpListenerConfig> httpListenerConfigs = httpConfigParser.parse(muleConfigContent);
    Map<String, APIKitConfig> apikitConfigs = apiKitConfigParser.parse(muleConfigContent);

    List<Flow> flowsInConfig = new ArrayList<>();
    List<Test> testsInConfig = new ArrayList<>();

    for (Content content : muleConfigContent.getRootElement().getContent()) {
      if (content instanceof Element) {
        Element contentElement = (Element) content;

        if ("flow".equals(contentElement.getName())) {
          flowsInConfig.add(new Flow(contentElement));
        }

        if ("munit:test".equals(contentElement.getName())) {
          testsInConfig.add(new Test(contentElement));
        }
      }
    }

    return new MuleConfig(httpListenerConfigs, apikitConfigs, flowsInConfig, testsInConfig, muleConfigContent);
  }

  public static MuleConfig fromStream(InputStream input) throws Exception {
    SAXBuilder builder = new SAXBuilder();
    Document inputAsDocument = builder.build(input);
    return fromDoc(inputAsDocument);
  }
}
