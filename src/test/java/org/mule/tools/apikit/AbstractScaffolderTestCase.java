/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.mule.apikit.implv2.ParserV2Utils;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleConfigBuilder;
import org.mule.tools.apikit.model.MuleDomain;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;
import org.mule.tools.apikit.model.RuntimeEdition;

import static org.junit.Assert.assertTrue;

public abstract class AbstractScaffolderTestCase extends AbstractMultiParserTestCase {

  @After
  public void after() {
    System.clearProperty(ParserV2Utils.PARSER_V2_PROPERTY);
  }

  protected List<MuleConfig> createMuleConfigsFromLocations(List<String> ramlLocations) throws Exception {
    List<MuleConfig> muleConfigs = new ArrayList<>();
    for(String location : ramlLocations) {
      InputStream muleConfigInputStream = AbstractScaffolderTestCase.class.getClassLoader().getResourceAsStream(location);
      muleConfigs.add(MuleConfigBuilder.fromStream(muleConfigInputStream));
    }
    return muleConfigs;
  }

  protected MuleDomain createMuleDomainFromLocation(String location) throws Exception {
    if(location == null)
      return null;

    String muleDomainFilePath = ScaffolderMule4Test.class.getClassLoader().getResource(location).getFile();
    InputStream muleDomainInputStream = new FileInputStream(muleDomainFilePath);
    return MuleDomain.fromInputStream(muleDomainInputStream);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, String ramlLocation) throws Exception {
    return scaffoldApi(runtimeEdition, ramlLocation, Collections.emptyList(), null);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, String ramlLocation, List<String> existingMuleConfigsLocation) throws Exception {
    return scaffoldApi(runtimeEdition, ramlLocation, existingMuleConfigsLocation, null);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, String ramlLocation, String muleDomainLocation) throws Exception {
    return scaffoldApi(runtimeEdition, ramlLocation, Collections.emptyList(), muleDomainLocation);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, String ramlLocation,
                                        List<String> existingMuleConfigsLocations, String muleDomainLocation) throws Exception {
    ScaffolderContext context = new ScaffolderContext.Builder().withRuntimeEdition(runtimeEdition).build();
    MuleScaffolder muleScaffolder = new MuleScaffolder(context);

    List<MuleConfig> muleConfigs = createMuleConfigsFromLocations(existingMuleConfigsLocations);
    MuleDomain muleDomain = createMuleDomainFromLocation(muleDomainLocation);
    ScaffoldingConfiguration scaffoldingConfiguration = getScaffoldingConfiguration(ramlLocation, muleConfigs, muleDomain);


    ScaffoldingResult scaffoldingResult = muleScaffolder.run(scaffoldingConfiguration);
    assertTrue(scaffoldingResult.isSuccess());
    return scaffoldingResult;
  }

  protected ScaffoldingConfiguration getScaffoldingConfiguration(String apiPath, List<MuleConfig> muleConfigs,
                                                                         MuleDomain muleDomain) {
    ApiReference apiReference = ApiReference.create(apiPath);
    ParseResult parseResult = new ParserService().parse(apiReference);
    ScaffoldingConfiguration.Builder configuration = new ScaffoldingConfiguration.Builder();
    configuration.withApi(parseResult.get());
    if (muleConfigs != null) {
      configuration.withMuleConfigurations(muleConfigs);
    }

    if (muleDomain != null) {
      configuration.withDomain(muleDomain);
    }
    return configuration.build();
  }

  protected static String fileNameWhithOutExtension(final String path) {
    return FilenameUtils.removeExtension(Paths.get(path).getFileName().toString());
  }
}
