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
import org.mule.tools.apikit.model.ScaffoldingResult;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mule.apikit.model.api.ApiReference.create;
import static org.mule.tools.apikit.model.MuleConfigBuilder.fromStream;
import static org.mule.tools.apikit.model.RuntimeEdition.CE;

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
    ApiReference apiReference = create(RAML_RESOURCE_URL_V1, testScaffolderResourceLoader);

    ScaffoldingResult result = scaffoldApi(CE, apiReference);
    verifySuccessfulScaffolding(result, TEST_RESOURCES_APISYNC + "/v1/api.xml");

    List<MuleConfig> muleConfig = new ArrayList<>(result.getGeneratedConfigs());
    ScaffoldingResult secondScaffoldingResult = scaffoldApi(CE, apiReference, muleConfig);

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
    ApiReference apiReference = create(RAML_RESOURCE_URL_V1, testScaffolderResourceLoader);

    ScaffoldingResult result = scaffoldApi(CE, apiReference, null,
                                           asList(existingConfigV1Folder + "/api.xml"));
    verifySuccessfulScaffolding(result, existingConfigV1Folder + "/api.xml");

    testScaffolderResourceLoader = new TestScaffolderResourceLoader(existingConfigV2Folder);
    apiReference = create(RAML_RESOURCE_URL_V2, testScaffolderResourceLoader);

    List<String> muleConfigs = asList(existingConfigV1Folder + "/api_refactored.xml", existingConfigV1Folder + "/global.xml");
    ScaffoldingResult rescaffoldResult =
        scaffoldApi(CE, apiReference, null, muleConfigs);
    verifySuccessfulScaffolding(rescaffoldResult, existingConfigV2Folder + "/api_refactored.xml",
                                existingConfigV2Folder + "/global.xml");
  }

  /**
   * First it scaffolds an API normally with configuration of APIKit inside the main xml file with version of raml 1.0.0.
   * Finally it re-scaffolds with version 2.0.0.
   * Expected behaviour is that the new scaffolded API has version 2.0.0 in the main xml.
   */
  @Test
  public void reScaffoldDifferentVersions() throws Exception {
    ResourceLoader testScaffolderResourceLoader = new TestScaffolderResourceLoader(TEST_RESOURCES_APISYNC + "/v1");
    ApiReference apiReference = create(RAML_RESOURCE_URL_V1, testScaffolderResourceLoader);

    ScaffoldingResult result = scaffoldApi(CE, apiReference);
    verifySuccessfulScaffolding(result, TEST_RESOURCES_APISYNC + "/v1/api.xml");

    testScaffolderResourceLoader = new TestScaffolderResourceLoader(TEST_RESOURCES_APISYNC + "/v2");
    apiReference = create(RAML_RESOURCE_URL_V2, testScaffolderResourceLoader);

    List<MuleConfig> muleConfigs = new ArrayList<>(result.getGeneratedConfigs());
    ScaffoldingResult rescaffoldResult = scaffoldApi(CE, apiReference, muleConfigs);
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
    ApiReference apiReference = create(RAML_RESOURCE_URL_V2, testScaffolderResourceLoader);
    List<String> existingConfigLocations = asList(existingConfigFolder + "/api_refactored.xml",
                                                  existingConfigFolder + "/global.xml");
    ScaffoldingResult result = scaffoldApi(CE, apiReference, null, existingConfigLocations);
    verifySuccessfulScaffolding(result, existingConfigFolder + "/api_refactored.xml");
  }


  @Test
  public void reScaffoldLossesDisableValidations() throws Exception {
    ResourceLoader resourceLoader = new TestScaffolderResourceLoader("rescaffolding-losses-disable-validations/v2");
    ApiReference apiReference = create(RAML_RESOURCE_URL_V2, resourceLoader);

    MuleConfig existingConfig = fromStream(
                                           currentThread().getContextClassLoader()
                                               .getResourceAsStream("rescaffolding-losses-disable-validations/v1/api.xml"));

    existingConfig.setName("api.xml");

    List<MuleConfig> muleConfigs = singletonList(existingConfig);

    ScaffoldingResult reScaffoldingResult = scaffoldApi(CE, apiReference, muleConfigs);

    verifySuccessfulScaffolding(reScaffoldingResult, "rescaffolding-losses-disable-validations/v2/api.xml");
  }

}
