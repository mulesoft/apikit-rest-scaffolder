/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.mule.tools.apikit.input.RAMLFilesParser;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.Flow;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MunitScaffolderContext;
import org.mule.tools.apikit.model.Scaffolder;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingError;
import org.mule.tools.apikit.model.ScaffoldingResult;
import org.mule.tools.apikit.model.ScaffolderResult;
import org.mule.tools.apikit.output.GenerationModel;
import org.mule.tools.apikit.output.MunitTestSuiteGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

public final class MunitScaffolder implements Scaffolder {

  private static final String MULTIPLE_APIKIT_CONFIGS_ERROR_TEMPLATE = "There are multiple apikit configs for %s";
  private static final String NO_APIKIT_CONFIGS_ERROR_TEMPLATE = "No apikit configs for %s were found";
  private static final String MULTIPLE_MAIN_FLOWS_ERROR_TEMPLATE = "There are multiple main flows for the config %s";
  private static final String NO_MAIN_FLOWS_ERROR_TEMPLATE = "No main flow for %s were found";
  private static final String NO_MAIN_MULE_CONFIGS_ERROR = "No mule configs found";
  private static final String EMPTY_APIKIT_CONFIG_ERROR = "Apikit config is empty";

  private MunitScaffolderContext scaffolderContext;

  public MunitScaffolder(MunitScaffolderContext scaffolderContext) {
    this.scaffolderContext = scaffolderContext;
  }

  @Override
  public ScaffoldingResult run(ScaffoldingConfiguration config) {
    ScaffolderResult.Builder scaffolderResultBuilder = ScaffolderResult.builder();

    try {
      validateNonEmptyCollection(config.getMuleConfigurations(), NO_MAIN_MULE_CONFIGS_ERROR);

      RAMLFilesParser ramlFilesParser = new RAMLFilesParser(new APIFactory(Collections.emptyList()), config.getApi());
      List<GenerationModel> generationModels = new ArrayList<>(ramlFilesParser.getEntries().values());

      String mainFlowName = getMainFlowName(config.getMuleConfigurations(), config.getApi().getLocation());
      scaffolderContext.setApikitMainFlowName(mainFlowName);
      MunitTestSuiteGenerator munitTestSuiteGenerator = new MunitTestSuiteGenerator(generationModels, scaffolderContext);

      List<MuleConfig> generatedConfigs = munitTestSuiteGenerator.generate();
      scaffolderResultBuilder.withGeneratedConfigs(generatedConfigs);
      scaffolderResultBuilder.withGeneratedResources(munitTestSuiteGenerator.getGeneratedResources());
    } catch (Exception e) {
      List<ScaffoldingError> errors = Arrays.asList(new ScaffoldingError(e.getMessage()));
      scaffolderResultBuilder.withErrors(errors);
    } finally {
      return scaffolderResultBuilder.build();
    }
  }

  /*
   * To get the main flow name for the current ApiSpecification we need:
   * 1) Iterate over all the mule configs to get the <apikit:config> that references the ApiSpec
   * 2) Validate that there's only one <apikit:config> referencing such ApiSpec
   * 3) Iterate again all the mule configs to get the main flow that has a router referencing the config found in step 1
   * 4) Validate that there is only one main flow referencing the <apikit:config>
   */
  private String getMainFlowName(List<MuleConfig> muleConfigs, String apiLocation) {
    List<APIKitConfig> apikitConfigs = getApikitConfigsForApiSpecification(muleConfigs, apiLocation);
    validateNonEmptyCollection(apikitConfigs, format(NO_APIKIT_CONFIGS_ERROR_TEMPLATE, apiLocation));
    validateNoMoreThanOneElementInCollection(apikitConfigs, format(MULTIPLE_APIKIT_CONFIGS_ERROR_TEMPLATE, apiLocation));

    APIKitConfig apikitConfig = apikitConfigs.get(0);
    List<Element> mainFlows = getMainFlowsReferencigApikitConfig(muleConfigs, apikitConfig);
    validateNonEmptyCollection(mainFlows, format(NO_MAIN_FLOWS_ERROR_TEMPLATE, apikitConfig.getName()));
    validateNoMoreThanOneElementInCollection(mainFlows, format(MULTIPLE_MAIN_FLOWS_ERROR_TEMPLATE, apikitConfig.getName()));

    String mainFlowName = mainFlows.get(0).getAttribute("name").getValue();
    validateNonEmptyString(mainFlowName, EMPTY_APIKIT_CONFIG_ERROR);

    return mainFlowName;
  }

  private List<APIKitConfig> getApikitConfigsForApiSpecification(List<MuleConfig> muleConfigs, String apiLocation) {
    List<APIKitConfig> apikitConfigs = new ArrayList<>();
    for (MuleConfig muleConfig : muleConfigs) {
      for (APIKitConfig config : muleConfig.getApikitConfigs()) {
        String currentConfigApiLocation = config.getApiSpecificationLocation();
        if (currentConfigApiLocation.endsWith(apiLocation)) {
          apikitConfigs.add(config);
        }
      }
    }
    return apikitConfigs;
  }

  private List<Element> getMainFlowsReferencigApikitConfig(List<MuleConfig> muleConfigs, APIKitConfig apiKitConfig) {
    List<Element> mainFlows = new ArrayList<>();

    for (MuleConfig muleConfig : muleConfigs) {
      for (Flow flow : muleConfig.getFlows()) {
        Element flowElement = flow.generate();
        for (Content content : flowElement.getContent()) {
          if (content instanceof Element) {
            Element contentElement = (Element) content;
            if (contentElement.getNamespace().getPrefix().equals("apikit") && contentElement.getName().equals("router")) {
              Attribute configRefAttribute = contentElement.getAttribute("config-ref");
              if (configRefAttribute.getValue().equals(apiKitConfig.getName())) {
                mainFlows.add(flowElement);
              }
            }
          }
        }
      }
    }

    return mainFlows;
  }

  private static void validateNoMoreThanOneElementInCollection(Collection<?> collection, String errorMessage) {
    if (collection.size() > 1) {
      throw new RuntimeException(errorMessage);
    }
  }

  private static void validateNonEmptyCollection(Collection<?> collection, String errorMessage) {
    if (collection.isEmpty()) {
      throw new RuntimeException(errorMessage);
    }
  }

  private static void validateNonEmptyString(String name, String errorMessage) {
    if (name.isEmpty()) {
      throw new RuntimeException(errorMessage);
    }
  }
}
