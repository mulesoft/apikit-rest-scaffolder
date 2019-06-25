/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mule.apikit.implv2.ParserV2Utils;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.model.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.Helper.countOccurences;

public class MainAppScaffolderWithExistingConfigMule4Test extends AbstractScaffolderTestCase {

  @Test
  public void testAlreadyExistsMultipleConfigurationsFirstFlowsXml() throws Exception {
    String ramlFilePath = "scaffolder-existing-multiples/api.raml";
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFilePath));
    ScaffolderContext context = new ScaffolderContext.Builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);
    MuleConfig muleConfig1 = MuleConfigBuilder.fromStream(MainAppScaffolderWithExistingConfigMule4Test.class.getClassLoader()
        .getResourceAsStream("scaffolder-existing-multiples/resources-flows.xml"));
    MuleConfig muleConfig2 = MuleConfigBuilder.fromStream(MainAppScaffolderWithExistingConfigMule4Test.class.getClassLoader()
        .getResourceAsStream("scaffolder-existing-multiples/no-resources-flows.xml"));
    List<MuleConfig> muleConfigs = new ArrayList<>();
    muleConfigs.add(muleConfig1);
    muleConfigs.add(muleConfig2);
    ScaffoldingConfiguration configuration =
        new ScaffoldingConfiguration.Builder().withApi(parseResult.get()).withMuleConfigurations(muleConfigs).build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());

    String s = IOUtils.toString(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(2, countOccurences(s, "get:\\books"));
    assertEquals(2, countOccurences(s, "put:\\shows"));
    assertEquals(0, countOccurences(s, "patch:\\movies"));
  }

  @Test
  public void testAlreadyExistsMultipleConfigurationsFirstNoFlowsXml() throws Exception {
    String ramlFilePath = "scaffolder-existing-multiples/api.raml";
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFilePath));
    ScaffolderContext context = new ScaffolderContext.Builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    MuleConfig muleConfig1 = MuleConfigBuilder.fromStream(MainAppScaffolderWithExistingConfigMule4Test.class.getClassLoader()
        .getResourceAsStream("scaffolder-existing-multiples/resources-flows.xml"));
    MuleConfig muleConfig2 = MuleConfigBuilder.fromStream(MainAppScaffolderWithExistingConfigMule4Test.class.getClassLoader()
        .getResourceAsStream("scaffolder-existing-multiples/no-resources-flows.xml"));
    List<MuleConfig> muleConfigs = new ArrayList<>();
    muleConfigs.add(muleConfig2);
    muleConfigs.add(muleConfig1);
    ScaffoldingConfiguration configuration =
        new ScaffoldingConfiguration.Builder().withApi(parseResult.get()).withMuleConfigurations(muleConfigs).build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());

    String s = IOUtils.toString(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(2, countOccurences(s, "get:\\books"));
    assertEquals(2, countOccurences(s, "put:\\shows"));
    assertEquals(0, countOccurences(s, "patch:\\movies"));
  }

  @Test
  public void testAlreadyExistsWithExtensionNotPresentWithOldParser() throws Exception {
    testAlreadyExistsWithExtensionNotPresent();
  }

  @Test
  public void testAlreadyExistsWithExtensionNotPresentWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistsWithExtensionNotPresent();
  }

  private void testAlreadyExistsWithExtensionNotPresent() throws Exception {
    String ramlFilePath = "scaffolder-existing-extension/simple.raml";
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFilePath));
    ScaffolderContext context = new ScaffolderContext.Builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    MuleConfig muleConfig = MuleConfigBuilder.fromStream(MainAppScaffolderWithExistingConfigMule4Test.class.getClassLoader()
        .getResourceAsStream("scaffolder-existing-extension/simple-extension-not-present-4.xml"));
    List<MuleConfig> muleConfigs = new ArrayList<>();
    muleConfigs.add(muleConfig);
    ScaffoldingConfiguration configuration =
        new ScaffoldingConfiguration.Builder().withApi(parseResult.get()).withMuleConfigurations(muleConfigs).build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());
    String s = IOUtils.toString(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\""));
    assertEquals(1, countOccurences(s, "http:listener-connection host=\"0.0.0.0\" port=\"${serverPort}\""));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
    assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
    assertEquals(1, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(1, countOccurences(s, "get:\\\""));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(0, countOccurences(s, "#[mel:null]"));
    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testAlreadyExistsGenerateWithOldParser() throws Exception {
    testAlreadyExistsGenerate();
  }

  @Test
  public void testAlreadyExistsGenerateWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistsGenerate();
  }

  private void testAlreadyExistsGenerate() throws Exception {
    String ramlFilePath = "scaffolder-existing-extension/simple.raml";
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFilePath));
    ScaffolderContext context = new ScaffolderContext.Builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    MuleConfig muleConfig = MuleConfigBuilder.fromStream(MainAppScaffolderWithExistingConfigMule4Test.class.getClassLoader()
        .getResourceAsStream("scaffolder-existing/simple-4.xml"));
    List<MuleConfig> muleConfigs = new ArrayList<>();
    muleConfigs.add(muleConfig);
    ScaffoldingConfiguration configuration =
        new ScaffoldingConfiguration.Builder().withApi(parseResult.get()).withMuleConfigurations(muleConfigs).build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());
    String s = IOUtils.toString(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\""));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
    assertEquals(0, countOccurences(s, "inbound-endpoint"));
    assertEquals(1, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(1, countOccurences(s, "get:\\\""));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(1, countOccurences(s, "#[payload]"));
    assertEquals(2, countOccurences(s, "http:body"));
    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testAlreadyExistsGenerateWithCustomDomainWithOldParser() throws Exception {
    testAlreadyExistsGenerateWithCustomDomain();
  }

  @Test
  public void testAlreadyExistsGenerateWithCustomDomainWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistsGenerateWithCustomDomain();
  }

  private void testAlreadyExistsGenerateWithCustomDomain() throws Exception {
    String ramlFilePath = "scaffolder-existing-custom-lc/simple.raml";
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFilePath));
    ScaffolderContext context = new ScaffolderContext.Builder().withRuntimeEdition(RuntimeEdition.EE).build();

    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);
    MuleConfig muleConfig = MuleConfigBuilder.fromStream(MainAppScaffolderWithExistingConfigMule4Test.class.getClassLoader()
        .getResourceAsStream("scaffolder-existing-custom-lc/simple-4.xml"));
    List<MuleConfig> muleConfigs = new ArrayList<>();
    muleConfigs.add(muleConfig);
    MuleDomain muleDomain = MuleDomain.fromInputStream(MainAppScaffolderWithExistingConfigMule4Test.class.getClassLoader()
        .getResourceAsStream("custom-domain-4/mule-domain-config.xml"));
    ScaffoldingConfiguration configuration = new ScaffoldingConfiguration.Builder().withApi(parseResult.get())
        .withMuleConfigurations(muleConfigs).withDomain(muleDomain).build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());
    String s = IOUtils.toString(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(0, countOccurences(s, "<http:listener-config"));
    assertEquals(0, countOccurences(s, "http:listener-connection"));

    assertEquals(1, countOccurences(s, "http:listener config-ref=\"http-lc-0.0.0.0-8081\" path=\"/api/*\""));
    assertEquals(0, countOccurences(s, "inbound-endpoint"));
    assertEquals(1, countOccurences(s, "get:\\pet"));
    assertEquals(1, countOccurences(s, "get:\\\""));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testAlreadyExistsGenerateWithCustomAndNormalLCWithOldParser() throws Exception {
    testAlreadyExistsGenerateWithCustomAndNormalLC();
  }

  @Test
  public void testAlreadyExistsGenerateWithCustomAndNormalLCWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistsGenerateWithCustomAndNormalLC();
  }

  private void testAlreadyExistsGenerateWithCustomAndNormalLC() throws Exception {
    String ramlFilePath = "scaffolder-existing-custom-and-normal-lc/leagues-custom-normal-lc.raml";
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFilePath));
    ScaffolderContext context = new ScaffolderContext.Builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    MuleConfig muleConfig = MuleConfigBuilder.fromStream(MainAppScaffolderWithExistingConfigMule4Test.class.getClassLoader()
        .getResourceAsStream("scaffolder-existing-custom-and-normal-lc/leagues-custom-normal-lc-4.xml"));
    List<MuleConfig> muleConfigs = new ArrayList<>();
    muleConfigs.add(muleConfig);
    MuleDomain muleDomain = MuleDomain.fromInputStream(MainAppScaffolderWithExistingConfigMule4Test.class.getClassLoader()
        .getResourceAsStream("custom-domain-4/mule-domain-config.xml"));
    ScaffoldingConfiguration configuration = new ScaffoldingConfiguration.Builder().withApi(parseResult.get())
        .withMuleConfigurations(muleConfigs).withDomain(muleDomain).build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());
    String s = IOUtils.toString(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"http-lc-0.0.0.0-8081\" path=\"/api/*\""));
    assertEquals(0, countOccurences(s, "inbound-endpoint"));
    assertEquals(2, countOccurences(s, "get:\\leagues\\(leagueId)"));
    assertEquals(2, countOccurences(s, "post:\\leagues\\(leagueId)"));
    assertEquals(1, countOccurences(s, "<http:listener config-ref=\"HTTP_Listener_Configuration\""));
    assertEquals(1, countOccurences(s, "<http:listener config-ref=\"http-lc-0.0.0.0-8081\""));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
    assertEquals(2, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testAlreadyExistingMuleConfigWithApikitRouterWithOldParser() throws Exception {
    testAlreadyExistingMuleConfigWithApikitRouter();
  }

  @Test
  public void testAlreadyExistingMuleConfigWithApikitRouterWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistingMuleConfigWithApikitRouter();
  }

  private void testAlreadyExistingMuleConfigWithApikitRouter() throws Exception {
    String ramlFilePath = "scaffolder-existing/simple.raml";
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFilePath));
    ScaffolderContext context = new ScaffolderContext.Builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    MuleConfig muleConfig = MuleConfigBuilder.fromStream(MainAppScaffolderWithExistingConfigMule4Test.class.getClassLoader()
        .getResourceAsStream("scaffolder-existing/mule-config-no-api-flows-4.xml"));
    List<MuleConfig> muleConfigs = new ArrayList<>();
    muleConfigs.add(muleConfig);
    ScaffoldingConfiguration configuration =
        new ScaffoldingConfiguration.Builder().withApi(parseResult.get()).withMuleConfigurations(muleConfigs).build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());
    String s = IOUtils.toString(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\">"));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
    assertEquals(1, countOccurences(s, "<apikit:router config-ref=\"apikit-config\" />"));
    assertEquals(0, countOccurences(s, "inbound-endpoint"));
    assertEquals(2, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(2, countOccurences(s, "get:\\:"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
    assertEquals(3, countOccurences(s, "<logger level=\"INFO\" message="));
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

  private void testMultipleMimeTypes(String apiPath) throws Exception {
    final String name = fileNameWhithOutExtension(apiPath);
    ParseResult parseResult = new ParserService().parse(ApiReference.create(apiPath));
    ScaffolderContext context = new ScaffolderContext.Builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);
    ScaffoldingConfiguration configuration = new ScaffoldingConfiguration.Builder().withApi(parseResult.get()).build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());
    String s = IOUtils.toString(result.getGeneratedConfigs().get(0).getContent());
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
}
