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
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class MuleConfig implements NamedContent, WithConstructs, WithConfigs {

  private String name;
  private Document originalContent;
  private List<HttpListenerConfig> configurations;
  private List<APIKitConfig> apikitConfigs;
  private APIAutodiscoveryConfig apiAutodiscoveryConfig;
  private List<Flow> flows;
  private static final String INDENTATION = "    ";

  protected MuleConfig(List<HttpListenerConfig> configurations, List<APIKitConfig> apikitConfigs, List<Flow> flows,
                       APIAutodiscoveryConfig apiAutodiscoveryConfig) {
    this.configurations = configurations;
    this.apikitConfigs = apikitConfigs;
    this.apiAutodiscoveryConfig = apiAutodiscoveryConfig;
    this.flows = flows;
  }

  protected MuleConfig(List<HttpListenerConfig> httpConfigs, List<APIKitConfig> apikitConfigs, List<Flow> flows,
                       APIAutodiscoveryConfig apiAutodiscoveryConfig,
                       Document content) {
    this(httpConfigs, apikitConfigs, flows, apiAutodiscoveryConfig);
    this.originalContent = content;
  }

  public APIAutodiscoveryConfig getApiAutodiscoveryConfig() {
    return apiAutodiscoveryConfig;
  }

  public void setApiAutodiscoveryConfig(APIAutodiscoveryConfig apiAutodiscoveryConfig) {
    this.apiAutodiscoveryConfig = apiAutodiscoveryConfig;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Document getContentAsDocument() {
    return originalContent;
  }

  @Override
  public InputStream getContent() {
    Format prettyFormat = Format.getPrettyFormat();
    prettyFormat.setIndent(INDENTATION);
    prettyFormat.setLineSeparator(System.getProperty("line.separator"));
    prettyFormat.setEncoding("UTF-8");
    XMLOutputter xout = new XMLOutputter(prettyFormat);
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

  public List<MainFlow> getMainFlows() {
    List<MainFlow> mainFlows = flows.stream()
        .filter(flow -> flow instanceof MainFlow)
        .map(MainFlow.class::cast)
        .collect(toList());
    return Collections.unmodifiableList(mainFlows);
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
    if (apiAutodiscoveryConfig != null) {
      addContent(document, apiAutodiscoveryConfig.generate());
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
