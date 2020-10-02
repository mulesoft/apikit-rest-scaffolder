/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.junit.Test;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.api.ApiReference;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffoldingResult;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class MainAppScaffolderWithExistingConfigApiSyncTest extends AbstractScaffolderTestCase {

  private final static String RAML_RESOURCE_URL_V1 = "resource::com.mycompany:raml-api:1.0.0:raml:zip:api.raml";
  private final static String RAML_RESOURCE_URL_V2 = "resource::com.mycompany:raml-api:2.0.0:raml:zip:api.raml";

  private static final String TEST_RESOURCES_APISYNC = "rescaffolding-apisync-version";
  private static final String TEST_RESOURCES_APISYNC_W_GLOBAL = "rescaffolding-apisync-version-with-global-config";

  /**
   * First it scaffolds an API normally with configuration of APIKit inside the main xml file with version of raml 1.0.0.
   * Finally it re-scaffolds with same version
   * Expected behaviour is that the new scaffolded API has version 1.0.0 in the main xml.
   */
  @Test
  public void reScaffold() throws Exception {
    ResourceLoader testScaffolderResourceLoader = new TestScaffolderResourceLoader(TEST_RESOURCES_APISYNC + "/v1");
    ApiReference apiReference = ApiReference.create(RAML_RESOURCE_URL_V1, testScaffolderResourceLoader);

    ScaffoldingResult result = scaffoldApi(RuntimeEdition.CE, apiReference);
    verifySuccessfulScaffolding(result, TEST_RESOURCES_APISYNC + "/v1/api.xml");

    List<MuleConfig> muleConfig = new ArrayList<>(result.getGeneratedConfigs());
    ScaffoldingResult secondScaffoldingResult = scaffoldApi(RuntimeEdition.CE, apiReference, muleConfig);

    verifySuccessfulScaffolding(secondScaffoldingResult, TEST_RESOURCES_APISYNC + "/v1/api.xml");
  }

  /**
   * First it scaffolds an API normally with configuration of APIKit inside the main xml file with version of raml 1.0.0.
   * Then it simulates the manual changing of file from main xml --> globals xml.
   * Finally it re-scaffolds with version 2.0.0.
   * Expected behaviour is that the new scaffolded API has version 2.0.0 in global.xml and the main xml has the new resource.
   */
  @Test
  public void reScaffoldDifferentVersionsWithGlobal() throws Exception {
    String existingConfigV1Folder = TEST_RESOURCES_APISYNC_W_GLOBAL + "/v1";
    String existingConfigV2Folder = TEST_RESOURCES_APISYNC_W_GLOBAL + "/v2";

    ResourceLoader testScaffolderResourceLoader = new TestScaffolderResourceLoader(existingConfigV1Folder);
    ApiReference apiReference = ApiReference.create(RAML_RESOURCE_URL_V1, testScaffolderResourceLoader);

    ScaffoldingResult result = scaffoldApi(RuntimeEdition.CE, apiReference, null,
                                           asList(existingConfigV1Folder + "/api.xml"));
    verifySuccessfulScaffolding(result, existingConfigV1Folder + "/api.xml");

    testScaffolderResourceLoader = new TestScaffolderResourceLoader(existingConfigV2Folder);
    apiReference = ApiReference.create(RAML_RESOURCE_URL_V2, testScaffolderResourceLoader);

    List<String> muleConfigs = asList(existingConfigV1Folder + "/api_refactored.xml", existingConfigV1Folder + "/global.xml");
    ScaffoldingResult rescaffoldResult =
        scaffoldApi(RuntimeEdition.CE, apiReference, null, muleConfigs);
    verifySuccessfulScaffolding(rescaffoldResult, existingConfigV2Folder + "/api_refactored.xml",
                                existingConfigV2Folder + "/global.xml");
  }

  /**
   * First it takes an API normally with configuration of APIKit, http and api autodiscovery inside global xml file.
   * Finally it simulates re-scaffolding.
   * Expected behaviour is that the new scaffolded API has two configurations, globals.xml(with common configurations) and api.xml.
   */
  @Test
  public void reScaffoldWithGlobalsAndAPIAutodiscovery() throws Exception {
    String existingConfigFolder = TEST_RESOURCES_APISYNC_W_GLOBAL + "/api-autodiscovery";

    ResourceLoader testScaffolderResourceLoader = new TestScaffolderResourceLoader(existingConfigFolder);
    ApiReference apiReference = ApiReference.create(RAML_RESOURCE_URL_V1, testScaffolderResourceLoader);

    String api = existingConfigFolder + "/api.xml";
    String global = existingConfigFolder + "/globals.xml";
    String expectedGlobals = existingConfigFolder + "/globals-with-autodiscovery.xml";
    List<MuleConfig> muleConfigsFromLocations = createMuleConfigsFromLocations(asList(api, global));

    //rescaffold
    testScaffolderResourceLoader = new TestScaffolderResourceLoader(existingConfigFolder);
    apiReference = ApiReference.create(RAML_RESOURCE_URL_V1, testScaffolderResourceLoader);

    ScaffoldingResult rescaffoldResult =
        scaffoldApi(RuntimeEdition.CE, apiReference, null, muleConfigsFromLocations, true, "globals.xml", "1234");
    verifySuccessfulScaffolding(rescaffoldResult, api, expectedGlobals);
  }

  /**
   * First it takes an API normally with configuration of APIKit, http and api autodiscovery inside global xml file.
   * Finally it simulates re-scaffolding.
   * Expected behaviour is that the new scaffolded API has two configurations, globals.xml(with common configurations) and api.xml.
   */
  @Test
  public void reScaffoldToOneFileFromTwoWithGlobalsAndAPIAutodiscoveryID() throws Exception {
    String existingConfigFolder = TEST_RESOURCES_APISYNC_W_GLOBAL + "/api-autodiscovery";

    String existingAPI = existingConfigFolder + "/api.xml";
    String existingGlobals = existingConfigFolder + "/globals.xml";
    String expectedAPI = existingConfigFolder + "/api-with-autodiscovery.xml";
    List<MuleConfig> muleConfigsFromLocations = createMuleConfigsFromLocations(asList(existingAPI, existingGlobals));

    //rescaffold
    ResourceLoader testScaffolderResourceLoader = new TestScaffolderResourceLoader(existingConfigFolder);
    ApiReference apiReference = ApiReference.create(RAML_RESOURCE_URL_V1, testScaffolderResourceLoader);

    ScaffoldingResult rescaffoldResult =
        scaffoldApi(RuntimeEdition.CE, apiReference, null, muleConfigsFromLocations, true, null, "5678");
    List<MuleConfig> muleConfigsFromLocations2 = createMuleConfigsFromLocations(asList(expectedAPI));
    verifySuccessfulScaffolding(rescaffoldResult, expectedAPI);
  }

  /**
   * First it scaffolds an API normally with configuration of APIKit inside the main xml file with version of raml 1.0.0.
   * Finally it re-scaffolds with version 2.0.0.
   * Expected behaviour is that the new scaffolded API has version 2.0.0 in the main xml.
   */
  @Test
  public void reScaffoldDifferentVersions() throws Exception {
    ResourceLoader testScaffolderResourceLoader = new TestScaffolderResourceLoader(TEST_RESOURCES_APISYNC + "/v1");
    ApiReference apiReference = ApiReference.create(RAML_RESOURCE_URL_V1, testScaffolderResourceLoader);

    ScaffoldingResult result = scaffoldApi(RuntimeEdition.CE, apiReference);
    verifySuccessfulScaffolding(result, TEST_RESOURCES_APISYNC + "/v1/api.xml");

    testScaffolderResourceLoader = new TestScaffolderResourceLoader(TEST_RESOURCES_APISYNC + "/v2");
    apiReference = ApiReference.create(RAML_RESOURCE_URL_V2, testScaffolderResourceLoader);

    List<MuleConfig> muleConfigs = new ArrayList<>(result.getGeneratedConfigs());
    ScaffoldingResult rescaffoldResult = scaffoldApi(RuntimeEdition.CE, apiReference, muleConfigs);
    verifySuccessfulScaffolding(rescaffoldResult, TEST_RESOURCES_APISYNC + "/v2/api.xml");
  }

  /**
   * Rescaffoldig of an API Sync without any changes that already has the APIKit config in a global.xml.
   * Expected behaviour is that the new scaffolded API has no changes.
   */
  @Test
  public void reScaffoldApiSyncWithGlobalWithoutChanges() throws Exception {
    String existingConfigFolder = TEST_RESOURCES_APISYNC_W_GLOBAL + "/v2";
    ResourceLoader testScaffolderResourceLoader = new TestScaffolderResourceLoader(existingConfigFolder);
    ApiReference apiReference = ApiReference.create(RAML_RESOURCE_URL_V2, testScaffolderResourceLoader);
    List<String> existingConfigLocations = asList(existingConfigFolder + "/api_refactored.xml",
                                                  existingConfigFolder + "/global.xml");
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.CE, apiReference, null, existingConfigLocations);
    verifySuccessfulScaffolding(result, existingConfigFolder + "/api_refactored.xml");
  }

}
