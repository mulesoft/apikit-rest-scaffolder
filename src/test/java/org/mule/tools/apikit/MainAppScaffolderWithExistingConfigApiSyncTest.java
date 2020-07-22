/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleConfigBuilder;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffolderContextBuilder;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.TestUtils.getResourceAsStream;

public class MainAppScaffolderWithExistingConfigApiSyncTest extends AbstractScaffolderTestCase {

  private final static String ROOT_RAML_RESOURCE_URL = "resource::com.mycompany:raml-api:1.0.0:raml:zip:";
  private final static String ROOT_RAML_RESOURCE_URL_V2 = "resource::com.mycompany:raml-api:2.0.0:raml:zip:";

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Before
  public void setUp() throws IOException {
    folder.newFolder("rescaffolding-apisync-version");
    folder.newFolder("rescaffolding-apisync-version", "v1");
    folder.newFolder("rescaffolding-apisync-version", "v2");
  }

  /**
   *
   * First it scaffolds an API normally with configuration of APIKit inside the main xml file with version of raml 0.0.1.
   * Finally it re-scaffolds with same version
   * Expected behaviour is that the new scaffolded API has version 0.0.1 in the main xml.
   *
   */
  @Test
  public void reScaffold() throws Exception {
    String raml = "api.raml";
    String ramlFolder = "src/test/resources/rescaffolding-apisync-version/v1";

    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.CE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    ResourceLoader testScaffolderResourceLoader = new TestScaffolderResourceLoader(ramlFolder);
    ParseResult parseResult =
        new ParserService().parse(ApiReference.create(ROOT_RAML_RESOURCE_URL + raml, testScaffolderResourceLoader));
    assertTrue(parseResult.success());

    ScaffoldingConfiguration configuration =
        new ScaffoldingConfiguration.Builder().withApi(parseResult.get()).build();
    ScaffoldingResult result = mainAppScaffolder.run(configuration);

    verifySuccessfulScaffolding(result, "rescaffolding-apisync-version/v1/api.xml");

    List<MuleConfig> muleConfigs = new ArrayList<>(result.getGeneratedConfigs());

    ScaffoldingConfiguration secondScaffoldingConfiguration =
        new ScaffoldingConfiguration.Builder().withApi(parseResult.get()).withMuleConfigurations(muleConfigs).build();
    ScaffoldingResult secondScaffoldingResult = mainAppScaffolder.run(secondScaffoldingConfiguration);

    verifySuccessfulScaffolding(secondScaffoldingResult, "rescaffolding-apisync-version/v1/api.xml");
  }

  /**
   *
   * First it scaffolds an API normally with configuration of APIKit inside the main xml file with version of raml 0.0.1.
   * Finally it re-scaffolds with version 0.0.2.
   * Expected behaviour is that the new scaffolded API has version 0.0.2 in the main xml.
   *
   */
  @Test
  public void reScaffoldDifferentVersions() throws Exception {
    String raml = "api.raml";
    String ramlFolderV1 = "src/test/resources/rescaffolding-apisync-version/v1";
    String ramlFolderV2 = "src/test/resources/rescaffolding-apisync-version/v2";
    ScaffoldingResult result = scaffoldApiSync(raml, ramlFolderV1, ROOT_RAML_RESOURCE_URL, null);
    verifySuccessfulScaffolding(result, "rescaffolding-apisync-version/v1/api.xml");
    List<MuleConfig> muleConfigs = new ArrayList<>(result.getGeneratedConfigs());
    ScaffoldingResult rescaffoldResult = scaffoldApiSync(raml, ramlFolderV2, ROOT_RAML_RESOURCE_URL_V2, muleConfigs);
    verifySuccessfulScaffolding(rescaffoldResult, "rescaffolding-apisync-version/v2/api.xml");
  }

  /**
   *
   * First it scaffolds an API normally with configuration of APIKit inside the main xml file with version of raml 0.0.1.
   * Then it simulates the manual changing of file from main xml --> globals xml.
   * Finally it re-scaffolds with version 0.0.2.
   * Expected behaviour is that the new scaffolded API has version 0.0.2 in globals.xml and for the main xml it doesn't
   * add or remove anything.
   *
   */
  @Test
  public void reScaffoldDifferentVersionsWithGlobal() throws Exception {
    String raml = "api.raml";
    String ramlFolderV1 = "src/test/resources/rescaffolding-apisync-version/v1";
    String ramlFolderV2 = "src/test/resources/rescaffolding-apisync-version/v2";
    String firstScaffoldingPath = "rescaffolding-apisync-version/v1/api.xml";
    String secondScaffoldingApiPathV1 = "scaffolder-from-two-apis/simple/src/main/resources/api/api2-with-global/v1/api.xml";
    String secondScaffoldingGlobalPathV1 =
        "scaffolder-from-two-apis/simple/src/main/resources/api/api2-with-global/v1/global.xml";
    String secondScaffoldingApiPathV2 = "scaffolder-from-two-apis/simple/src/main/resources/api/api2-with-global/v2/api.xml";
    String secondScaffoldingGlobalPathV2 =
        "scaffolder-from-two-apis/simple/src/main/resources/api/api2-with-global/v2/global.xml";

    ScaffoldingResult result = scaffoldApiSync(raml, ramlFolderV1, ROOT_RAML_RESOURCE_URL, null);
    verifySuccessfulScaffolding(result, firstScaffoldingPath);
    List<MuleConfig> muleConfigs = changeGlobalsToSeparateFile(secondScaffoldingApiPathV1, secondScaffoldingGlobalPathV1);
    ScaffoldingResult secondScaffoldingResult = scaffoldApiSync(raml, ramlFolderV2, ROOT_RAML_RESOURCE_URL_V2, muleConfigs);
    verifySuccessfulScaffolding(secondScaffoldingResult,
                                new String[] {secondScaffoldingApiPathV2, secondScaffoldingGlobalPathV2});
  }

  private static List<MuleConfig> changeGlobalsToSeparateFile(String secondScaffoldingApiPathV1,
                                                              String secondScaffoldingGlobalPathV1)
      throws Exception {
    List<MuleConfig> configurations = new ArrayList<>();
    String[] existingMuleConfigPaths = {secondScaffoldingGlobalPathV1, secondScaffoldingApiPathV1};
    for (String path : existingMuleConfigPaths) {
      configurations.add(createConfig(path));
    }
    return configurations;
  }

  @After
  public void after() {
    System.clearProperty(TestUtils.PARSER_V2_PROPERTY);
  }
}
