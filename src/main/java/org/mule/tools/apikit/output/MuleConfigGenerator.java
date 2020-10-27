/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;


import org.apache.commons.io.FilenameUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.APIAutodiscoveryConfig;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.ConfigurationPropertiesConfig;
import org.mule.tools.apikit.model.Flow;
import org.mule.tools.apikit.model.MainFlow;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
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
  public static final String MAIN_FLOW_SUFFIX = "-main";
  public static final String API_ID_REFERENCE = "${apiId}";

  private final List<GenerationModel> flowEntriesDiff;
  private final List<ApikitMainFlowContainer> apiContainers;
  private List<MuleConfig> muleConfigsInApp = new ArrayList<>();
  private ScaffolderContext scaffolderContext;
  private ScaffoldingConfiguration configuration;

  /**
   * @param apiContainers     Information about APIs being scaffolded
   * @param flowEntriesDiff   New flows that needs to be added
   * @param muleConfigsInApp  Pre-existing Mule configurations
   * @param scaffolderContext Scaffolder context information
   * @param configuration     Scaffolding custom configurations
   */
  public MuleConfigGenerator(List<ApikitMainFlowContainer> apiContainers, List<GenerationModel> flowEntriesDiff,
                             List<MuleConfig> muleConfigsInApp, ScaffolderContext scaffolderContext,
                             ScaffoldingConfiguration configuration) {
    this.apiContainers = apiContainers;
    this.flowEntriesDiff = flowEntriesDiff;
    this.muleConfigsInApp.addAll(muleConfigsInApp);
    this.scaffolderContext = scaffolderContext;
    this.configuration = configuration;
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
    List<MainFlow> mainFlows;
    String referenceName;
    for (ApikitMainFlowContainer api : apiContainers) {
      mainFlows = api.getMuleConfig().getMainFlows().stream().filter(flow -> flow.getApikitRouter() != null).collect(toList());
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
    MuleConfig global = createCommonPropertiesFile();
    ConfigurationPropertiesConfig configurationPropertiesConfig =
        configuration.getScaffoldingAccessories().getProperties() != null ? createConfigurationProperties() : null;
    for (ApikitMainFlowContainer api : apiContainers) {
      muleConfigs.addAll(processMuleConfig(global, configurationPropertiesConfig, api));
    }
    return muleConfigs;
  }

  private Set<MuleConfig> processMuleConfig(MuleConfig global, ConfigurationPropertiesConfig configurationPropertiesConfig,
                                            ApikitMainFlowContainer api) {
    Set<MuleConfig> muleConfigs = new HashSet<>();
    FlowScope flowScope = null;
    String muleConfigID = createMuleConfigID(api.getId());
    String mainFlowRef = FilenameUtils.removeExtension(muleConfigID).concat(MAIN_FLOW_SUFFIX);
    APIAutodiscoveryConfig apiAutodiscoveryConfig = createAPIAutodiscoveryConfig(mainFlowRef);
    MuleConfig muleConfig = createMuleConfig(api);
    if (global != null) {
      commonConfigurations(api, global);
      global = addCustomizations(configurationPropertiesConfig, apiAutodiscoveryConfig, global);
      flowScope = new FlowScope(api, isMuleEE(), global.getApikitConfigs().stream().findFirst().orElse(null).getName());
      addMuleConfig(muleConfigs, fromDoc(global.buildContent()),
                    configuration.getScaffoldingAccessories().getExternalConfigurationFile());
    } else {
      muleConfig = addCustomizations(configurationPropertiesConfig, apiAutodiscoveryConfig, muleConfig);
    }
    if (api.getMuleConfig() == null) {
      flowScope = flowScope == null ? new FlowScope(api, isMuleEE()) : flowScope;
      muleConfig.addFlow(new Flow(flowScope.generate()));
      addConsoleFlow(api, muleConfig);
      muleConfig = fromDoc(muleConfig.buildContent());
      api.setMuleConfig(muleConfig);
    } else {
      muleConfigID = api.getMuleConfig().getName();
    }
    addMuleConfig(muleConfigs, muleConfig, muleConfigID);
    return muleConfigs;
  }

  /**
   * Used for adding custom attributes that the mule config may have
   *
   * @param apiAutodiscoveryConfig        API Autodiscovery configuratoin to add.
   * @param configurationPropertiesConfig Configuration properties configuration to add.
   * @param muleConfig                    Mule configuration to customize.
   * @return muleConfig with customizations.
   */
  private MuleConfig addCustomizations(ConfigurationPropertiesConfig configurationPropertiesConfig,
                                       APIAutodiscoveryConfig apiAutodiscoveryConfig, MuleConfig muleConfig) {
    muleConfig.setConfigurationPropertiesConfig(configurationPropertiesConfig);
    muleConfig = setAPIAutodiscoveryId(muleConfig, apiAutodiscoveryConfig);
    return muleConfig;
  }

  private ConfigurationPropertiesConfig createConfigurationProperties() {
    ConfigurationPropertiesConfig configurationPropertiesConfig = new ConfigurationPropertiesConfig();
    configurationPropertiesConfig
        .setFile("${env}-configuration.".concat(configuration.getScaffoldingAccessories().getProperties().getFormat()));
    return configurationPropertiesConfig;
  }

  private MuleConfig setAPIAutodiscoveryId(MuleConfig muleConfig, APIAutodiscoveryConfig apiAutodiscoveryConfig) {
    Optional<MuleConfig> preExistingMuleConfigOptional =
        muleConfigsInApp.stream().filter(config -> config.getApiAutodiscoveryConfig() != null
            && config.getName() != null && !config.getName().equalsIgnoreCase(muleConfig.getName())).findAny();
    boolean hasAPIAutodiscoveryId = hasAPIAutodiscoveryId();
    if (hasAPIAutodiscoveryId
        && (!preExistingMuleConfigOptional.isPresent() || apiAutodiscoveryConfig != null)) {
      muleConfig.setApiAutodiscoveryConfig(apiAutodiscoveryConfig);
      //update originalContent
      return fromDoc(muleConfig.buildContent(), false);
    }
    return muleConfig;
  }

  private boolean hasAPIAutodiscoveryId() {
    if (configuration.getScaffoldingAccessories().getProperties() != null) {
      Map<String, Map<String, Object>> properties = configuration.getScaffoldingAccessories().getProperties().getFiles();
      for (Entry<String, Map<String, Object>> propertyList : properties.entrySet()) {
        for (Entry<String, Object> property : propertyList.getValue().entrySet()) {
          if (property.getKey().equalsIgnoreCase("apiId")) {
            return true;
          }
        }
      }
    }
    return configuration.getScaffoldingAccessories().getApiId() != null;
  }

  private Optional<MuleConfig> searchExistingMuleConfigByName() {
    return muleConfigsInApp.stream().filter(config -> {
      String externalConfigurationFile = configuration.getScaffoldingAccessories().getExternalConfigurationFile();
      return isNotEmpty(externalConfigurationFile) && externalConfigurationFile.equalsIgnoreCase(config.getName());
    }).findAny();
  }

  private MuleConfig createMuleConfig(ApikitMainFlowContainer api) {
    if (api.getMuleConfig() == null) {
      Document document = new Document();
      document.setRootElement(new MuleScope(false, false, hasAPIAutodiscoveryId()).generate());
      MuleConfig muleConfig = fromDoc(document);
      if (configuration.getScaffoldingAccessories().getExternalConfigurationFile() == null) {
        commonConfigurations(api, muleConfig);
      }
      return muleConfig;
    }
    return api.getMuleConfig();
  }

  private void commonConfigurations(ApikitMainFlowContainer api, MuleConfig muleConfig) {
    addApikitConfiguration(api, muleConfig);
    api.setPath(APIKitTools.addAsteriskToPath(api.getPath()));
    addHttpListenerConfiguration(api, muleConfig);
  }

  /**
   * If needed it will add the muleConfig file to the mule configurations, previously setting its name.
   */

  public void addMuleConfig(Set<MuleConfig> muleConfigs, MuleConfig muleConfig, String name) {
    muleConfig.setName(name);
    muleConfigs.add(muleConfig);
  }

  private APIAutodiscoveryConfig createAPIAutodiscoveryConfig(String mainFlowRef) {
    if (hasAPIAutodiscoveryId()) {
      String apiId = configuration.getScaffoldingAccessories().getProperties() != null ? API_ID_REFERENCE
          : configuration.getScaffoldingAccessories().getApiId();
      Boolean ignoreBasePath = Boolean.valueOf(APIAutodiscoveryConfig.IGNORE_BASE_PATH_DEFAULT);
      return new APIAutodiscoveryConfig(apiId, ignoreBasePath, mainFlowRef);
    }
    return null;
  }


  /**
   * Generates new flows based on the generation models previously created. Also updates apikit configuration in case
   * needed.
   *
   * @param flowEntry new flow to be generated
   */
  private void generateFlowEntryInMuleConfig(GenerationModel flowEntry) {
    Element apikitFlowScope = new APIKitFlowScope(flowEntry, isMuleEE()).generate();
    MuleConfig mainMuleConfig = flowEntry.getApi().getMuleConfig();
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
   * Creates a document containing all common configuration files
   *
   * @return a document to build a common mule configuration
   */
  private MuleConfig createCommonPropertiesFile() {
    Optional<MuleConfig> muleConfig = searchExistingMuleConfigByName();
    if (muleConfig.isPresent()) {
      return muleConfig.get();
    }
    if (configuration.getScaffoldingAccessories().getExternalConfigurationFile() != null) {
      Document document = new Document();
      document.setRootElement(new MuleScope(false, false, hasAPIAutodiscoveryId()).generate());
      MuleConfig global = fromDoc(document);
      return global;
    }
    return null;
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
   * @param api        container of the main mule application file
   * @param muleConfig main mule configuration (contains http listener, apikit router, console and flows)
   */
  private void addApikitConfiguration(ApikitMainFlowContainer api, MuleConfig muleConfig) {
    if (api.getConfig() == null) {
      APIKitConfig apikitConfig = new APIKitConfig();
      String apiFilePathSource = api.getApiFilePath();
      String apiFilePath = configuration.getApiSyncResource() == null ? apiFilePathSource
          : configuration.getApiSyncResource();
      apikitConfig.setApi(apiFilePath);
      apikitConfig.setName(api.getId() + "-" + APIKitConfig.DEFAULT_CONFIG_NAME);
      api.setConfig(apikitConfig);
    }
    if (!muleConfig.getApikitConfigs().stream().filter(config -> config.equals(api.getConfig())).findAny().isPresent()) {
      muleConfig.addConfig(api.getConfig());
    }
  }


  /**
   * Adds http listener configuration if it doesn't already exist
   *
   * @param api        container of the main mule application file
   * @param muleConfig main mule configuration (contains http listener, apikit router, console and flows)
   */
  private void addHttpListenerConfiguration(ApikitMainFlowContainer api, MuleConfig muleConfig) {
    if (isEmpty(muleConfig.getHttpListenerConfigs())
        || !muleConfig.getHttpListenerConfigs().contains(api.getHttpListenerConfig())) {
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
    if (configuration.getScaffoldingAccessories().isShowConsole()) {
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
