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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.Flow;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleConfigBuilder;
import org.mule.tools.apikit.model.ScaffolderContext;
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

  /**
   * Generates a list of mule configurations, one way is the basic, when there aren't new resources in the API.
   * Other way is when there are new flow entries (resources in the API) and should be updated along with the basics.
   * NOTE: newFlowPositionIndex is AtomicInteger because inside forEach values cannot be updated dynamically (int += 1)
   * and must be final.
   *
   * @return List of mule configurations  (contains http listener, apikit router, console and flows). Typically one,
   * but could be many and have global configuration separatelly from main file.
   */
  public List<MuleConfig> generate() {
    Set<MuleConfig> muleConfigs = new HashSet<>();
    if (flowEntries.isEmpty()) {
      apis.forEach(api -> generateBasicMuleConfiguration(muleConfigs, api));
    } else {
      ApikitMainFlowContainer api = flowEntries.stream().findFirst().get().getApi();
      MuleConfig mainMuleConfig = retrieveMuleConfig(api);
      MuleConfig apikitConfig = updateApikitConfig(api, mainMuleConfig);
      AtomicInteger newFlowPositionIndex = new AtomicInteger(getLastFlowIndex(mainMuleConfig.getContentAsDocument()));
      flowEntries.stream().forEach(flowEntry -> generateForFlowEntries(muleConfigs, mainMuleConfig, apikitConfig,
                                                                       newFlowPositionIndex, flowEntry));
    }
    return new ArrayList<>(muleConfigs);
  }

  /**
   * Generates new flows based on the generation models previously created. Also updates apikit configuration in case
   * needed.
   * @param muleConfigs generated mule configurations
   * @param mainMuleConfig main mule configuration file
   * @param apikitConfig apikit configuration file
   * @param newFlowPositionIndex index from where new flow should be created
   * @param flowEntry new flow to be generated
   */
  private void generateForFlowEntries(Set<MuleConfig> muleConfigs, MuleConfig mainMuleConfig, MuleConfig apikitConfig,
                                      AtomicInteger newFlowPositionIndex, GenerationModel flowEntry) {
    Element apikitFlowScope = new APIKitFlowScope(flowEntry, isMuleEE()).generate();
    newFlowPositionIndex.addAndGet(1);
    mainMuleConfig.getContentAsDocument().getRootElement().getContent().add(newFlowPositionIndex.get(), apikitFlowScope);
    mainMuleConfig.addFlow(new Flow(apikitFlowScope));
    muleConfigs.add(mainMuleConfig);
    addApikitConfig(muleConfigs, apikitConfig);
  }

  /**
   * Looks for main mule configuration file and updates apikit configuration in case needed. Adds both files to
   * mule configurations if applies.
   * @param muleConfigs generated mule configurations
   * @param api container of the main mule application file
   */
  private void generateBasicMuleConfiguration(Set<MuleConfig> muleConfigs, ApikitMainFlowContainer api) {
    MuleConfig mainMuleConfig = retrieveMuleConfig(api);
    MuleConfig apikitConfigMuleConfig = updateApikitConfig(api, mainMuleConfig);
    muleConfigs.add(mainMuleConfig);
    addApikitConfig(muleConfigs, apikitConfigMuleConfig);
  }

  /**
   * Determines whether to use existing mule configuration or create a new one.
   * @param api container of the main mule application file
   * @return new or existing mule configuration
   */
  private MuleConfig retrieveMuleConfig(ApikitMainFlowContainer api) {
    return api.getMuleConfig() != null ? api.getMuleConfig() : createMuleConfig(api);
  }

  /**
   * Adds apikit configuration only if it exists
   * @param muleConfigs set of mule configuration
   * @param apikitConfig possibly new apikit configuration
   */
  private void addApikitConfig(Set<MuleConfig> muleConfigs, MuleConfig apikitConfig) {
    if (apikitConfig != null) {
      muleConfigs.add(apikitConfig);
    }
  }

  /**
   * It looks for the first (and only) apikit router configuration reference within the mainMuleConfig
   * received by parameter. Then with that it looks inside all of the files of the app for the file which
   * calls that reference.
   * @param mainMuleConfig the configuration which contains the reference to the apikit configuration.
   * @return the file on which apikit configuration is made
   */
  private MuleConfig retrieveApikitConfigMuleConfig(MuleConfig mainMuleConfig) {
    if (muleConfigsInApp.size() > 1) {
      String configRef = mainMuleConfig.getMainFlows().stream().findFirst().get().getApikitRouter().getConfigRef();
      Stream<MuleConfig> apikitConfigurations = muleConfigsInApp.stream().filter(muleConfig -> muleConfig.getContentAsDocument()
          .getRootElement().getChild("config", APIKitTools.API_KIT_NAMESPACE.getNamespace()) != null);
      return apikitConfigurations.filter(configuration -> filterConfigurations(configRef, configuration)).findFirst()
          .orElse(mainMuleConfig);
    }
    return mainMuleConfig;
  }

  /**
   * Given an apikit configuration reference and a configuration, it looks for the file which contains
   * the apikit configuration.
   * @param configRef apikit configuration reference
   * @param configuration file from the app - could contain apikit configuration
   * @return true/false depending on existence of any file matching the configuration.
   */
  private boolean filterConfigurations(String configRef, MuleConfig configuration) {
    Stream<APIKitConfig> apikitConfigurationStream = configuration.getApikitConfigs().stream();
    List<APIKitConfig> filteredApikitConfigurations = apikitConfigurationStream
        .filter(apikitConfig -> apikitConfig.getName().contains(configRef)).collect(Collectors.toList());
    return filteredApikitConfigurations.size() > 0;
  }

  /**
   * Given a document it looks for the last element from its content which is a flow
   * @param document mule xml document file
   * @return
   */
  private int getLastFlowIndex(Document document) {
    int lastFlowIndex = 0;
    //    Set<Integer> set = ContiguousSet.create(Range.closed(0, document.getRootElement().getContentSize()), DiscreteDomain.integers());
    //    set.stream().filter(element -> )
    for (int i = 0; i < document.getRootElement().getContentSize(); i++) {
      Content content = document.getRootElement().getContent(i);
      if (content instanceof Element && "flow".equals(((Element) content).getName())) {
        lastFlowIndex = i;
      }
    }
    return lastFlowIndex;
  }

  /**
   * Looks for an existing apikit configuration file, grabs the element from it, then the element from the incoming
   * API. It checks if it should update (based on differences between existing and incoming configuration).
   * Finally applies the change and returns existing configuration.
   * @param api container of the main mule application file
   * @param mainMuleConfig main mule configuration (contains http listener, apikit router, console and flows)
   * @return an existing configuration or null
   */
  private MuleConfig updateApikitConfig(ApikitMainFlowContainer api, MuleConfig mainMuleConfig) {
    MuleConfig apikitConfigMuleConfig = retrieveApikitConfigMuleConfig(mainMuleConfig);
    Element apikitConfiFromMuleConfig = lookForApikitConfig(apikitConfigMuleConfig);
    Element apikitConfigFromApi = api.getConfig().generate();
    MuleConfig apikitConfigResult = null;
    if (shouldUpdateApikitConfig(apikitConfigFromApi, apikitConfiFromMuleConfig)) {
      int index = apikitConfigMuleConfig.getContentAsDocument().getRootElement().indexOf(apikitConfiFromMuleConfig);
      apikitConfigMuleConfig.getContentAsDocument().getRootElement().removeContent(index);
      apikitConfigMuleConfig.getContentAsDocument().getRootElement().addContent(index, apikitConfigFromApi);
      apikitConfigResult = apikitConfigMuleConfig;
    }
    return apikitConfigResult;
  }

  /**
   * Given a mule configuration, it looks for an element containing `config` for apikit
    * @param config mule configuration file
   * @return the element for the apikit configuration
   */
  private Element lookForApikitConfig(MuleConfig config) {
    return config.getContentAsDocument().getRootElement().getChild("config", APIKitTools.API_KIT_NAMESPACE.getNamespace());
  }

  /**
   * It creates a document for the new mule configuration, then builds it. Sets its name and adds it to the
   * existing mule configurations of the application.
   * @param api container of the main mule application file
   * @return new mule configuration
   */
  public MuleConfig createMuleConfig(ApikitMainFlowContainer api) {
    Document muleConfigContent = createMuleConfigContent(api);
    MuleConfig config = MuleConfigBuilder.fromDoc(muleConfigContent);
    config.setName(createMuleConfigID(api.getId()));
    api.setMuleConfig(config);
    muleConfigsInApp.add(config);
    return config;
  }

  /**
   * Creates a document containing a name, apikit configuration, http listener flow and console flow
   * @param api container of the main mule application file
   * @return a document to build a mule configuration
   */
  private Document createMuleConfigContent(ApikitMainFlowContainer api) {
    Document document = new Document();
    document.setRootElement(new MuleScope(false, false).generate());
    MuleConfig muleConfig = MuleConfigBuilder.fromDoc(document);
    muleConfig.setName(createMuleConfigID(api.getId()));
    addApikitConfiguration(api, muleConfig);
    api.setPath(APIKitTools.addAsteriskToPath(api.getPath()));
    muleConfig.addFlow(new Flow(new FlowScope(api, isMuleEE()).generate()));
    addHttpListenerConfiguration(api, muleConfig);
    addConsoleFlow(api, muleConfig);
    return muleConfig.buildContent();
  }

  /**
   * Concatenates an id with suffix to create a mule configuration id.
   * @param id id of apikit main flow container
   * @return new mule configuration id
   */
  private String createMuleConfigID(String id) {
    return id + ".xml";
  }

  /**
   * Creates a new apikit configuration and adds it to the mule configuration file.
   * @param api container of the main mule application file
   * @param muleConfig main mule configuration (contains http listener, apikit router, console and flows)
   */
  private void addApikitConfiguration(ApikitMainFlowContainer api, MuleConfig muleConfig) {
    if (api.getConfig() == null) {
      APIKitConfig apikitConfig = new APIKitConfig();
      apikitConfig.setApi(api.getApiFilePath());
      apikitConfig.setName(api.getId() + "-" + APIKitConfig.DEFAULT_CONFIG_NAME);
      api.setConfig(apikitConfig);
      muleConfig.addConfig(api.getConfig());
    }
  }

  /**
   * Adds http listener configuration if it doesn't already exist
   * @param api container of the main mule application file
   * @param muleConfig main mule configuration (contains http listener, apikit router, console and flows)
   */
  private void addHttpListenerConfiguration(ApikitMainFlowContainer api, MuleConfig muleConfig) {
    if (!muleConfig.getHttpListenerConfigs().contains(api.getHttpListenerConfig())) {
      muleConfig.addHttpListener(api.getHttpListenerConfig());
    }
  }

  /**
   * Adds console flow only if it is toggled on.
   * @param api container of the main mule application file
   * @param muleConfig main mule configuration (contains http listener, apikit router, console and flows)
   */
  private void addConsoleFlow(ApikitMainFlowContainer api, MuleConfig muleConfig) {
    if (showConsole) {
      muleConfig.addFlow(new Flow(new ConsoleFlowScope(api, isMuleEE()).generate()));
    }
  }

  /**
   * Determines runtime edition
   * @return true for EE / false for CE
   */
  private boolean isMuleEE() {
    return scaffolderContext.getRuntimeEdition() == EE;
  }

  /**
   * Conditions for updating apikit configurations: apikit configuration from the file is null | any attribute
   * is null or has changed.
   * @param apikitConfigFromApi incoming new element for apikit configuration
   * @param apikitConfigFromMuleConfig existing element for apikit configuration
   * @return true for conditions explained above, false any other
   */
  private boolean shouldUpdateApikitConfig(Element apikitConfigFromApi, Element apikitConfigFromMuleConfig) {
    return apikitConfigFromMuleConfig == null || apikitConfigFromApi.getAttributes().stream()
        .filter(attribute -> lookForDifferences(apikitConfigFromMuleConfig.getAttribute(attribute.getName()), attribute))
        .findAny().isPresent();
  }

  /**
   * Conditions for attributes to be different: existing attribute not existing, or attribute has changed.
   * @param existingAttribute existing attribute
   * @param attribute new attribute potentially different
   * @return true for conditions explained above, false any other
   */
  private boolean lookForDifferences(Attribute existingAttribute, Attribute attribute) {
    return existingAttribute == null || attributeHasChanged(attribute.getValue(), existingAttribute.getValue());
  }

  /**
   * Checks if existing attribute contains incoming. If it does, then it hasn't changed, otherwise attribute has changed.
   * @param currentAttribute existing attribute
   * @param incomingAttribute attribute that could be different than existing one
   * @return true for conditions explained above, false any other
   */
  private boolean attributeHasChanged(String currentAttribute, String incomingAttribute) {
    String normalizedCurrentAttribute = normalizePath(currentAttribute);
    String normalizedIncomingAttribute = normalizePath(incomingAttribute);
    boolean attributesExist =
        StringUtils.isNotEmpty(normalizedCurrentAttribute) && StringUtils.isNotEmpty(normalizedIncomingAttribute);
    return attributesExist && !normalizedCurrentAttribute.contains(normalizedIncomingAttribute);
  }

  /**
   * Normalize every path to single slash format
   * @param path path to check
   * @return normalized path
   */
  private String normalizePath(String path) {
    String result = path;
    if (StringUtils.isNotEmpty(path)) {
      result = path.replace("\\", "/");
    }
    return result;
  }

}
