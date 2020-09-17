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
import org.mule.tools.apikit.input.parsers.APIAutodiscoveryConfigParser;
import org.mule.tools.apikit.input.parsers.APIKitConfigParser;
import org.mule.tools.apikit.input.parsers.HttpListenerConfigParser;

import java.io.InputStream;
import java.util.ArrayList;

import java.util.List;
import java.util.Optional;

public class MuleConfigBuilder {

  public static MuleConfig fromDoc(Document muleConfigContent) {
    HttpListenerConfigParser httpConfigParser = new HttpListenerConfigParser();
    APIKitConfigParser apiKitConfigParser = new APIKitConfigParser();
    APIAutodiscoveryConfigParser apiAutodiscoveryConfigParser = new APIAutodiscoveryConfigParser();

    List<HttpListenerConfig> httpListenerConfigs = httpConfigParser.parse(muleConfigContent);
    List<APIKitConfig> apikitConfigs = apiKitConfigParser.parse(muleConfigContent);
    APIAutodiscoveryConfig apiAutodiscoveryConfig = apiAutodiscoveryConfigParser.parse(muleConfigContent);

    List<Flow> flowsInConfig = new ArrayList<>();

    for (Content content : muleConfigContent.getRootElement().getContent()) {
      if (content instanceof Element) {
        Element contentElement = (Element) content;
        if ("flow".equals(contentElement.getName())) {
          Optional<ApikitRouter> apikitRouter = getRouter(contentElement);
          Flow flow = new Flow(contentElement);
          if (apikitRouter.isPresent()) {
            MainFlow mainFlow = new MainFlow(contentElement);
            mainFlow.setApikitRouter(apikitRouter.get());
            flow = mainFlow;
          }
          flowsInConfig.add(flow);
        }
      }
    }
    return new MuleConfig(httpListenerConfigs, apikitConfigs, flowsInConfig, apiAutodiscoveryConfig, muleConfigContent);
  }

  public static MuleConfig fromStream(InputStream input) throws Exception {
    SAXBuilder builder = MuleConfigBuilder.getSaxBuilder();
    Document inputAsDocument = builder.build(input);
    input.close();
    return fromDoc(inputAsDocument);
  }

  public static Optional<ApikitRouter> getRouter(Element flow) {
    for (Content flowContent : flow.getContent()) {
      if (flowContent instanceof Element) {
        Element flowContentElement = (Element) flowContent;
        if (elementIsApikitRouter(flowContentElement)) {
          return Optional.of(new ApikitRouter(flowContentElement));
        }
      }
    }
    return Optional.empty();
  }

  private static boolean elementIsApikitRouter(Element element) {
    return element.getNamespace().getPrefix().equals("apikit") && element.getName().equals("router");
  }

  /**
   * Used for Mule configuration only.
   * As a good practice it prevents any potential XXE attack.
   * No need to relax it by externalized configuration due it is not allowed by design.
   *
   * @return
   */
  static SAXBuilder getSaxBuilder() {
    SAXBuilder builder = new SAXBuilder();
    builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
    builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    return builder;
  }
}
