/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MuleConfig implements NamedContent, WithConstructs, WithConfigs {

  private Document originalContent;
  private List<HttpListenerConfig> configurations;
  private Map<String, APIKitConfig> apikitConfigs;
  private List<Flow> flows;
  private List<Test> tests;

  protected MuleConfig(List<HttpListenerConfig> configurations, Map<String, APIKitConfig> apikitConfigs,
                       List<Flow> flows, List<Test> test) {
    this.configurations = configurations;
    this.apikitConfigs = apikitConfigs;
    this.flows = flows;
    this.tests = test;
  }

  protected MuleConfig(List<HttpListenerConfig> httpListenerConfigs, Map<String, APIKitConfig> apikitConfigs, List<Flow> flows,
                       List<Test> tests, Document content) {
    this(httpListenerConfigs, apikitConfigs, flows, tests);
    this.originalContent = content;
  }

  public String getName() {
    return "";
  }

  public Document getContentAsDocument() {
    return originalContent;
  }

  @Override
  public InputStream getContent() {
    XMLOutputter xout = new XMLOutputter();
    String contentAsString = xout.outputString(originalContent);
    return IOUtils.toInputStream(contentAsString);
  }

  @Override
  public List<HttpListenerConfig> getHttpListenerConfigs() {
    return configurations;
  }

  @Override
  public List<Flow> getFlows() {
    return flows;
  }

  @Override
  public List<Test> getTests() {
    return tests;
  }

  public Map<String, APIKitConfig> getApikitConfigs() {
    return apikitConfigs;
  }

  public Document buildContent() {
    Document document = new Document();
    Element rootElement = originalContent.getRootElement().clone().detach();
    rootElement.setContent(new ArrayList<>()); // we only need the root element, not its content.
    document.setRootElement(rootElement);

    for (HttpListenerConfig config : configurations) {
      if (!config.isPeristed())
        addContent(document, config.generate());
    }
    apikitConfigs.values().forEach(apiKitConfig -> addContent(document, apiKitConfig.generate()));
    flows.forEach(flow -> addContent(document, flow.generate().clone().detach()));

    return document;
  }

  public void addFlow(Flow flow) {
    this.flows.add(flow);
  }

  private void addContent(Document document, Element element) {
    document.getRootElement().getContent().add(element);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    MuleConfig that = (MuleConfig) o;
    return Objects.equals(originalContent, that.originalContent) &&
        Objects.equals(configurations, that.configurations) &&
        Objects.equals(apikitConfigs, that.apikitConfigs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(originalContent, configurations, apikitConfigs);
  }
}
