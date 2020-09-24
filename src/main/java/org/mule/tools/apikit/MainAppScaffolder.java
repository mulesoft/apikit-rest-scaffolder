/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.commons.io.IOUtils;
import org.mule.apikit.model.Resource;
import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.RAMLFilesParser;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.Configuration;
import org.mule.tools.apikit.model.ConfigurationGroup;
import org.mule.tools.apikit.model.CustomConfiguration;
import org.mule.tools.apikit.model.HttpListenerConfig;
import org.mule.tools.apikit.model.HttpListenerConnection;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.Scaffolder;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffolderResource;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingError;
import org.mule.tools.apikit.model.ScaffoldingResult;
import org.mule.tools.apikit.model.ScaffolderResult;
import org.mule.tools.apikit.output.GenerationModel;
import org.mule.tools.apikit.output.GenerationStrategy;
import org.mule.tools.apikit.output.MuleConfigGenerator;
import org.mule.tools.apikit.output.ResourcesGenerator;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.mule.tools.apikit.model.ApikitMainFlowContainer.DEFAULT_HOST;
import static org.mule.tools.apikit.model.ApikitMainFlowContainer.DEFAULT_PORT;
import static org.mule.tools.apikit.model.ApikitMainFlowContainer.DEFAULT_PROTOCOL;

public final class MainAppScaffolder implements Scaffolder {

  private static final GenerationStrategy GENERATOR = new GenerationStrategy();
  private final ScaffolderContext scaffolderContext;

  public MainAppScaffolder(ScaffolderContext scaffolderContext) {
    this.scaffolderContext = scaffolderContext;
  }

  @Override
  public ScaffoldingResult run(ScaffoldingConfiguration config) {
    ScaffolderResult.Builder scaffolderResultBuilder = ScaffolderResult.builder();
    try {
      APIFactory apiFactory = new APIFactory(config.getDomain().getHttpListenerConfigs());
      List<MuleConfig> muleConfigs = config.getMuleConfigurations();

      MuleConfigParser muleConfigParser = new MuleConfigParser(apiFactory, config.getApi().getLocation(), muleConfigs);
      RAMLFilesParser ramlFilesParser = new RAMLFilesParser(apiFactory, config.getApi());

      List<ApikitMainFlowContainer> includedApis = ramlFilesParser.getApisAsList();
      ResourcesGenerator.replaceReferencesToProperties(config.getCustomConfiguration(), includedApis);
      List<GenerationModel> generationModels = GENERATOR.generate(ramlFilesParser.getEntries(),
                                                                  muleConfigParser.getIncludedApis(),
                                                                  muleConfigParser.getEntries());

      MuleConfigGenerator muleConfigGenerator = new MuleConfigGenerator(includedApis,
                                                                        generationModels,
                                                                        muleConfigs,
                                                                        scaffolderContext, config.isShowConsole(),
                                                                        config.getCustomConfiguration());

      List<MuleConfig> generatedConfigs = muleConfigGenerator.generate();
      scaffolderResultBuilder.withGeneratedConfigs(generatedConfigs);

      scaffolderResultBuilder.withGeneratedResources(ResourcesGenerator.generate(config.getCustomConfiguration()));

    } catch (Exception e) {
      List<ScaffoldingError> errors = Arrays.asList(new ScaffoldingError(e.getMessage()));
      scaffolderResultBuilder.withErrors(errors);
    } finally {
      return scaffolderResultBuilder.build();
    }
  }
}
