/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.*;
import org.mule.tools.apikit.output.scopes.*;

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
  private final List<ApikitMainFlowContainer> apis;
  private List<MuleConfig> muleConfigsInApp = new ArrayList<>();
  private ScaffolderContext scaffolderContext;
  private boolean showConsole;

  public MuleConfigGenerator(List<ApikitMainFlowContainer> apis, List<GenerationModel> flowEntries,
                             List<MuleConfig> muleConfigsInApp, ScaffolderContext scaffolderContext, boolean showConsole) {
    this.apis = apis;
    this.flowEntries = flowEntries;
    this.muleConfigsInApp.addAll(muleConfigsInApp);
    this.scaffolderContext = scaffolderContext;
    this.showConsole = showConsole;
  }

  public List<MuleConfig> generate() {
    List<MuleConfig> configs = new ArrayList<>();
    Set<MuleConfig> muleConfigs = new HashSet<>();
    if (flowEntries.isEmpty()) {
      apis.forEach(api -> {
        MuleConfig mainMuleConfig = api.getMuleConfig() != null ? api.getMuleConfig() : createMuleConfig(api);
        Optional<MuleConfig> apikitConfigMuleConfig = updateApikitConfig(api, mainMuleConfig);
        muleConfigs.add(mainMuleConfig);
        addApikitConfig(muleConfigs, apikitConfigMuleConfig);
      });
    } else {
      ApikitMainFlowContainer api = flowEntries.stream().findFirst().get().getApi();
      MuleConfig mainMuleConfig = api.getMuleConfig() != null ? api.getMuleConfig() : createMuleConfig(api);
      Optional<MuleConfig> apikitConfig = updateApikitConfig(api, mainMuleConfig);
      for (GenerationModel flowEntry : flowEntries) {
        Element apikitFlowScope = new APIKitFlowScope(flowEntry, isMuleEE()).generate();
        int newFlowPositionIndex = getLastFlowIndex(mainMuleConfig.getContentAsDocument()) + 1;
        mainMuleConfig.getContentAsDocument().getRootElement().getContent().add(newFlowPositionIndex, apikitFlowScope);
        mainMuleConfig.addFlow(new Flow(apikitFlowScope));
        muleConfigs.add(mainMuleConfig);
        this.addApikitConfig(muleConfigs, apikitConfig);
      }
    }
    configs.addAll(muleConfigs);
    return configs;
  }

  private void addApikitConfig(Set<MuleConfig> muleConfigs, Optional<MuleConfig> apikitConfigMuleConfig) {
    if (apikitConfigMuleConfig.isPresent()) {
      muleConfigs.add(apikitConfigMuleConfig.get());
    }
  }

  private MuleConfig retrieveApikitConfigMuleConfig(MuleConfig mainMuleConfig) {
    MuleConfig apikitConfigMuleConfig = mainMuleConfig;
    Optional<MuleConfig> apikitConfigMuleConfigOptional = Optional.empty();

    if (muleConfigsInApp.size() > 1) {
      String configRef = apikitConfigMuleConfig.getMainFlows().stream().findFirst().get().getApikitRouter().getConfigRef();
      Stream<MuleConfig> apikitConfigurations = muleConfigsInApp.stream().filter(muleConfig -> muleConfig.getContentAsDocument()
          .getRootElement().getChild("config", APIKitTools.API_KIT_NAMESPACE.getNamespace()) != null);
      apikitConfigMuleConfigOptional = apikitConfigurations.filter(configuration -> {
        Stream<APIKitConfig> apikitConfigurationStream = configuration.getApikitConfigs().stream();
        List<APIKitConfig> filteredApikitConfigurations = apikitConfigurationStream
            .filter(apikitConfig -> apikitConfig.getName().contains(configRef)).collect(Collectors.toList());
        return filteredApikitConfigurations.size() > 0;
      }).findFirst();
    }
    if (apikitConfigMuleConfigOptional.isPresent()) {
      apikitConfigMuleConfig = apikitConfigMuleConfigOptional.get();
    }
    return apikitConfigMuleConfig;
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

  private Optional<MuleConfig> updateApikitConfig(ApikitMainFlowContainer api, MuleConfig mainMuleConfig) {
    MuleConfig apikitConfigMuleConfig = retrieveApikitConfigMuleConfig(mainMuleConfig);
    Element apikitConfiFromMuleConfig = lookForApikitConfig(apikitConfigMuleConfig);
    Element apikitConfigFromApi = api.getConfig().generate();
    Optional<MuleConfig> apikitConfigResult = Optional.empty();
    if (shouldUpdateApikitConfig(apikitConfigFromApi, apikitConfiFromMuleConfig)) {
      int index = apikitConfigMuleConfig.getContentAsDocument().getRootElement().indexOf(apikitConfiFromMuleConfig);
      apikitConfigMuleConfig.getContentAsDocument().getRootElement().removeContent(index);
      apikitConfigMuleConfig.getContentAsDocument().getRootElement().addContent(index, apikitConfigFromApi);
      apikitConfigResult = Optional.of(apikitConfigMuleConfig);
    }
    return apikitConfigResult;
  }

  private Element lookForApikitConfig(MuleConfig config) {
    return config.getContentAsDocument().getRootElement().getChild("config", APIKitTools.API_KIT_NAMESPACE.getNamespace());
  }

  public MuleConfig createMuleConfig(ApikitMainFlowContainer api) {
    Document document = new Document();
    document.setRootElement(new MuleScope(false, false).generate());
    MuleConfig mConfig = MuleConfigBuilder.fromDoc(document);
    mConfig.setName(api.getId() + ".xml");
    setDefaultApikitAndListenersConfigs(api, mConfig);

    MuleConfig config = MuleConfigBuilder.fromDoc(mConfig.buildContent());
    config.setName(mConfig.getName());
    api.setMuleConfig(config);
    muleConfigsInApp.add(config);
    return config;
  }

  private void generateAPIKitAndListenerConfig(ApikitMainFlowContainer api, MuleConfig muleConfig) {
    if (!muleConfig.getHttpListenerConfigs().contains(api.getHttpListenerConfig())) {
      muleConfig.addHttpListener(api.getHttpListenerConfig());
    }
    api.setPath(APIKitTools.addAsteriskToPath(api.getPath()));

    muleConfig.addConfig(api.getConfig());
    muleConfig.addFlow(new Flow(new FlowScope(api, isMuleEE()).generate()));
    if (showConsole) {
      muleConfig.addFlow(new Flow(new ConsoleFlowScope(api, isMuleEE()).generate()));
    }
  }

  private boolean isMuleEE() {
    return scaffolderContext.getRuntimeEdition() == EE;
  }

  // it checks both elements have the same attributes
  private boolean shouldUpdateApikitConfig(Element apikitConfigFromApi, Element apikitConfigFromMuleConfig) {
    boolean shouldUpdate = apikitConfigFromMuleConfig == null;
    for (Attribute attr : apikitConfigFromApi.getAttributes()) {
      Attribute muleConfigAttr = apikitConfigFromMuleConfig.getAttribute(attr.getName());
      shouldUpdate =
          shouldUpdate || muleConfigAttr == null || this.attributeHasChanged(attr.getValue(), muleConfigAttr.getValue());
    }
    return shouldUpdate;
  }

  private boolean attributeHasChanged(String currentAttribute, String incomingAttribute) {
    Optional<String> normalizedCurrentAttribute = this.normalizePath(currentAttribute);
    Optional<String> normalizedIncomingAttribute = this.normalizePath(incomingAttribute);
    boolean hasChanged = false;
    boolean attributesExist = normalizedCurrentAttribute.isPresent() && normalizedIncomingAttribute.isPresent();
    if (attributesExist) {
      hasChanged = !normalizedCurrentAttribute.get().contains(normalizedIncomingAttribute.get());
    }
    return hasChanged;
  }

  private Optional<String> normalizePath(String path) {
    Optional<String> result = Optional.empty();
    if (StringUtils.isNotEmpty(path)) {
      result = Optional.of(path.replace("\\", "/"));
    }
    return result;
  }

}
