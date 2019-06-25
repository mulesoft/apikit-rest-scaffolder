/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import java.io.InputStream;
import java.util.*;

public class MuleConfig implements NamedContent, WithConstructs, WithConfigs {

  private String name;
  private Document originalContent;
  private List<HttpListenerConfig> configurations;
  private List<APIKitConfig> apikitConfigs;
  private List<Flow> flows;

  protected MuleConfig(List<HttpListenerConfig> configurations, List<APIKitConfig> apikitConfigs, List<Flow> flows) {
    this.configurations = configurations;
    this.apikitConfigs = apikitConfigs;
    this.flows = flows;
  }

  protected MuleConfig(List<HttpListenerConfig> httpConfigs, List<APIKitConfig> apikitConfigs, List<Flow> flows,
                       Document content) {
    this(httpConfigs, apikitConfigs, flows);
    this.originalContent = content;
  }

  public String getName() {
    return "";
  }

  public void setName(String name) {
    this.name = name;
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
    return Collections.unmodifiableList(configurations);
  }

  public void addHttpListener(HttpListenerConfig config) {
    this.configurations.add(config);
  }

  @Override
  public List<Flow> getFlows() {
    return Collections.unmodifiableList(flows);
  }

  public void addConfig(APIKitConfig value) {
    this.apikitConfigs.add(value);
  }

  public List<APIKitConfig> getApikitConfigs() {
    return Lists.newArrayList(apikitConfigs);
  }

  public Document buildContent() {
    Document document = new Document();
    Element rootElement = originalContent.getRootElement().clone().detach();
    rootElement.setContent(new ArrayList<>()); // we only need the root element, not its content.
    document.setRootElement(rootElement);

    for (HttpListenerConfig config : configurations) {
      if (!config.isPersisted())
        addContent(document, config.generate());
    }
    apikitConfigs.forEach(apiKitConfig -> addContent(document, apiKitConfig.generate()));
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
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
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
