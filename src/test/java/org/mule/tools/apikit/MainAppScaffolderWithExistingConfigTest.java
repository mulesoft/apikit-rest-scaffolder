/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.Helper.countOccurences;
import static org.mule.tools.apikit.TestUtils.getResourceAsStream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleConfigBuilder;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffolderContextBuilder;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;

public class MainAppScaffolderWithExistingConfigTest extends AbstractScaffolderTestCase {

  @Test
  public void testApiReferencesWorkCorrectlyWithExistingConfig() throws Exception {
    MuleConfig muleConfig =
        scaffoldApi("scaffolder-with-global-apikit-config/api/simple.raml", "scaffolder-with-global-apikit-config/simple.xml",
                    "scaffolder-with-global-apikit-config/global.xml");
    String s = APIKitTools.readContents(muleConfig.getContent());

    assertEquals(2, countOccurences(s, "http:listener-config"));
    assertEquals(7, countOccurences(s, "http:listener"));
    assertEquals(6, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testAlreadyExistsOldGenerateWithOldParser() throws Exception {
    testAlreadyExistsOldGenerate();
  }

  @Test
  public void testAlreadyExistsOldGenerateWithNewParser() throws Exception {

    testAlreadyExistsOldGenerate();
  }

  private void testAlreadyExistsOldGenerate() throws Exception {
    MuleConfig muleConfig = scaffoldApi("scaffolder-existing-old/simple.raml", "scaffolder-existing-old/simple.xml");
    String s = APIKitTools.readContents(muleConfig.getContent());

    assertEquals(0, countOccurences(s, "http:listener-config"));
    assertEquals(0, countOccurences(s, "http:listener"));
    assertEquals(1, countOccurences(s, "http:inbound-endpoint port=\"${serverPort}\" host=\"localhost\" path=\"api\""));
    assertEquals(1, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testMultipleMimeTypesWithOldParser() throws Exception {
    testMultipleMimeTypes("scaffolder/multipleMimeTypes.raml");
  }

  @Test
  public void testMultipleMimeTypesWithNewParser() throws Exception {
    testMultipleMimeTypes("scaffolder/multipleMimeTypes.raml");
  }

  @Test
  public void testMultipleMimeTypesV10() throws Exception {
    testMultipleMimeTypes("scaffolder/multipleMimeTypesV10.raml");
  }

  private void testMultipleMimeTypes(final String apiPath) throws Exception {
    String name = fileNameWhithOutExtension(apiPath);
    MuleConfig muleConfig = scaffoldApi(apiPath, (String[]) null);
    String s = APIKitTools.readContents(muleConfig.getContent());
    assertTrue(s.contains("post:\\pet:application\\json:" + name + "-config"));
    assertTrue(s.contains("post:\\pet:text\\xml:" + name + "-config"));
    if (name.endsWith("V10")) {
      assertTrue(s.contains("post:\\pet:application\\xml:" + name + "-config"));
    } else {
      assertTrue(s.contains("post:\\pet:application\\x-www-form-urlencoded:" + name + "-config"));
    }
    assertTrue(s.contains("post:\\pet:application\\xml:" + name + "-config"));
    assertTrue(s.contains("post:\\vet:application\\xml:" + name + "-config"));;
    assertEquals(0, countOccurences(s, "extensionEnabled"));
  }

  private MuleConfig scaffoldApi(String apiPath, String... existingMuleConfigPaths) throws Exception {
    ApiReference apiReference = ApiReference.create(apiPath);
    ParseResult parseResult = new ParserService().parse(apiReference);
    assertTrue(parseResult.success());

    ScaffolderContext scaffolderContext = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(scaffolderContext);

    ScaffoldingConfiguration.Builder configurationBuilder = new ScaffoldingConfiguration.Builder().withApi(parseResult.get());

    if (existingMuleConfigPaths != null) {
      List<MuleConfig> configs = new ArrayList<MuleConfig>();
      for (String s : existingMuleConfigPaths) {
        try (InputStream muleConfigIS = getResourceAsStream(s)) {
          MuleConfig existingMuleConfig = MuleConfigBuilder.fromStream(muleConfigIS);
          configs.add(existingMuleConfig);
        }
      }
      configurationBuilder.withMuleConfigurations(configs);
    }

    ScaffoldingConfiguration configuration = configurationBuilder.build();
    ScaffoldingResult scaffoldingResult = mainAppScaffolder.run(configuration);
    assertTrue(scaffoldingResult.isSuccess());
    assertEquals(1, scaffoldingResult.getGeneratedConfigs().size());
    return scaffoldingResult.getGeneratedConfigs().get(0);
  }
}
