/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;


import org.apache.commons.io.FilenameUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleDomain;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffolderContextBuilder;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.TestUtils.getResourceAsStream;
import static org.mule.tools.apikit.model.MuleConfigBuilder.fromStream;

public abstract class AbstractScaffolderTestCase extends AbstractMultiParserTestCase {

  protected static void verifySuccessfulScaffolding(ScaffoldingResult result, String... expectedArrayPath)
      throws IOException, SAXException {
    List<String> expectedPaths = Arrays.asList(expectedArrayPath);
    assertTrue(result.isSuccess());
    assertEquals(expectedPaths.size(), result.getGeneratedConfigs().size());
    for (String expectedPath : expectedArrayPath) {
      String expected = APIKitTools.readContents(getResourceAsStream(expectedPath));
      String configFileName = extractName(expectedPath);
      String generated = retrieveGeneratedFile(result, configFileName);
      XMLUnit.setIgnoreWhitespace(true);
      Diff diff = XMLUnit.compareXML(expected, generated);
      assertTrue(diff.identical());
    }
  }

  protected static String retrieveGeneratedFile(ScaffoldingResult result, String fileName)
      throws IOException {
    InputStream api =
        result.getGeneratedConfigs().stream().filter(config -> {
          String name = config.getName() != null ? extractName(config.getName()) : "";
          return name.contains(fileName) || fileName.contains(name);
        })
            .findFirst().orElseThrow(() -> new RuntimeException(("unable to find generated file"))).getContent();
    return APIKitTools.readContents(api);
  }

  protected static String extractName(String path) {
    String[] pathParts = path.split("/");
    return pathParts[pathParts.length - 1].split(".xml")[0];
  }

  public static List<MuleConfig> createMuleConfigsFromLocations(List<String> ramlLocations) throws Exception {
    if (isEmpty(ramlLocations)) {
      return emptyList();
    }
    List<MuleConfig> muleConfigs = new ArrayList<>();
    for (String location : ramlLocations) {
      InputStream muleConfigInputStream = getResourceAsStream(location);
      MuleConfig muleConfig = fromStream(muleConfigInputStream, false);
      muleConfig.setName(extractName(location).concat(".xml"));
      muleConfigs.add(muleConfig);
    }
    return muleConfigs;
  }

  public static MuleDomain createMuleDomainFromLocation(String location) throws Exception {
    if (isBlank(location)) {
      return null;
    }

    InputStream muleDomainInputStream = getResourceAsStream(location);
    return MuleDomain.fromInputStream(muleDomainInputStream);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, String ramlLocation) throws Exception {
    return scaffoldApi(runtimeEdition, ramlLocation, emptyList(), (MuleDomain) null);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, ApiReference apiReference) {
    return scaffoldApi(runtimeEdition, apiReference, null, emptyList(), true, null, null);
  }


  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, String ramlLocation,
                                          List<String> existingMuleConfigsLocations, MuleDomain muleDomain)
      throws Exception {
    List<MuleConfig> existingMuleConfigs = createMuleConfigsFromLocations(existingMuleConfigsLocations);
    return scaffoldApi(runtimeEdition, ramlLocation, muleDomain, existingMuleConfigs, true);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, ApiReference apiReference,
                                          List<MuleConfig> existingMuleConfigs) {
    return scaffoldApi(runtimeEdition, apiReference, null, existingMuleConfigs, true, null, null);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, ApiReference apiReference, MuleDomain muleDomain,
                                          List<String> existingMuleConfigsLocations)
      throws Exception {
    List<MuleConfig> existingMuleConfigs = createMuleConfigsFromLocations(existingMuleConfigsLocations);
    return scaffoldApi(runtimeEdition, apiReference, muleDomain, existingMuleConfigs, true, null, null);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, String ramlLocation,
                                          MuleDomain muleDomain, List<MuleConfig> existingMuleConfigs, boolean showConsole) {
    ApiReference apiReference = ApiReference.create(Paths.get(ramlLocation).toString());
    return scaffoldApi(runtimeEdition, apiReference, muleDomain, existingMuleConfigs, showConsole, null, null);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, ApiReference apiReference,
                                          MuleDomain muleDomain, List<MuleConfig> existingMuleConfigs, boolean showConsole,
                                          String externalizedCommonFile, String apiAutodiscoveryId) {
    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(runtimeEdition).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    ScaffoldingConfiguration scaffoldingConfiguration =
        getScaffoldingConfiguration(apiReference, existingMuleConfigs, muleDomain, showConsole, externalizedCommonFile,
                                    apiAutodiscoveryId);

    ScaffoldingResult scaffoldingResult = mainAppScaffolder.run(scaffoldingConfiguration);
    assertTrue(scaffoldingResult.isSuccess());
    return scaffoldingResult;
  }


  protected ScaffoldingConfiguration getScaffoldingConfiguration(ApiReference apiReference, List<MuleConfig> muleConfigs,
                                                                 MuleDomain muleDomain, boolean showConsole,
                                                                 String externalizedCommonFile, String apiAutodiscoveryId) {
    ParseResult parseResult = new ParserService().parse(apiReference);
    ScaffoldingConfiguration.Builder configuration = new ScaffoldingConfiguration.Builder();
    configuration.withApi(parseResult.get());
    if (isNotEmpty(muleConfigs)) {
      configuration.withMuleConfigurations(muleConfigs);
    }
    if (muleDomain != null) {
      configuration.withDomain(muleDomain);
    }
    if (externalizedCommonFile != null) {
      configuration.withExternalConfigurationFile(externalizedCommonFile);
    }
    if (apiAutodiscoveryId != null) {
      configuration.withApiAutodiscoveryId(apiAutodiscoveryId);
    }
    configuration.withShowConsole(showConsole);
    return configuration.build();
  }

  protected static String fileNameWhithOutExtension(final String path) {
    return FilenameUtils.removeExtension(Paths.get(path).getFileName().toString());
  }
}
