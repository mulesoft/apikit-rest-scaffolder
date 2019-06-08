/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.apikit.implv2.ParserV2Utils;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleConfigBuilder;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;

import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.Helper.countOccurences;

public class ScaffolderWithExistingConfigTest extends AbstractScaffolderTestCase {

  @Test
  public void testAlreadyExistsOldGenerateWithOldParser() throws Exception {
    testAlreadyExistsOldGenerate();
  }

  @Test
  public void testAlreadyExistsOldGenerateWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistsOldGenerate();
  }

  private void testAlreadyExistsOldGenerate() throws Exception {
    MuleConfig muleConfig = scaffoldApi("scaffolder-existing-old/simple.raml", "scaffolder-existing-old/simple.xml");
    String s = IOUtils.toString(muleConfig.getContent());

    assertEquals(0, countOccurences(s, "http:listener-config"));
    assertEquals(0, countOccurences(s, "http:listener"));
    assertEquals(1, countOccurences(s, "http:inbound-endpoint port=\"${serverPort}\" host=\"localhost\" path=\"api\""));
    assertEquals(1, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message="));
  }


  @Test
  public void testAlreadyExistsOldWithAddressGenerateWithOldParser() throws Exception {
    testAlreadyExistsOldWithAddressGenerate();
  }

  @Test
  public void testAlreadyExistsOldWithAddressGenerateWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistsOldWithAddressGenerate();
  }

  @Test
  @Ignore
  public void testAlreadyExistsOldWithAddressGenerate() throws Exception {
    MuleConfig muleConfig =
        scaffoldApi("scaffolder-existing-old-address/complex.raml", "scaffolder-existing-old-address/complex.xml");
    String s = IOUtils.toString(muleConfig.getContent());
    assertEquals(0, countOccurences(s, "http:listener-config"));
    assertEquals(0, countOccurences(s, "http:listener"));
    assertEquals(1, countOccurences(s, "http:inbound-endpoint address"));
    assertEquals(1, countOccurences(s, "name=\"put:\\clients\\(clientId):complex-config"));
    assertEquals(1, countOccurences(s, "put:\\invoices\\(invoiceId):complex-config"));
    assertEquals(1, countOccurences(s, "name=\"put:\\items\\(itemId):application\\json:complex-config"));
    assertEquals(2, countOccurences(s, "put:\\providers\\(providerId):complex-config"));
    assertEquals(2, countOccurences(s, "delete:\\clients\\(clientId):complex-config"));
    assertEquals(2, countOccurences(s, "delete:\\invoices\\(invoiceId):complex-config"));
    assertEquals(2, countOccurences(s, "delete:\\items\\(itemId):multipart\\form-data:complex-config"));
    assertEquals(2, countOccurences(s, "delete:\\providers\\(providerId):complex-config"));
    assertEquals(2, countOccurences(s, "get:\\:complex-config"));
    assertEquals(1, countOccurences(s, "name=\"get:\\clients\\(clientId):complex-config"));
    assertEquals(1, countOccurences(s, "name=\"get:\\clients:complex-config"));
    assertEquals(1, countOccurences(s, "get:\\invoices\\(invoiceId):complex-config"));
    assertEquals(1, countOccurences(s, "get:\\invoices:complex-config"));
    assertEquals(1, countOccurences(s, "get:\\items\\(itemId):complex-config"));
    assertEquals(1, countOccurences(s, "get:\\items:complex-config"));
    assertEquals(2, countOccurences(s, "get:\\providers\\(providerId):complex-config"));
    assertEquals(2, countOccurences(s, "get:\\providers:complex-config"));
    assertEquals(1, countOccurences(s, "name=\"post:\\clients:complex-config"));
    assertEquals(1, countOccurences(s, "post:\\invoices:complex-config"));
    assertEquals(2, countOccurences(s, "post:\\items:application\\json:complex-config"));
    assertEquals(2, countOccurences(s, "post:\\providers:complex-config"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
  }

  @Test
  public void testMultipleMimeTypesWithOldParser() throws Exception {
    testMultipleMimeTypes("scaffolder/multipleMimeTypes.raml");
  }

  @Test
  public void testMultipleMimeTypesWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testMultipleMimeTypes("scaffolder/multipleMimeTypes.raml");
  }

  @Test
  public void testMultipleMimeTypesV10() throws Exception {
    testMultipleMimeTypes("scaffolder/multipleMimeTypesV10.raml");
  }

  private void testMultipleMimeTypes(final String apiPath) throws Exception {
    String name = fileNameWhithOutExtension(apiPath);
    MuleConfig muleConfig = scaffoldApi(apiPath, null);
    String s = IOUtils.toString(muleConfig.getContent());
    assertTrue(s.contains("post:\\pet:application\\json:" + name + "-config"));
    assertTrue(s.contains("post:\\pet:text\\xml:" + name + "-config"));
    if (name.endsWith("V10")) {
      assertTrue(s.contains("post:\\pet:" + name + "-config"));
    } else {
      assertTrue(s.contains("post:\\pet:application\\x-www-form-urlencoded:" + name + "-config"));
    }
    assertTrue(s.contains("post:\\pet:" + name + "-config"));
    assertTrue(!s.contains("post:\\pet:application\\xml:" + name + "-config"));
    assertTrue(s.contains("post:\\vet:" + name + "-config"));
    assertTrue(!s.contains("post:\\vet:application\\xml:" + name + "-config"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
  }

  private MuleConfig scaffoldApi(String apiPath, String existingMuleConfigPath) throws Exception {
    ApiReference apiReference = ApiReference.create(apiPath);
    ParseResult parseResult = new ParserService().parse(apiReference);
    assertTrue(parseResult.success());

    ScaffolderContext scaffolderContext = new ScaffolderContext.Builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MuleScaffolder muleScaffolder = new MuleScaffolder(scaffolderContext);

    ScaffoldingConfiguration.Builder configurationBuilder = new ScaffoldingConfiguration.Builder().withApi(parseResult.get());

    if (existingMuleConfigPath != null) {
      InputStream muleConfigIS =
          ScaffolderWithExistingConfigTest.class.getClassLoader().getResourceAsStream(existingMuleConfigPath);
      MuleConfig existingMuleConfig = MuleConfigBuilder.fromStream(muleConfigIS);
      configurationBuilder.withMuleConfigurations(Arrays.asList(existingMuleConfig));
    }

    ScaffoldingConfiguration configuration = configurationBuilder.build();
    ScaffoldingResult scaffoldingResult = muleScaffolder.run(configuration);
    assertTrue(scaffoldingResult.isSuccess());
    assertEquals(1, scaffoldingResult.getGeneratedConfigs().size());
    return scaffoldingResult.getGeneratedConfigs().get(0);
  }
}
