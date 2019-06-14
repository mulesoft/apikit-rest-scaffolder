/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.Flow;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleConfigBuilder;
import org.mule.tools.apikit.model.ScaffolderResource;
import org.mule.tools.apikit.model.ScaffoldingError;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.output.scopes.APIKitFlowScope;
import org.mule.tools.apikit.output.scopes.ConsoleFlowScope;
import org.mule.tools.apikit.output.scopes.FlowScope;
import org.mule.tools.apikit.output.scopes.MuleScope;

import static org.mule.tools.apikit.model.RuntimeEdition.EE;

public class MuleConfigGenerator {

  public static final NamespaceWithLocation XMLNS_NAMESPACE = new NamespaceWithLocation(
                                                                                        Namespace
                                                                                            .getNamespace("http://www.mulesoft.org/schema/mule/core"),
                                                                                        "http://www.mulesoft.org/schema/mule/core/current/mule.xsd");
  public static final NamespaceWithLocation XSI_NAMESPACE = new NamespaceWithLocation(
                                                                                      Namespace
                                                                                          .getNamespace("xsi",
                                                                                                        "http://www.w3.org/2001/XMLSchema-instance"),
                                                                                      null);
  public static final NamespaceWithLocation HTTP_NAMESPACE = new NamespaceWithLocation(
                                                                                       Namespace
                                                                                           .getNamespace("http",
                                                                                                         "http://www.mulesoft.org/schema/mule/http"),
                                                                                       "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd");

  public static final NamespaceWithLocation EE_NAMESPACE = new NamespaceWithLocation(
                                                                                     Namespace
                                                                                         .getNamespace("ee",
                                                                                                       "http://www.mulesoft.org/schema/mule/ee/core"),
                                                                                     "http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd");

  private final List<GenerationModel> flowEntries;
  private final RuntimeEdition runtimeEdition;
  private final List<ApikitMainFlowContainer> apis;
  private List<MuleConfig> muleConfigsInApp = new ArrayList<>();

  private final List<ScaffoldingError> scaffoldingErrors = new ArrayList<>();
  private final List<ScaffolderResource> generatedResources = new ArrayList<>();

  public MuleConfigGenerator(List<ApikitMainFlowContainer> apis, List<GenerationModel> flowEntries,
                             List<MuleConfig> muleConfigsInApp, RuntimeEdition runtimeEdition) {
    this.apis = apis;
    this.flowEntries = flowEntries;
    this.runtimeEdition = runtimeEdition;
    this.muleConfigsInApp.addAll(muleConfigsInApp);
  }

  public List<ScaffoldingError> getScaffoldingErrors() {
    return scaffoldingErrors;
  }

  public List<ScaffolderResource> getGeneratedResources() {
    return generatedResources;
  }

  public List<MuleConfig> generate() {
    List<MuleConfig> configs = new ArrayList<>();
    if (flowEntries.isEmpty()) {
      apis.forEach(api -> {
        MuleConfig muleConfig = api.getMuleConfig() != null ? api.getMuleConfig() : createMuleConfig(api);
        configs.add(muleConfig);
      });
    } else {
      Set<MuleConfig> muleConfigs = new HashSet<>();
      ApikitMainFlowContainer api = flowEntries.get(0).getApi();
      MuleConfig muleConfig = api.getMuleConfig() != null ? api.getMuleConfig() : createMuleConfig(api);

      for (GenerationModel flowEntry : flowEntries) {
        Element apikitFlowScope = new APIKitFlowScope(flowEntry, isMuleEE()).generate();

        int newFlowPositionIndex = getLastFlowIndex(muleConfig.getContentAsDocument()) + 1;
        muleConfig.getContentAsDocument().getRootElement().getContent().add(newFlowPositionIndex, apikitFlowScope);
        muleConfig.addFlow(new Flow(apikitFlowScope));
        updateApikitConfig(api, muleConfig);
        muleConfigs.add(muleConfig);
      }
      configs.addAll(muleConfigs);
    }
    return configs;
  }

  private void setDefaultApikitAndListenersConfigs(ApikitMainFlowContainer api, MuleConfig muleConfig) {
    if (api.getConfig() == null || api.getHttpListenerConfig() == null) {
      if (api.getConfig() == null) {
        api.setDefaultAPIKitConfig();
      }
      generateAPIKitAndListenerConfig(api, muleConfig);
    }
  }

  private int getLastFlowIndex(Document doc) {
    int lastFlowIndex = 0;
    for (int i = 0; i < doc.getRootElement().getContentSize(); i++) {
      Content content = doc.getRootElement().getContent(i);
      if (content instanceof Element && "flow".equals(((Element) content).getName())) {
        lastFlowIndex = i;
      }
    }
    return lastFlowIndex;
  }

  private void updateApikitConfig(ApikitMainFlowContainer api, MuleConfig config) {
    Element apikitConfiFromMuleConfig =
        config.getContentAsDocument().getRootElement().getChild("config", APIKitTools.API_KIT_NAMESPACE.getNamespace());
    Element apikitConfigFromApi = api.getConfig().generate();

    if (shouldUpdateApikitConfig(apikitConfigFromApi, apikitConfiFromMuleConfig)) {
      int index = config.getContentAsDocument().getRootElement().indexOf(apikitConfiFromMuleConfig);
      config.getContentAsDocument().getRootElement().removeContent(index);
      config.getContentAsDocument().getRootElement().addContent(index, apikitConfigFromApi);
    }
  }

  public MuleConfig createMuleConfig(ApikitMainFlowContainer api) {
    Document document = new Document();
    document.setRootElement(new MuleScope(false).generate());
    MuleConfig mConfig = MuleConfigBuilder.fromDoc(document);
    setDefaultApikitAndListenersConfigs(api, mConfig);

    MuleConfig config = MuleConfigBuilder.fromDoc(mConfig.buildContent());
    api.setMuleConfig(config);
    muleConfigsInApp.add(config);
    return config;
  }

  private void generateAPIKitAndListenerConfig(ApikitMainFlowContainer api, MuleConfig muleConfig) {
    if (!muleConfig.getHttpListenerConfigs().contains(api.getHttpListenerConfig())) {
      muleConfig.addHttpListener(api.getHttpListenerConfig());
    }
    api.setPath(APIKitTools.addAsteriskToPath(api.getPath()));

    muleConfig.putApikitConfig(api.getConfig().getName(), api.getConfig());
    muleConfig.addFlow(new Flow(new FlowScope(api, isMuleEE()).generate()));
    muleConfig.addFlow(new Flow(new ConsoleFlowScope(api, isMuleEE()).generate()));
  }

  private boolean isMuleEE() {
    return runtimeEdition == EE;
  }

  // it checks both elements have the same attributes
  private boolean shouldUpdateApikitConfig(Element apikitConfigFromApi, Element apikitConfigFromMuleConfig) {
    for (Attribute attr : apikitConfigFromApi.getAttributes()) {
      Attribute muleConfigAttr = apikitConfigFromMuleConfig.getAttribute(attr.getName());
      if (muleConfigAttr == null) {
        return true;
      }

      if (!attr.getValue().equals(muleConfigAttr.getValue())) {
        return true;
      }
    }
    return false;
  }

}
