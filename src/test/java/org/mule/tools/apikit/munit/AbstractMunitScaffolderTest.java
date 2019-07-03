/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.munit;

import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserMode;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.MunitScaffolder;
import org.mule.tools.apikit.model.*;

import java.io.File;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public abstract class AbstractMunitScaffolderTest {

  protected ScaffoldingResult simpleGeneration(String name, String resourcesFolder, boolean shouldCreateResources) {
    String suiteFileName = String.format("%s.xml", name);

    ScaffolderContext.Builder scaffolderContextBuilder = ScaffolderContext.builder();
    scaffolderContextBuilder.withRuntimeEdition(RuntimeEdition.EE);
    scaffolderContextBuilder.withApikitMainFlowName("apikitFlow");
    scaffolderContextBuilder.shouldCreateMunitResources(shouldCreateResources);
    scaffolderContextBuilder.withMunitSuiteName(suiteFileName);
    ScaffolderContext scaffolderContext = scaffolderContextBuilder.build();

    ApiReference apiReference = ApiReference.create(resourcesFolder + File.separator + String.format("%s.yaml", name));
    ParseResult parseResult = new ParserService().parse(apiReference, ParserMode.RAML);

    assertTrue(parseResult.success());

    ScaffoldingConfiguration scaffoldingConfiguration = ScaffoldingConfiguration.builder().withApi(parseResult.get()).build();
    MunitScaffolder scaffolder = new MunitScaffolder(scaffolderContext);
    ScaffoldingResult scaffoldingResult = scaffolder.run(scaffoldingConfiguration);
    assertTrue(scaffoldingResult.isSuccess());
    assertEquals(1, scaffoldingResult.getGeneratedConfigs().size());

    return scaffoldingResult;
  }
}
