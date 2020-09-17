/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;


import org.apache.commons.collections.CollectionUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.APIAutodiscoveryConfig;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.CustomConfiguration;
import org.mule.tools.apikit.model.Flow;
import org.mule.tools.apikit.model.MainFlow;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.output.scopes.APIKitFlowScope;
import org.mule.tools.apikit.output.scopes.ConsoleFlowScope;
import org.mule.tools.apikit.output.scopes.FlowScope;
import org.mule.tools.apikit.output.scopes.MuleScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.mule.tools.apikit.model.MuleConfigBuilder.fromDoc;
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

  private static final String DEFAULT_APIKIT_CONFIG_NAME = "no_named_config";

  private final List<GenerationModel> flowEntriesDiff;
  private final List<ApikitMainFlowContainer> apiContainers;
  private List<MuleConfig> muleConfigsInApp = new ArrayList<>();
  private ScaffolderContext scaffolderContext;
  private boolean showConsole;
  private CustomConfiguration customConfiguration;

  /**
   * @param apiContainers     Information about APIs being scaffolded
   * @param flowEntriesDiff   New flows that needs to be added
   * @param muleConfigsInApp  Pre-existing Mule configurations
   * @param scaffolderContext Scaffolder context information
   * @param includeConsole    Whether console should be included or not
   */
  public MuleConfigGenerator(List<ApikitMainFlowContainer> apiContainers, List<GenerationModel> flowEntriesDiff,
                             List<MuleConfig> muleConfigsInApp, ScaffolderContext scaffolderContext, boolean includeConsole,
                             CustomConfiguration customConfiguration) {
    this.apiContainers = apiContainers;
    this.flowEntriesDiff = flowEntriesDiff;
    this.muleConfigsInApp.addAll(muleConfigsInApp);
    this.scaffolderContext = scaffolderContext;
    this.showConsole = includeConsole;
    this.customConfiguration = customConfiguration;
  }

  /**
   * Generates or updates Mule configurations
   *
   * @return list of new or updated Mule configurations
   */
  public List<MuleConfig> generate() {
    Set<MuleConfig> updatedMuleConfigs = getConfigsFromApiContainers();
    generateMissingFlowEntriesInMuleConfigs();
    Map<String, MuleConfig> indexByApikitConfigName = createIndexOfConfigurationsWithApikitConfig();
    if (isEmpty(indexByApikitConfigName.entrySet())) {
      // No existing config that needs to be checked for an update
      return new ArrayList<>(updatedMuleConfigs);
    }
    Map<String, Element> indexOfApikitRouterConfigByRefName = createIndexOfRouterReferences();
    if (isEmpty(indexOfApikitRouterConfigByRefName.entrySet())) {
      // No existing references to apikit configs that needs to be checked for an update
      return new ArrayList<>(updatedMuleConfigs);
    }
    if (!isThereConfigsInIntersection(indexByApikitConfigName, indexOfApikitRouterConfigByRefName)) {
      // No cross references found for configs
      return new ArrayList<>(updatedMuleConfigs);
    }
    updatedMuleConfigs.addAll(getCrossReferencedConfigsUpdated(indexByApikitConfigName, indexOfApikitRouterConfigByRefName));
    return new ArrayList<>(updatedMuleConfigs);
  }

  /**
   * Whether there is a config to update in an existing configuration.
   *
   * @param muleConfigsContainingApikitConfig
   * @param muleConfigsReferencingApikitConfig
   * @return true is api references to an existing configuration.
   */
  private boolean isThereConfigsInIntersection(Map<String, MuleConfig> muleConfigsContainingApikitConfig,
                                               Map<String, Element> muleConfigsReferencingApikitConfig) {
    Set<String> muleConfigsCandidateToUpdate = muleConfigsContainingApikitConfig.keySet();
    muleConfigsCandidateToUpdate.retainAll(muleConfigsReferencingApikitConfig.keySet());
    return isNotEmpty(muleConfigsCandidateToUpdate);
  }

  /**
   * Generates the new flow entries for the existing or new APIs
   */
  private void generateMissingFlowEntriesInMuleConfigs() {
    if (!flowEntriesDiff.isEmpty()) {
      flowEntriesDiff.forEach(this::generateFlowEntryInMuleConfig);
    }
  }

  /**
   * Get existing Apikit configuration names mapped to the Mule configuration that contains it.
   *
   * @return Map of Apikit configuration names and Mule configurations
   */
  private Map<String, MuleConfig> createIndexOfConfigurationsWithApikitConfig() {
    Map<String, MuleConfig> configsWithApikitConfig = new HashMap<>();
    List<Element> apikitConfigElements;
    String apikitConfigName;
    for (MuleConfig muleConfig : muleConfigsInApp) {
      apikitConfigElements = getApikitConfigDocumentElement(muleConfig);
      if (isEmpty(apikitConfigElements)) {
        continue;
      }
      for (Element configElement : apikitConfigElements) {
        apikitConfigName = configElement.getAttributeValue("name");
        if (apikitConfigName == null) {
          apikitConfigName = DEFAULT_APIKIT_CONFIG_NAME;
        }
        configsWithApikitConfig.put(apikitConfigName, muleConfig);
      }
    }
    return configsWithApikitConfig;
  }

  /**
   * Get the reference names of Apikit configurations that are being scaffolded mapped to the document element that contains it.
   *
   * @return Map of Apikit config references and document element
   */
  private Map<String, Element> createIndexOfRouterReferences() {
    Map<String, Element> refsToApikitConfig = new HashMap<>();
    List<MainFlow> mainFlows = new ArrayList<>();
    String referenceName;
    for (ApikitMainFlowContainer api : apiContainers) {
      MuleConfig mainMuleConfig = getMainMuleConfig(api);
      if (mainMuleConfig != null) {
        mainFlows = mainMuleConfig.getMainFlows().stream().filter(flow -> flow.getApikitRouter() != null).collect(toList());
      }
      if (isNotEmpty(mainFlows)) {
        for (MainFlow mainFlow : mainFlows) {
          referenceName = mainFlow.getApikitRouter().getContent().getAttributeValue("config-ref");
          if (referenceName == null) {
            referenceName = DEFAULT_APIKIT_CONFIG_NAME;
          }
          refsToApikitConfig.put(referenceName, api.getConfig().generate());
        }
      }
    }
    return refsToApikitConfig;
  }

  private MuleConfig getMainMuleConfig(ApikitMainFlowContainer api) {
    List<MuleConfig> muleConfigs = api.getMuleConfig();
    MuleConfig existingMuleConfig = muleConfigs.stream().findFirst().get();
    return muleConfigs.stream().filter(muleConfig -> CollectionUtils.isNotEmpty(muleConfig.getMainFlows())).findFirst()
        .orElse(existingMuleConfig);
  }

  /**
   * Returns the set of Mule configurations that were updated.
   *
   * @param configsContainingApikitConfig
   * @param apikitConfigsCandidateToUpdate
   * @return Update Mule configurations or none.
   */
  private Set<MuleConfig> getCrossReferencedConfigsUpdated(Map<String, MuleConfig> configsContainingApikitConfig,
                                                           Map<String, Element> apikitConfigsCandidateToUpdate) {
    Set<MuleConfig> updatedMuleConfigs = new HashSet<>();
    MuleConfig updatedMuleConfig;
    for (Entry<String, MuleConfig> muleConfigRef : configsContainingApikitConfig.entrySet()) {
      updatedMuleConfig = getUpdatedMuleConfig(muleConfigRef.getValue(),
                                               apikitConfigsCandidateToUpdate.get(muleConfigRef.getKey()));
      if (updatedMuleConfig != null) {
        updatedMuleConfigs.add(updatedMuleConfig);
      }
    }
    return updatedMuleConfigs;
  }

  /**
   * Returns the updated Mule Config or null if no changes were done in the config.
   *
   * @param muleConfig
   * @param newApikitConfigFromApi
   * @return updated Mule config or null
   */
  private MuleConfig getUpdatedMuleConfig(MuleConfig muleConfig, Element newApikitConfigFromApi) {
    MuleConfig updatedMuleConfig = null;
    List<Element> preExistingApikitConfigs = getApikitConfigDocumentElement(muleConfig);
    for (Element preExistingApikitConfig : preExistingApikitConfigs) {
      if (shouldUpdateApikitConfig(newApikitConfigFromApi, preExistingApikitConfig)) {
        replaceExistingConfigWithNew(newApikitConfigFromApi, preExistingApikitConfig, muleConfig);
        updatedMuleConfig = muleConfig;
      }
    }
    return updatedMuleConfig;
  }

  /**
   * Returns the set of Mule configurations for the APIs being scaffolded. In configurations does not exist, it generates one.
   *
   * @return Mule configurations for APIs
   */
  private Set<MuleConfig> getConfigsFromApiContainers() {
    Set<MuleConfig> muleConfigs = new HashSet<>();
    for (ApikitMainFlowContainer api : apiContainers) {
      FlowScope flowScope = null;
      MuleConfig globals = null;
      if (customConfiguration.getExternalConfigurationFile().isPresent()) {
        globals = createCommonPropertiesFile(api);
        muleConfigs.add(globals);
        APIKitConfig apiKitConfig = lookForAPIKitConfig(globals, api);
        flowScope = new FlowScope(api, isMuleEE(), globals.getName());
      }
      MuleConfig muleConfig = api.getMuleConfig() == null ? createMuleConfig(api, flowScope) : getMainMuleConfig(api);
      MuleConfig updatedConfig = createAPIAutodiscoveryConfig(muleConfig, globals);
      muleConfigs.add(updatedConfig);
    }
    return muleConfigs;
  }


  private APIKitConfig lookForAPIKitConfig(MuleConfig globals, ApikitMainFlowContainer api) {
    if (api.getMuleConfig() != null) {
      return api.getMuleConfig().stream()
          .filter(muleConfig -> CollectionUtils.isNotEmpty(muleConfig.getApikitConfigs())).findAny().orElse(globals)
          .getApikitConfigs().stream().findAny().get();
    }
    return globals.getApikitConfigs().stream().findAny().get();
  }

  private MuleConfig createAPIAutodiscoveryConfig(MuleConfig muleConfig, MuleConfig globals) {
    MuleConfig configWhereToCreate = customConfiguration.getExternalConfigurationFile().isPresent() ? muleConfig : globals;
    Optional<MainFlow> mainFlow =
        muleConfig.getMainFlows().stream().filter(mainFlowElement -> mainFlowElement.getName().endsWith("-main")).findAny();
    String mainFlowRef = mainFlow.isPresent() ? mainFlow.get().getName() : null;
    if (customConfiguration.getApiAutodiscoveryID().isPresent()) {
      APIAutodiscoveryConfig apiAutodiscoveryConfig = new APIAutodiscoveryConfig();
      apiAutodiscoveryConfig.setFlowRef(mainFlowRef);
      apiAutodiscoveryConfig.setApiId(customConfiguration.getApiAutodiscoveryID().get());
      apiAutodiscoveryConfig.setIgnoreBasePath(Boolean.valueOf(APIAutodiscoveryConfig.IGNORE_BASE_PATH_DEFAULT));
      configWhereToCreate.setApiAutodiscoveryConfig(apiAutodiscoveryConfig);
    }
    return fromDoc(configWhereToCreate.buildContent());
  }

  /**
   * Generates new flows based on the generation models previously created. Also updates apikit configuration in case
   * needed.
   *
   * @param flowEntry new flow to be generated
   */
  private void generateFlowEntryInMuleConfig(GenerationModel flowEntry) {
    Element apikitFlowScope = new APIKitFlowScope(flowEntry, isMuleEE()).generate();
    MuleConfig mainMuleConfig = getMainMuleConfig(flowEntry.getApi());
    mainMuleConfig.getContentAsDocument().getRootElement().getContent().add(apikitFlowScope);
    mainMuleConfig.addFlow(new Flow(apikitFlowScope));
  }

  /**
   * Replaces the pre-existent Apikit configuration element with the new one.
   *
   * @param newApikitConfigFromApi
   * @param preExistingApikitConfig
   * @param preExistingMuleConfig
   */
  private void replaceExistingConfigWithNew(Element newApikitConfigFromApi, Element preExistingApikitConfig,
                                            MuleConfig preExistingMuleConfig) {
    int index = preExistingMuleConfig.getContentAsDocument().getRootElement().indexOf(preExistingApikitConfig);
    preExistingMuleConfig.getContentAsDocument().getRootElement().removeContent(index);
    preExistingMuleConfig.getContentAsDocument().getRootElement().addContent(index, newApikitConfigFromApi);
  }

  /**
   * Returns the children elements of type apikit:config from the configuration
   *
   * @param config
   * @return list of apikit:config elements
   */
  private List<Element> getApikitConfigDocumentElement(MuleConfig config) {
    return config.getContentAsDocument().getRootElement().getChildren("config", APIKitTools.API_KIT_NAMESPACE.getNamespace());
  }

  /**
   * It creates a document for the new mule configuration, then builds it. Sets its name and adds it to the
   * existing mule configurations of the application.
   *
   * @param api       container of the main mule application file
   * @param flowScope helps creating the flow of the application
   * @return new mule configuration
   */
  public MuleConfig createMuleConfig(ApikitMainFlowContainer api, FlowScope flowScope) {
    Document muleConfigContent = createMuleConfigContent(api, flowScope);
    MuleConfig config = fromDoc(muleConfigContent);
    config.setName(createMuleConfigID(api.getId()));
    List<MuleConfig> muleConfigs = new ArrayList<>();
    muleConfigs.add(config);
    api.setMuleConfig(muleConfigs);
    return config;
  }

  /**
   * Creates a document containing a name,  http/apikit configuration if applies, http listener flow and console flow
   *
   * @param api       container of the main mule application file
   * @param flowScope helps creating the flow of the application
   * @return a document to build a mule configuration
   */
  private Document createMuleConfigContent(ApikitMainFlowContainer api, FlowScope flowScope) {
    Document document = new Document();
    document.setRootElement(new MuleScope(false, false).generate());
    MuleConfig muleConfig = fromDoc(document);
    muleConfig.setName(createMuleConfigID(api.getId()));
    if (!customConfiguration.getExternalConfigurationFile().isPresent()) {
      muleConfig.addConfig(retrieveApikitConfiguration(api));
      api.setPath(APIKitTools.addAsteriskToPath(api.getPath()));
      addHttpListenerConfiguration(api, muleConfig);
    }
    flowScope = flowScope == null ? new FlowScope(api, isMuleEE()) : flowScope;
    muleConfig.addFlow(new Flow(flowScope.generate()));
    addConsoleFlow(api, muleConfig);
    return muleConfig.buildContent();
  }

  /**
   * Creates a document containing all common configuration files
   *
   * @param api container of the main mule application file
   * @return a document to build a common mule configuration
   */
  private MuleConfig createCommonPropertiesFile(ApikitMainFlowContainer api) {
    Document document = new Document();
    document.setRootElement(new MuleScope(false, false).generate());
    MuleConfig global = fromDoc(document);
    addHttpListenerConfiguration(api, global);
    global.addConfig(retrieveApikitConfiguration(api));
    api.setPath(APIKitTools.addAsteriskToPath(api.getPath()));
    MuleConfig globals = fromDoc(global.buildContent());
    globals.setName(customConfiguration.getExternalConfigurationFile().get());
    return globals;
  }

  /**
   * Concatenates an id with suffix to create a mule configuration id.
   *
   * @param id id of apikit main flow container
   * @return new mule configuration id
   */
  private String createMuleConfigID(String id) {
    return id + ".xml";
  }

  /**
   * Creates a new apikit configuration and adds it to the mule configuration file.
   *
   * @param api container of the main mule application file
   * @return an object containing the apikit configuration
   */
  private APIKitConfig retrieveApikitConfiguration(ApikitMainFlowContainer api) {
    if (api.getConfig() == null) {
      APIKitConfig apikitConfig = new APIKitConfig();
      apikitConfig.setApi(api.getApiFilePath());
      apikitConfig.setName(api.getId() + "-" + APIKitConfig.DEFAULT_CONFIG_NAME);
      api.setConfig(apikitConfig);
      return apikitConfig;
    }
    return api.getConfig();
  }

  /**
   * Adds http listener configuration if it doesn't already exist
   *
   * @param api        container of the main mule application file
   * @param muleConfig main mule configuration (contains http listener, apikit router, console and flows)
   */
  private void addHttpListenerConfiguration(ApikitMainFlowContainer api, MuleConfig muleConfig) {
    if (!muleConfig.getHttpListenerConfigs().contains(api.getHttpListenerConfig())) {
      muleConfig.addHttpListener(api.getHttpListenerConfig());
    }
  }

  /**
   * Adds console flow only if it is toggled on.
   *
   * @param api        container of the main mule application file
   * @param muleConfig main mule configuration (contains http listener, apikit router, console and flows)
   */
  private void addConsoleFlow(ApikitMainFlowContainer api, MuleConfig muleConfig) {
    if (showConsole) {
      muleConfig.addFlow(new Flow(new ConsoleFlowScope(api, isMuleEE()).generate()));
    }
  }

  /**
   * Determines runtime edition
   *
   * @return true for EE / false for CE
   */
  private boolean isMuleEE() {
    return scaffolderContext.getRuntimeEdition() == EE;
  }

  /**
   * Conditions for updating apikit configurations: attributes have the same name and any other attribute is null or has changed.
   *
   * @param apikitConfigFromApi        incoming new element for apikit configuration
   * @param apikitConfigFromMuleConfig existing element for apikit configuration
   * @return true for conditions explained above, false any other
   */
  private boolean shouldUpdateApikitConfig(Element apikitConfigFromApi, Element apikitConfigFromMuleConfig) {
    return apikitConfigFromApi.getAttributeValue("name").equals(apikitConfigFromMuleConfig.getAttributeValue("name")) &&
        apikitConfigFromApi.getAttributes().stream()
            .anyMatch(attribute -> lookForDifferences(apikitConfigFromMuleConfig.getAttribute(attribute.getName()), attribute));
  }

  /**
   * Conditions for attributes to be different: existing attribute not existing, or attribute has changed.
   *
   * @param existingAttribute existing attribute
   * @param attribute         new attribute potentially different
   * @return true for conditions explained above, false any other
   */
  private boolean lookForDifferences(Attribute existingAttribute, Attribute attribute) {
    return existingAttribute == null || attributeHasChanged(attribute.getValue(), existingAttribute.getValue());
  }

  /**
   * Checks if existing attribute contains incoming. If it does, then it hasn't changed, otherwise attribute has changed.
   *
   * @param currentAttribute  existing attribute
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
   *
   * @param path path to check
   * @return normalized path
   */
  private String normalizePath(String path) {
    return isNotEmpty(path) ? path.replace("\\", "/") : path;
  }

}
