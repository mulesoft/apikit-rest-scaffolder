/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.api.ApiReference;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleConfigBuilder;
import org.mule.tools.apikit.model.MuleDomain;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffolderContextBuilder;
import org.mule.tools.apikit.model.ScaffoldingAccessories;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class MainAppScaffolderWithExistingConfigApiSyncTest extends AbstractScaffolderTestCase {

  private final static String RAML_RESOURCE_URL_V1 = "resource::com.mycompany:raml-api:1.0.0:raml:zip:api.raml";
  private final static String RAML_RESOURCE_URL_V2 = "resource::com.mycompany:raml-api:2.0.0:raml:zip:api.raml";

  private static final String TEST_RESOURCES_APISYNC = "rescaffolding-apisync-version";
  private static final String TEST_RESOURCES_APISYNC_W_GLOBAL = "rescaffolding-apisync-version-with-global-config";
  public static final String COLON = ":";

  /**
   * First it scaffolds an API normally with configuration of APIKit inside the main xml file with version of raml 1.0.0.
   * Finally it re-scaffolds with same version
   * Expected behaviour is that the new scaffolded API has version 1.0.0 in the main xml.
   */
  @Test
  public void reScaffold() throws Exception {
    ResourceLoader testScaffolderResourceLoader = new TestScaffolderResourceLoader(TEST_RESOURCES_APISYNC + "/v1");
    ApiReference apiReference = ApiReference.create(RAML_RESOURCE_URL_V1, testScaffolderResourceLoader);
    ApiSpecification apiSpecification = buildApiSpecification(apiReference);
    ScaffoldingConfiguration.Builder builder = ScaffoldingConfiguration.builder().withApi(apiSpecification);
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.CE, builder.build());
    verifySuccessfulScaffolding(result, TEST_RESOURCES_APISYNC + "/v1/api.xml");

    List<MuleConfig> muleConfig = new ArrayList<>(result.getGeneratedConfigs());
    builder.withMuleConfigurations(muleConfig);
    ScaffoldingResult secondScaffoldingResult = scaffoldApi(RuntimeEdition.CE, builder.build());

    verifySuccessfulScaffolding(secondScaffoldingResult, TEST_RESOURCES_APISYNC + "/v1/api.xml");
  }

  @Test
  public void testRescaffoldSameVersionsGlobals() throws Exception {
    ScaffoldingConfiguration.Builder configurationBuilder = ScaffoldingConfiguration.builder();
    List<MuleConfig> muleConfigs = new ArrayList<>();
    InputStream api =
        new FileInputStream("src/test/resources/rescaffolding-apisync-version-with-global-config/pre-existing/api.xml");
    InputStream global =
        new FileInputStream("src/test/resources/rescaffolding-apisync-version-with-global-config/pre-existing/global.xml");
    muleConfigs.add(MuleConfigBuilder.fromStream(api));
    muleConfigs.add(MuleConfigBuilder.fromStream(global));
    configurationBuilder.withMuleConfigurations(muleConfigs);
    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);
    ObjectMapper mapper = new ObjectMapper();
    InputStream scaffoldingConfigurationFile =
        new FileInputStream("src/test/resources/rescaffolding-apisync-version-with-global-config/pre-existing/configuration.json");
    ScaffoldingAccessories scaffoldingAccessories =
        mapper.readValue(scaffoldingConfigurationFile, ScaffoldingAccessories.class);
    configurationBuilder.withExternalCommonFile(scaffoldingAccessories.getExternalCommonFile());
    configurationBuilder.withApiId(scaffoldingAccessories.getApiId());
    configurationBuilder.withProperties(scaffoldingAccessories.getProperties());
    String resourceForApiSync = createResourceForApiSync("967b013a-46fe-4be7-8eb5-c91caebf3bc0", "test-yaml", "1.0.0");
    configurationBuilder
        .withApiSyncResource(resourceForApiSync);
    String existingConfigV1Folder = TEST_RESOURCES_APISYNC_W_GLOBAL + "/v1";
    ResourceLoader testScaffolderResourceLoader = new TestScaffolderResourceLoader(existingConfigV1Folder);
    ApiReference apiReference = ApiReference.create(RAML_RESOURCE_URL_V1, testScaffolderResourceLoader);
    ApiSpecification apiSpecification = buildApiSpecification(apiReference);
    ScaffoldingConfiguration scaffoldingConfiguration = ScaffoldingConfiguration.builder().withApi(apiSpecification)
        .withDomain(MuleDomain.builder().build()).withMuleConfigurations(muleConfigs)
        .withExternalCommonFile(scaffoldingAccessories.getExternalCommonFile()).withApiId(scaffoldingAccessories.getApiId())
        .withProperties(scaffoldingAccessories.getProperties()).withApiSyncResource(resourceForApiSync).build();
    ScaffoldingResult scaffoldingResult =
        scaffoldApi(RuntimeEdition.EE, scaffoldingConfiguration);
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

    ApiSpecification apiSpecification = buildApiSpecification(apiReference);
    ScaffoldingConfiguration scaffoldingConfiguration = ScaffoldingConfiguration.builder().withApi(apiSpecification)
        .withMuleConfigurations(createMuleConfigsFromLocations(asList(existingConfigV1Folder + "/api.xml"))).build();


    ScaffoldingResult result = scaffoldApi(RuntimeEdition.CE, scaffoldingConfiguration);
    verifySuccessfulScaffolding(result, existingConfigV1Folder + "/api.xml");

    testScaffolderResourceLoader = new TestScaffolderResourceLoader(existingConfigV2Folder);
    apiReference = ApiReference.create(RAML_RESOURCE_URL_V2, testScaffolderResourceLoader);

    List<String> muleConfigs = asList(existingConfigV1Folder + "/api_refactored.xml", existingConfigV1Folder + "/global.xml");

    apiSpecification = buildApiSpecification(apiReference);
    ScaffoldingConfiguration scaffoldingConfiguration2 = ScaffoldingConfiguration.builder().withApi(apiSpecification)
        .withMuleConfigurations(createMuleConfigsFromLocations(muleConfigs)).build();


    ScaffoldingResult rescaffoldResult =
        scaffoldApi(RuntimeEdition.CE, scaffoldingConfiguration2);
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

    String api = existingConfigFolder + "/actual/api.xml";
    String global = existingConfigFolder + "/actual/globals.xml";
    String expectedGlobals = existingConfigFolder + "/expected/globals.xml";
    List<MuleConfig> muleConfigs = createMuleConfigsFromLocations(asList(api, global));

    //rescaffold
    ApiSpecification apiSpecification = buildApiSpecification(apiReference);
    ScaffoldingConfiguration scaffoldingConfiguration =
        ScaffoldingConfiguration.builder().withApi(apiSpecification).withDomain(MuleDomain.builder().build())
            .withMuleConfigurations(muleConfigs).withExternalCommonFile("globals.xml").withApiId("1234").build();

    ScaffoldingResult rescaffoldResult =
        scaffoldApi(RuntimeEdition.CE, scaffoldingConfiguration);
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

    String existingAPI = existingConfigFolder + "/actual/api.xml";
    String existingGlobals = existingConfigFolder + "/actual/globals.xml";
    String expectedAPI = existingConfigFolder + "/expected/api.xml";
    List<MuleConfig> muleConfigs = createMuleConfigsFromLocations(asList(existingAPI, existingGlobals));

    //rescaffold
    ResourceLoader testScaffolderResourceLoader = new TestScaffolderResourceLoader(existingConfigFolder);
    ApiReference apiReference = ApiReference.create(RAML_RESOURCE_URL_V1, testScaffolderResourceLoader);

    ApiSpecification apiSpecification = buildApiSpecification(apiReference);
    ScaffoldingConfiguration scaffoldingConfiguration = ScaffoldingConfiguration.builder().withApi(apiSpecification)
        .withMuleConfigurations(muleConfigs).withApiId("5678").build();

    ScaffoldingResult rescaffoldResult =
        scaffoldApi(RuntimeEdition.CE, scaffoldingConfiguration);
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
    ApiSpecification apiSpecification = buildApiSpecification(apiReference);
    ScaffoldingConfiguration.Builder scaffoldingConfiguration = ScaffoldingConfiguration.builder().withApi(apiSpecification);

    ScaffoldingResult result = scaffoldApi(RuntimeEdition.CE, scaffoldingConfiguration.build());
    verifySuccessfulScaffolding(result, TEST_RESOURCES_APISYNC + "/v1/api.xml");

    testScaffolderResourceLoader = new TestScaffolderResourceLoader(TEST_RESOURCES_APISYNC + "/v2");
    apiReference = ApiReference.create(RAML_RESOURCE_URL_V2, testScaffolderResourceLoader);

    List<MuleConfig> muleConfigs = new ArrayList<>(result.getGeneratedConfigs());
    scaffoldingConfiguration.withApi(buildApiSpecification(apiReference));
    scaffoldingConfiguration.withMuleConfigurations(muleConfigs);
    ScaffoldingResult rescaffoldResult = scaffoldApi(RuntimeEdition.CE, scaffoldingConfiguration.build());
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
    ApiSpecification apiSpecification = buildApiSpecification(apiReference);
    ScaffoldingConfiguration scaffoldingConfiguration = ScaffoldingConfiguration.builder().withApi(apiSpecification)
        .withMuleConfigurations(createMuleConfigsFromLocations(existingConfigLocations)).build();

    ScaffoldingResult result =
        scaffoldApi(RuntimeEdition.CE, scaffoldingConfiguration);
    verifySuccessfulScaffolding(result, existingConfigFolder + "/api_refactored.xml");
  }

  private String createResourceForApiSync(String groupId, String artifact, String version) {
    return "resource::".concat(groupId).concat(COLON).concat(artifact).concat(COLON)
        .concat(version).concat(COLON).concat("raml").concat(COLON).concat("zip")
        .concat(COLON).concat(artifact).concat(".raml");
  }

}
