/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;


import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.TestUtils.getResourceAsStream;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Assert;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleConfigBuilder;
import org.mule.tools.apikit.model.MuleDomain;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffolderContextBuilder;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;

public abstract class AbstractScaffolderTestCase extends AbstractMultiParserTestCase {

  @After
  public void after() {
    System.clearProperty(TestUtils.PARSER_V2_PROPERTY);
  }

  protected List<MuleConfig> createMuleConfigsFromLocations(List<String> ramlLocations) throws Exception {
    List<MuleConfig> muleConfigs = new ArrayList<>();
    for (String location : ramlLocations) {
      InputStream muleConfigInputStream = getResourceAsStream(location);
      muleConfigs.add(MuleConfigBuilder.fromStream(muleConfigInputStream));
    }
    return muleConfigs;
  }

  protected MuleDomain createMuleDomainFromLocation(String location) throws Exception {
    if (location == null)
      return null;

    InputStream muleDomainInputStream = getResourceAsStream(location);
    return MuleDomain.fromInputStream(muleDomainInputStream);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, String ramlLocation) throws Exception {
    return scaffoldApi(runtimeEdition, ramlLocation, Collections.emptyList(), (MuleDomain) null);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, String ramlLocation,
                                          List<String> existingMuleConfigsLocation)
      throws Exception {
    return scaffoldApi(runtimeEdition, ramlLocation, existingMuleConfigsLocation, (MuleDomain) null);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, String ramlLocation, String muleDomainLocation)
      throws Exception {
    return scaffoldApi(runtimeEdition, ramlLocation, Collections.emptyList(), muleDomainLocation);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, String ramlLocation,
                                          List<String> existingMuleConfigsLocations, String muleDomainLocation)
      throws Exception {
    MuleDomain muleDomain = createMuleDomainFromLocation(muleDomainLocation);
    return scaffoldApi(runtimeEdition, ramlLocation, existingMuleConfigsLocations, muleDomain);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, String ramlLocation,
                                          List<String> existingMuleConfigsLocations, MuleDomain muleDomain)
      throws Exception {
    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(runtimeEdition).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    List<MuleConfig> muleConfigs = createMuleConfigsFromLocations(existingMuleConfigsLocations);
    ScaffoldingConfiguration scaffoldingConfiguration = getScaffoldingConfiguration(ramlLocation, muleConfigs, muleDomain);


    ScaffoldingResult scaffoldingResult = mainAppScaffolder.run(scaffoldingConfiguration);
    assertTrue(scaffoldingResult.isSuccess());
    return scaffoldingResult;
  }

  protected ScaffoldingConfiguration getScaffoldingConfiguration(String apiPath, List<MuleConfig> muleConfigs,
                                                                 MuleDomain muleDomain) {
    ApiReference apiReference = ApiReference.create(Paths.get(apiPath).toString());
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

  private static String resource(String path) {
    URI result = null;
    path = path.startsWith("/") ? path : "/" + path;
    try {
      result = AbstractScaffolderTestCase.class.getResource(path).toURI();
    } catch (URISyntaxException e) {
      Assert.fail(e.getMessage());
    }
    return result.toString();
  }

  protected static String fileNameWhithOutExtension(final String path) {
    return FilenameUtils.removeExtension(Paths.get(path).getFileName().toString());
  }
}
