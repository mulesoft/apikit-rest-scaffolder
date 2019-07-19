/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.munit;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserMode;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.MunitScaffolder;
import org.mule.tools.apikit.TestUtils;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleConfigBuilder;
import org.mule.tools.apikit.model.MunitScaffolderContext;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderContextBuilder;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;

import java.io.File;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public abstract class AbstractMunitScaffolderTest {

  private static final String API_SPEC_LOCATION_PLACEHOLDER = "${apiLocation}";
  private static final String APIKIT_MAIN_FLOW_NAME_PLACEHOLDER = "${apikitMainFlowName}";

  protected ScaffoldingResult simpleGeneration(String name, String resourcesFolder, boolean shouldCreateResources)
      throws Exception {
    String suiteFileName = String.format("%s.xml", name);
    String mainFlowName = name + "-main";

    ApiReference apiReference = ApiReference.create(resourcesFolder + File.separator + String.format("%s.yaml", name));
    ParseResult parseResult = new ParserService().parse(apiReference, ParserMode.RAML);

    assertTrue(parseResult.success());

    String defaultMuleConfig = TestUtils.getResourceAsString("scaffolder/default-mule-config.xml");
    String fileContent = defaultMuleConfig.replace(APIKIT_MAIN_FLOW_NAME_PLACEHOLDER, mainFlowName)
        .replace(API_SPEC_LOCATION_PLACEHOLDER, parseResult.get().getLocation());

    MuleConfig muleConfig = MuleConfigBuilder.fromStream(IOUtils.toInputStream(fileContent));

    ScaffolderContextBuilder scaffolderContextBuilder = ScaffolderContextBuilder.builder();
    scaffolderContextBuilder.withRuntimeEdition(RuntimeEdition.EE);
    scaffolderContextBuilder.shouldCreateMunitResources(shouldCreateResources);
    scaffolderContextBuilder.withMunitSuiteName(suiteFileName);
    MunitScaffolderContext munitScaffolderContext = (MunitScaffolderContext) scaffolderContextBuilder.build();

    ScaffoldingConfiguration.Builder scaffoldingConfigurationBuilder = ScaffoldingConfiguration.builder();
    scaffoldingConfigurationBuilder.withApi(parseResult.get());
    scaffoldingConfigurationBuilder.withMuleConfigurations(Lists.newArrayList(muleConfig));
    MunitScaffolder scaffolder = new MunitScaffolder(munitScaffolderContext);
    ScaffoldingResult scaffoldingResult = scaffolder.run(scaffoldingConfigurationBuilder.build());
    assertTrue(scaffoldingResult.isSuccess());
    assertEquals(1, scaffoldingResult.getGeneratedConfigs().size());

    return scaffoldingResult;
  }
}
