/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.RAMLFilesParser;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleDomain;
import org.mule.tools.apikit.model.Scaffolder;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffolderResult;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;
import org.mule.tools.apikit.output.GenerationModel;
import org.mule.tools.apikit.output.GenerationStrategy;
import org.mule.tools.apikit.output.MuleConfigGenerator;

import java.util.List;

public class MuleScaffolder implements Scaffolder {

  private ScaffolderContext scaffolderContext;
  private APIFactory apiFactory;
  private MuleConfigParser muleConfigParser;

  public MuleScaffolder(ScaffolderContext scaffolderContext) {
    this.scaffolderContext = scaffolderContext;
  }

  @Override
  public ScaffoldingResult run(ScaffoldingConfiguration config) {
    apiFactory = resolveAPIFactory(config.getDomain());
    List<MuleConfig> muleConfigs = config.getMuleConfigurations();

    muleConfigParser = resolveMuleConfigParser();
    muleConfigParser.parse(config.getApi().getLocation(), muleConfigs);
    RAMLFilesParser ramlFilesParser = new RAMLFilesParser(apiFactory, config.getApi());

    List<ApikitMainFlowContainer> includedApis = ramlFilesParser.getApisAsList();
    List<GenerationModel> generationModels = new GenerationStrategy()
        .generate(ramlFilesParser.getEntries(), muleConfigParser.getIncludedApis(), muleConfigParser.getEntries());

    MuleConfigGenerator muleConfigGenerator =
        new MuleConfigGenerator(includedApis, generationModels, muleConfigs, scaffolderContext.getRuntimeEdition());
    List<MuleConfig> generatedConfigs = muleConfigGenerator.generate();

    return new ScaffolderResult.Builder()
        .withGeneratedConfigs(generatedConfigs)
        .withErrors(muleConfigGenerator.getScaffoldingErrors())
        .withGeneratedResources(muleConfigGenerator.getGeneratedResources())
        .build();
  }

  private APIFactory resolveAPIFactory(MuleDomain domain) {
    if (apiFactory != null)
      return apiFactory;
    if (domain == null || domain.getHttpListenerConfigs() == null || domain.getHttpListenerConfigs().size() == 0)
      return new APIFactory();

    return new APIFactory(domain.getHttpListenerConfigs());
  }

  private MuleConfigParser resolveMuleConfigParser() {
    if(muleConfigParser != null)
      return muleConfigParser;

    return new MuleConfigParser(apiFactory);
  }
}
