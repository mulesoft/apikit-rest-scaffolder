/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffolderContextBuilder;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class MainAppScaffolderWithExistingConfigApiSyncTest extends AbstractScaffolderTestCase {

  private final static String ROOT_RAML_RESOURCE_URL = "resource::com.mycompany:raml-api:1.0.0:raml:zip:";
  private final static String ROOT_RAML_RESOURCE_URL_V2 = "resource::com.mycompany:raml-api:2.0.0:raml:zip:";
  public static final String RESCAFFOLDING_APISYNC_VERSION = "rescaffolding-apisync-version";
  public static final String ROOT_TEST_RESOURCES = "src/test/resources/";
  public static final String ROOT_SCAFFOLDER_FROM_TWO_APIS =
      "scaffolder-from-two-apis/simple/src/main/resources/api/api2-with-global/";
  public static final String SLASH = "/";
  public static final String V1 = "v1";
  public static final String V2 = "v2";
  public static final String APIXML = "api.xml";
  public static final String GLOBAL = "global.xml";
  public static final String APIRAML = "api.raml";
  private final static String RAML_RESOURCE_URL = ROOT_RAML_RESOURCE_URL + APIRAML;
  public static final String RAML_FOLDER_V1 = ROOT_TEST_RESOURCES + RESCAFFOLDING_APISYNC_VERSION + SLASH + V1;
  public static final String RAML_FOLDER_V2 = ROOT_TEST_RESOURCES + RESCAFFOLDING_APISYNC_VERSION + SLASH + V2;
  public static final String ROOT_RESCAFFOLDING_APISYNC_V1 = RESCAFFOLDING_APISYNC_VERSION + SLASH + V1 + SLASH + APIXML;
  public static final String ROOT_RESCAFFOLDING_APISYNC_V2 = RESCAFFOLDING_APISYNC_VERSION + SLASH + V2 + SLASH + APIXML;
  public static final String SCAFFOLDER_FROM_TWO_APIS_V1_API = ROOT_SCAFFOLDER_FROM_TWO_APIS + V1 + SLASH + APIXML;
  public static final String SCAFFOLDER_FROM_TWO_APIS_V1_GLOBALS = ROOT_SCAFFOLDER_FROM_TWO_APIS + V1 + SLASH + GLOBAL;
  public static final String SCAFFOLDER_FROM_TWO_APIS_V2_API = ROOT_SCAFFOLDER_FROM_TWO_APIS + V2 + SLASH + APIXML;
  public static final String SCAFFOLDER_FROM_TWO_APIS_V2_GLOBALS = ROOT_SCAFFOLDER_FROM_TWO_APIS + V2 + SLASH + GLOBAL;


  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Before
  public void setUp() throws IOException {
    folder.newFolder(RESCAFFOLDING_APISYNC_VERSION);
    folder.newFolder(RESCAFFOLDING_APISYNC_VERSION, V1);
    folder.newFolder(RESCAFFOLDING_APISYNC_VERSION, "v2");
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
    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.CE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    ResourceLoader testScaffolderResourceLoader = new TestScaffolderResourceLoader(RAML_FOLDER_V1);
    ParseResult parseResult =
        new ParserService().parse(ApiReference.create(RAML_RESOURCE_URL, testScaffolderResourceLoader));
    assertTrue(parseResult.success());

    ScaffoldingConfiguration configuration =
        new ScaffoldingConfiguration.Builder().withApi(parseResult.get()).build();
    ScaffoldingResult result = mainAppScaffolder.run(configuration);

    verifySuccessfulScaffolding(result, ROOT_RESCAFFOLDING_APISYNC_V1);

    List<MuleConfig> muleConfigs = new ArrayList<>(result.getGeneratedConfigs());

    ScaffoldingConfiguration secondScaffoldingConfiguration =
        new ScaffoldingConfiguration.Builder().withApi(parseResult.get()).withMuleConfigurations(muleConfigs).build();
    ScaffoldingResult secondScaffoldingResult = mainAppScaffolder.run(secondScaffoldingConfiguration);

    verifySuccessfulScaffolding(secondScaffoldingResult, ROOT_RESCAFFOLDING_APISYNC_V1);
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
    ScaffoldingResult result = scaffoldApiSync(APIRAML, RAML_FOLDER_V1, ROOT_RAML_RESOURCE_URL, null);
    verifySuccessfulScaffolding(result, ROOT_RESCAFFOLDING_APISYNC_V1);
    List<MuleConfig> muleConfigs = new ArrayList<>(result.getGeneratedConfigs());
    ScaffoldingResult rescaffoldResult = scaffoldApiSync(APIRAML, RAML_FOLDER_V2, ROOT_RAML_RESOURCE_URL_V2, muleConfigs);
    verifySuccessfulScaffolding(rescaffoldResult, ROOT_RESCAFFOLDING_APISYNC_V2);
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
    ScaffoldingResult result = scaffoldApiSync(APIRAML, RAML_FOLDER_V1, ROOT_RAML_RESOURCE_URL, null);
    verifySuccessfulScaffolding(result, ROOT_RESCAFFOLDING_APISYNC_V1);
    List<MuleConfig> muleConfigs =
        Arrays.asList(createConfig(SCAFFOLDER_FROM_TWO_APIS_V1_API), createConfig(SCAFFOLDER_FROM_TWO_APIS_V1_GLOBALS));
    ScaffoldingResult secondScaffoldingResult = scaffoldApiSync(APIRAML, RAML_FOLDER_V2, ROOT_RAML_RESOURCE_URL_V2, muleConfigs);
    verifySuccessfulScaffolding(secondScaffoldingResult, SCAFFOLDER_FROM_TWO_APIS_V2_API, SCAFFOLDER_FROM_TWO_APIS_V2_GLOBALS);
  }

  @Override
  @After
  public void after() {
    System.clearProperty(TestUtils.PARSER_V2_PROPERTY);
  }
}
