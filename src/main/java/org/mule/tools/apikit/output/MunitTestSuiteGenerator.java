/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import java.util.*;

import com.google.common.collect.Lists;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleConfigBuilder;
import org.mule.tools.apikit.model.MunitScaffolderContext;
import org.mule.tools.apikit.model.ScaffolderResource;
import org.mule.tools.apikit.output.scopes.HttpRequestConfigScope;
import org.mule.tools.apikit.output.scopes.MunitConfigScope;
import org.mule.tools.apikit.output.scopes.MunitTestFlowScope;
import org.mule.tools.apikit.output.scopes.MuleScope;

public class MunitTestSuiteGenerator {

  public static final NamespaceWithLocation DOC_NAMESPACE = new NamespaceWithLocation(
                                                                                      Namespace
                                                                                          .getNamespace("doc",
                                                                                                        "http://www.mulesoft.org/schema/mule/documentation"),
                                                                                      "");


  public static final NamespaceWithLocation MUNIT_NAMESPACE = new NamespaceWithLocation(
                                                                                        Namespace
                                                                                            .getNamespace("munit",
                                                                                                          "http://www.mulesoft.org/schema/mule/munit"),
                                                                                        "http://www.mulesoft.org/schema/mule/munit/current/mule-munit.xsd");

  public static final NamespaceWithLocation MUNIT_TOOLS_NAMESPACE = new NamespaceWithLocation(
                                                                                              Namespace
                                                                                                  .getNamespace("munit-tools",
                                                                                                                "http://www.mulesoft.org/schema/mule/munit-tools"),
                                                                                              "http://www.mulesoft.org/schema/mule/munit-tools/current/mule-munit-tools.xsd");

  private final List<GenerationModel> flowEntries;
  private MunitScaffolderContext scaffolderContext;
  private String mainFlowName;

  private final Set<ScaffolderResource> generatedResources = new HashSet<>();

  public MunitTestSuiteGenerator(List<GenerationModel> flowEntries, MunitScaffolderContext scaffolderContext,
                                 String mainFlowName) {
    this.flowEntries = flowEntries;
    this.scaffolderContext = scaffolderContext;
    this.mainFlowName = mainFlowName;
  }

  public List<ScaffolderResource> getGeneratedResources() {
    return Collections.unmodifiableList(Lists.newArrayList(generatedResources));
  }

  public List<MuleConfig> generate() {
    Set<MuleConfig> muleConfigs = new HashSet<>();
    for (GenerationModel flowEntry : flowEntries) {
      ApikitMainFlowContainer api = flowEntry.getApi();
      MuleConfig muleConfig = api.getMuleConfig() != null ? api.getMuleConfig() : createMunitMuleConfig(api);
      if (api.getConfig() == null) {
        api.setDefaultAPIKitConfig();
        addMunitConfig(muleConfig);
        addHttpRequestConfig(api, muleConfig);
      }

      addMunitTests(muleConfig, flowEntry);
      muleConfigs.add(muleConfig);
    }
    return Lists.newArrayList(muleConfigs);
  }

  private MuleConfig createMunitMuleConfig(ApikitMainFlowContainer apikitMainFlow) {
    Document document = new Document();
    document.setRootElement(new MuleScope(false, true).generate());
    MuleConfig muleConfig = MuleConfigBuilder.fromDoc(document);
    muleConfig.setName(scaffolderContext.getMunitSuiteName());
    apikitMainFlow.setMuleConfig(muleConfig);
    return muleConfig;
  }

  private void addMunitConfig(MuleConfig muleConfig) {
    Element rootElement = muleConfig.getContentAsDocument().getRootElement();
    Element munitConfigElement = new MunitConfigScope(scaffolderContext.getMunitSuiteName()).generate();
    rootElement.addContent(munitConfigElement);
  }

  private void addHttpRequestConfig(ApikitMainFlowContainer apikitMainFlowContainer, MuleConfig muleConfig) {
    Element rootElement = muleConfig.getContentAsDocument().getRootElement();
    Element httpRequestElement = new HttpRequestConfigScope(apikitMainFlowContainer).generate();
    rootElement.addContent(httpRequestElement);
  }

  private void addMunitTests(MuleConfig muleConfig, GenerationModel flowEntry) {
    MunitTestFlowScope testFlowScope = new MunitTestFlowScope(flowEntry, mainFlowName);
    testFlowScope.setCreateResourceFiles(scaffolderContext.shouldCreateMunitResources());
    muleConfig.getContentAsDocument().getRootElement().addContent(testFlowScope.generateTests());
    generatedResources.addAll(testFlowScope.getGeneratedResources());
  }

}
