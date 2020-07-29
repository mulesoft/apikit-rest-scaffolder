/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.TestUtils.getResourceAsStream;
import static org.mule.tools.apikit.model.MuleConfigBuilder.fromStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleConfigBuilder;
import org.mule.tools.apikit.model.MuleDomain;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffolderContextBuilder;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;
import org.xml.sax.SAXException;

public abstract class AbstractScaffolderTestCase extends AbstractMultiParserTestCase {

  @After
  public void after() {
    System.clearProperty(TestUtils.PARSER_V2_PROPERTY);
  }

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
    InputStream api = result.getGeneratedConfigs().stream().filter(config -> config.getName().contains(fileName))
        .findFirst().orElseThrow(() -> new RuntimeException(("unable to find generated file"))).getContent();
    return APIKitTools.readContents(api);
  }

  protected static MuleConfig createConfig(String path) throws Exception {
    InputStream resourceAsStream = getResourceAsStream(path);
    MuleConfig muleConfig = fromStream(resourceAsStream);;
    muleConfig.setName(extractName(path));
    return muleConfig;
  }

  protected static String extractName(String path) {
    String[] pathParts = path.split("/");
    return pathParts[pathParts.length - 1];
  }

  // Check if this method could be replaced with already existing scaffoldAPI().
  //If the scaffoldApi is refactored a bit, then it can build and pass the path from here
  // ScaffoldingResult result = scaffoldApi(RuntimeEdition.CE, ROOT_RAML_RESOURCE_URL + ramlFolderV1 + raml);
  // in MainAppScaffolderWithExistingConfigApiSyncTest
  protected static ScaffoldingResult scaffoldApiSync(String raml, String ramlFolder, String rootRamlResourceUrl,
                                                     List<MuleConfig> muleConfigs) {
    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.CE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    ResourceLoader testScaffolderResourceLoader = new TestScaffolderResourceLoader(ramlFolder);
    ParseResult parseResult =
        new ParserService().parse(ApiReference.create(rootRamlResourceUrl + raml, testScaffolderResourceLoader));

    ScaffoldingConfiguration.Builder configurationBuilder =
        new ScaffoldingConfiguration.Builder().withApi(parseResult.get());
    if (muleConfigs != null) {
      configurationBuilder.withMuleConfigurations(muleConfigs);
    }

    ScaffoldingResult result = mainAppScaffolder.run(configurationBuilder.build());
    return result;
  }

  protected List<MuleConfig> createMuleConfigsFromLocations(List<String> ramlLocations) throws Exception {
    List<MuleConfig> muleConfigs = new ArrayList<>();
    for (String location : ramlLocations) {
      InputStream muleConfigInputStream = getResourceAsStream(location);
      muleConfigs.add(fromStream(muleConfigInputStream));
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

  protected ScaffoldingResult scaffoldApiHiddenConsole(RuntimeEdition runtimeEdition, String ramlLocation) throws Exception {
    return scaffoldApi(runtimeEdition, ramlLocation, Collections.emptyList(), (MuleDomain) null, false);
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
    return scaffoldApi(runtimeEdition, ramlLocation, existingMuleConfigsLocations, muleDomain, true);
  }

  protected ScaffoldingResult scaffoldApi(RuntimeEdition runtimeEdition, String ramlLocation,
                                          List<String> existingMuleConfigsLocations, MuleDomain muleDomain, boolean showConsole)
      throws Exception {
    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(runtimeEdition).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    List<MuleConfig> muleConfigs = createMuleConfigsFromLocations(existingMuleConfigsLocations);
    ScaffoldingConfiguration scaffoldingConfiguration =
        getScaffoldingConfiguration(ramlLocation, muleConfigs, muleDomain, showConsole);


    ScaffoldingResult scaffoldingResult = mainAppScaffolder.run(scaffoldingConfiguration);
    assertTrue(scaffoldingResult.isSuccess());
    return scaffoldingResult;
  }

  protected ScaffoldingConfiguration getScaffoldingConfiguration(String apiPath, List<MuleConfig> muleConfigs,
                                                                 MuleDomain muleDomain) {
    return getScaffoldingConfiguration(apiPath, muleConfigs, muleDomain, true);
  }

  protected ScaffoldingConfiguration getScaffoldingConfiguration(String apiPath, List<MuleConfig> muleConfigs,
                                                                 MuleDomain muleDomain, boolean showConsole) {
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
    configuration.withShowConsole(showConsole);
    return configuration.build();
  }

  protected static String fileNameWhithOutExtension(final String path) {
    return FilenameUtils.removeExtension(Paths.get(path).getFileName().toString());
  }
}
