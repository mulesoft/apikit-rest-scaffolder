/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.mule.tools.apikit.input.RAMLFilesParser;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.Scaffolder;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingError;
import org.mule.tools.apikit.model.ScaffoldingResult;
import org.mule.tools.apikit.model.ScaffolderResult;
import org.mule.tools.apikit.output.GenerationModel;
import org.mule.tools.apikit.output.MunitTestSuiteGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MunitScaffolder implements Scaffolder {

  private ScaffolderContext scaffolderContext;

  public MunitScaffolder(ScaffolderContext scaffolderContext) {
    this.scaffolderContext = scaffolderContext;
  }

  @Override
  public ScaffoldingResult run(ScaffoldingConfiguration config) {
    ScaffolderResult.Builder scaffolderResultBuilder = ScaffolderResult.builder();

    try {
      RAMLFilesParser ramlFilesParser = new RAMLFilesParser(new APIFactory(Collections.emptyList()), config.getApi());
      List<GenerationModel> generationModels = new ArrayList<>(ramlFilesParser.getEntries().values());
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
}
