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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Attribute;
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

import static org.apache.commons.lang.StringUtils.*;
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
   * Generates a list of mule configurations, first it generates basic mule configurations (http listener, console
   * and apikit configuration).
   * Finally, if there are new/edited/deleted flow entries (resources in the API) they are updated below of the basics.
   *
   * @return List of mule configurations  (contains http listener, apikit router, console and flows). Typically one,
   * but could be many and have global configuration separately from main file.
   */
  public List<MuleConfig> generate() {
    Set<MuleConfig> muleConfigs = new HashSet<>();
    apis.forEach(api -> muleConfigs.addAll(generateBasicMuleConfiguration(api)));
    if (!flowEntries.isEmpty()) {
      flowEntries.forEach(this::generateForFlowEntries);
    }
    return new ArrayList<>(muleConfigs);
  }

  /**
   * Looks for main mule configuration file and updates apikit configuration in case needed. Adds both files to
   * mule configurations list if applies.
   * @param api container of the main mule application file
   * @return
   */
  private List<MuleConfig> generateBasicMuleConfiguration(ApikitMainFlowContainer api) {
    List<MuleConfig> muleConfigs = new ArrayList<>();
    MuleConfig mainMuleConfig = api.getMuleConfig() != null ? api.getMuleConfig() : createMuleConfig(api);
    MuleConfig apikitConfig = updateApikitConfig(api, mainMuleConfig);
    muleConfigs.add(mainMuleConfig);
    if (apikitConfig != null) {
      muleConfigs.add(apikitConfig);
    }
    return muleConfigs;
  }

  /**
   * Generates new flows based on the generation models previously created. Also updates apikit configuration in case
   * needed.
   * @param flowEntry new flow to be generated
   */
  private void generateForFlowEntries(GenerationModel flowEntry) {
    Element apikitFlowScope = new APIKitFlowScope(flowEntry, isMuleEE()).generate();
    MuleConfig mainMuleConfig = flowEntry.getApi().getMuleConfig();
    mainMuleConfig.getContentAsDocument().getRootElement().getContent().add(apikitFlowScope);
    mainMuleConfig.addFlow(new Flow(apikitFlowScope));
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
      return apikitConfigurations.filter(configuration -> containsApikitConfiguration(configRef, configuration)).findFirst()
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
  private boolean containsApikitConfiguration(String configRef, MuleConfig configuration) {
    Stream<APIKitConfig> apikitConfigurationStream = configuration.getApikitConfigs().stream();
    List<APIKitConfig> filteredApikitConfigurations = apikitConfigurationStream
        .filter(apikitConfig -> apikitConfig.getName().contains(configRef)).collect(Collectors.toList());
    return filteredApikitConfigurations.size() > 0;
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
    Element apikitConfigFromMuleConfig = lookForChildApikitConfig(apikitConfigMuleConfig);
    Element apikitConfigFromApi = api.getConfig().generate();
    MuleConfig apikitConfigResult = null;
    if (shouldUpdateApikitConfig(apikitConfigFromApi, apikitConfigFromMuleConfig)) {
      int index = apikitConfigMuleConfig.getContentAsDocument().getRootElement().indexOf(apikitConfigFromMuleConfig);
      apikitConfigMuleConfig.getContentAsDocument().getRootElement().removeContent(index);
      apikitConfigMuleConfig.getContentAsDocument().getRootElement().addContent(index, apikitConfigFromApi);
      return apikitConfigMuleConfig;
    }
    return null;
  }

  /**
   * Given a mule configuration, it looks for an element containing `config` for apikit
    * @param config mule configuration file
   * @return the element for the apikit configuration
   */
  private Element lookForChildApikitConfig(MuleConfig config) {
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
        .anyMatch(attribute -> lookForDifferences(apikitConfigFromMuleConfig.getAttribute(attribute.getName()), attribute));
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
        isNotEmpty(normalizedCurrentAttribute) && isNotEmpty(normalizedIncomingAttribute);
    return attributesExist && !normalizedCurrentAttribute.contains(normalizedIncomingAttribute);
  }

  /**
   * Normalize every path to single slash format
   * @param path path to check
   * @return normalized path
   */
  private String normalizePath(String path) {
    return isNotEmpty(path) ? path.replace("\\", "/") : path;
  }

}
