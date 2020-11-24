/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import com.google.common.collect.Lists;
import org.junit.Test;
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
import org.mule.tools.apikit.model.ScaffolderResult;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.Helper.countOccurences;
import static org.mule.tools.apikit.TestUtils.getResourceAsStream;

public class MainAppScaffolderWithExistingConfigMule4Test extends AbstractScaffolderTestCase {

  @Test
  public void testAlreadyExistsMultipleConfigurationsFirstFlowsXml() throws Exception {
    String ramlFilePath = "scaffolder-existing-multiples/api.raml";
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFilePath));
    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);
    MuleConfig muleConfig1 =
        MuleConfigBuilder.fromStream(getResourceAsStream("scaffolder-existing-multiples/resources-flows.xml"));
    MuleConfig muleConfig2 =
        MuleConfigBuilder.fromStream(getResourceAsStream("scaffolder-existing-multiples/no-resources-flows.xml"));

    List<MuleConfig> muleConfigs = Lists.newArrayList(muleConfig1, muleConfig2);

    ScaffoldingConfiguration configuration = ScaffoldingConfiguration.builder()
        .withApi(parseResult.get())
        .withMuleConfigurations(muleConfigs)
        .build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertBooksApiScaffoldedContent(s);
  }

  @Test
  public void testAlreadyExistsMultipleConfigurationsFirstNoFlowsXml() throws Exception {
    String ramlFilePath = "scaffolder-existing-multiples/api.raml";
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFilePath));
    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);
    MuleConfig muleConfig1 =
        MuleConfigBuilder.fromStream(getResourceAsStream("scaffolder-existing-multiples/resources-flows.xml"));
    MuleConfig muleConfig2 =
        MuleConfigBuilder.fromStream(getResourceAsStream("scaffolder-existing-multiples/no-resources-flows.xml"));
    List<MuleConfig> muleConfigs = Lists.newArrayList(muleConfig2, muleConfig1);

    ScaffoldingConfiguration configuration = ScaffoldingConfiguration.builder()
        .withApi(parseResult.get())
        .withMuleConfigurations(muleConfigs)
        .build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertBooksApiScaffoldedContent(s);
  }

  @Test
  public void testAlreadyExistsWithExtensionNotPresent() throws Exception {
    String ramlFilePath = "scaffolder-existing-extension/simple.raml";
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFilePath));
    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);
    MuleConfig muleConfig =
        MuleConfigBuilder.fromStream(getResourceAsStream("scaffolder-existing-extension/simple-extension-not-present-4.xml"));
    List<MuleConfig> muleConfigs = Lists.newArrayList(muleConfig);
    ScaffoldingConfiguration configuration = ScaffoldingConfiguration.builder()
        .withApi(parseResult.get())
        .withMuleConfigurations(muleConfigs)
        .build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());
    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    XmlOccurrencesAsserterBuilder.XmlOccurrencesAsserter xmlOccurrencesAsserter = new XmlOccurrencesAsserterBuilder()
        .withOnErrorPropagateCount(6)
        .withHttplListenerConfigCount(1)
        .withHttplListenerCount(1)
        .withErrorHandlerTagCount(1)
        .withLoggerInfoCount(1)
        .build();
    xmlOccurrencesAsserter.assertOccurrences(s);
    assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\""));
    assertEquals(1, countOccurences(s, "http:listener-connection host=\"0.0.0.0\" port=\"${serverPort}\""));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
    assertEquals(1, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(1, countOccurences(s, "get:\\\""));
    assertEquals(0, countOccurences(s, "#[mel:null]"));
  }

  @Test
  public void testAlreadyExistsGenerate() throws Exception {
    String ramlFilePath = "scaffolder-existing-extension/simple.raml";
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFilePath));
    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);
    MuleConfig muleConfig = MuleConfigBuilder.fromStream(getResourceAsStream("scaffolder-existing/simple-4.xml"));
    List<MuleConfig> muleConfigs = Lists.newArrayList(muleConfig);

    ScaffoldingConfiguration configuration = ScaffoldingConfiguration.builder()
        .withApi(parseResult.get())
        .withMuleConfigurations(muleConfigs)
        .build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());
    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    XmlOccurrencesAsserterBuilder.XmlOccurrencesAsserter xmlOccurrencesAsserter = new XmlOccurrencesAsserterBuilder()
        .withHttpResponseStatusCode200Count(1)
        .withHttpResponseStatusCode500Count(1)
        .withHttpHeadersOutboundHeadersDefaultCount(2)
        .withOnErrorPropagateCount(5)
        .withEEMessageTagCount(5)
        .withEEVariablesTagCount(5)
        .withEESetVariableTagCount(5)
        .withEESetPayloadTagCount(5)
        .withHttpBodyCount(2)
        .withHttpHeadersCount(4)
        .withDWPayloadExpressionCount(1)
        .withHttplListenerConfigCount(1)
        .withHttplListenerCount(1)
        .withLoggerInfoCount(1)
        .build();
    xmlOccurrencesAsserter.assertOccurrences(s);
    assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\""));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
    assertEquals(1, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(1, countOccurences(s, "get:\\\""));
  }

  @Test
  public void testAlreadyExistsGenerateWithCustomDomain() throws Exception {
    String ramlFilePath = "scaffolder-existing-custom-lc/simple.raml";
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFilePath));
    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.EE).build();

    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);
    MuleConfig muleConfig = MuleConfigBuilder.fromStream(getResourceAsStream("scaffolder-existing-custom-lc/simple-4.xml"));
    List<MuleConfig> muleConfigs = Lists.newArrayList(muleConfig);

    MuleDomain muleDomain = MuleDomain.fromInputStream(getResourceAsStream("custom-domain-4/mule-domain-config.xml"));
    ScaffoldingConfiguration configuration = ScaffoldingConfiguration.builder().withApi(parseResult.get())
        .withMuleConfigurations(muleConfigs).withDomain(muleDomain).build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());
    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    XmlOccurrencesAsserterBuilder.XmlOccurrencesAsserter xmlOccurrencesAsserter = new XmlOccurrencesAsserterBuilder()
        .withOnErrorPropagateCount(6)
        .withHttplListenerCount(1)
        .withErrorHandlerTagCount(1)
        .withLoggerInfoCount(1)
        .build();
    xmlOccurrencesAsserter.assertOccurrences(s);
    assertEquals(0, countOccurences(s, "http:listener-connection"));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"http-lc-0.0.0.0-8081\" path=\"/api/*\""));
    assertEquals(1, countOccurences(s, "get:\\pet"));
    assertEquals(1, countOccurences(s, "get:\\\""));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
  }

  @Test
  public void testAlreadyExistsGenerateWithCustomAndNormalLC() throws Exception {
    String ramlFilePath = "scaffolder-existing-custom-and-normal-lc/leagues-custom-normal-lc.raml";
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFilePath));
    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);
    MuleConfig muleConfig = MuleConfigBuilder
        .fromStream(getResourceAsStream("scaffolder-existing-custom-and-normal-lc/leagues-custom-normal-lc-4.xml"));
    List<MuleConfig> muleConfigs = Lists.newArrayList(muleConfig);

    MuleDomain muleDomain = MuleDomain.fromInputStream(getResourceAsStream("custom-domain-4/mule-domain-config.xml"));
    ScaffoldingConfiguration configuration = ScaffoldingConfiguration.builder().withApi(parseResult.get())
        .withMuleConfigurations(muleConfigs).withDomain(muleDomain).build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());
    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    XmlOccurrencesAsserterBuilder.XmlOccurrencesAsserter xmlOccurrencesAsserter = new XmlOccurrencesAsserterBuilder()
        .withOnErrorPropagateCount(6)
        .withEEVariablesTagCount(2)
        .withEESetVariableTagCount(2)
        .withErrorHandlerTagCount(1)
        .withHttplListenerConfigCount(1)
        .withHttplListenerCount(2)
        .withLoggerInfoCount(2)
        .build();
    xmlOccurrencesAsserter.assertOccurrences(s);
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"http-lc-0.0.0.0-8081\" path=\"/api/*\""));
    assertEquals(2, countOccurences(s, "get:\\leagues\\(leagueId)"));
    assertEquals(2, countOccurences(s, "post:\\leagues\\(leagueId)"));
    assertEquals(1, countOccurences(s, "<http:listener config-ref=\"HTTP_Listener_Configuration\""));
    assertEquals(1, countOccurences(s, "<http:listener config-ref=\"http-lc-0.0.0.0-8081\""));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
  }

  @Test
  public void testAlreadyExistingMuleConfigWithApikitRouter() throws Exception {
    String ramlFilePath = "scaffolder-existing/simple.raml";
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFilePath));
    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);
    MuleConfig muleConfig =
        MuleConfigBuilder.fromStream(getResourceAsStream("scaffolder-existing/mule-config-no-api-flows-4.xml"));
    List<MuleConfig> muleConfigs = Lists.newArrayList(muleConfig);

    ScaffoldingConfiguration configuration = ScaffoldingConfiguration.builder()
        .withApi(parseResult.get())
        .withMuleConfigurations(muleConfigs)
        .build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());
    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    XmlOccurrencesAsserterBuilder.XmlOccurrencesAsserter xmlOccurrencesAsserter = new XmlOccurrencesAsserterBuilder()
        .withHttpResponseStatusCode200Count(1)
        .withHttpResponseStatusCode500Count(1)
        .withHttpHeadersOutboundHeadersDefaultCount(2)
        .withHttpBodyCount(2)
        .withHttpHeadersCount(4)
        .withDWPayloadExpressionCount(1)
        .withHttplListenerConfigCount(1)
        .withHttplListenerCount(1)
        .withLoggerInfoCount(3)
        .build();
    xmlOccurrencesAsserter.assertOccurrences(s);
    assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\">"));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
    assertEquals(1, countOccurences(s, "<apikit:router config-ref=\"apikit-config\" />"));
    assertEquals(2, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(2, countOccurences(s, "get:\\:"));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
  }

  @Test
  public void testMultipleMimeTypes() throws Exception {
    testMultipleMimeTypes("scaffolder/multipleMimeTypes.raml");
  }

  @Test
  public void testMultipleMimeTypesV10() throws Exception {
    testMultipleMimeTypes("scaffolder/multipleMimeTypesV10.raml");
  }

  private void testMultipleMimeTypes(String apiPath) throws Exception {
    final String name = fileNameWhithOutExtension(apiPath);
    ParseResult parseResult = new ParserService().parse(ApiReference.create(apiPath));
    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);
    ScaffoldingConfiguration configuration = ScaffoldingConfiguration.builder()
        .withApi(parseResult.get())
        .build();

    ScaffolderResult result = (ScaffolderResult) mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());
    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertTrue(s.contains("post:\\pet:application\\json:" + name + "-config"));
    assertTrue(s.contains("post:\\pet:text\\xml:" + name + "-config"));
    if (name.endsWith("V10")) {
      assertTrue(s.contains("post:\\pet:application\\xml:" + name + "-config"));
    } else {
      assertTrue(s.contains("post:\\pet:application\\x-www-form-urlencoded:" + name + "-config"));
    }
    assertTrue(s.contains("post:\\pet:application\\xml:" + name + "-config"));
    assertTrue(s.contains("post:\\vet:application\\xml:" + name + "-config"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
  }

  private void assertBooksApiScaffoldedContent(String s) {
    XmlOccurrencesAsserterBuilder.XmlOccurrencesAsserter xmlOccurrencesAsserter = new XmlOccurrencesAsserterBuilder()
        .withHttpResponseStatusCode200Count(2)
        .withHttpResponseStatusCode500Count(2)
        .withHttpHeadersOutboundHeadersDefaultCount(4)
        .withOnErrorPropagateCount(7)
        .withEEMessageTagCount(7)
        .withEEVariablesTagCount(7)
        .withEESetVariableTagCount(7)
        .withHttpBodyCount(4)
        .withHttpHeadersCount(8)
        .withEESetPayloadTagCount(7)
        .withDWPayloadExpressionCount(2)
        .withHttplListenerConfigCount(1)
        .withHttplListenerCount(2)
        .withApikitConsoleCount(1)
        .withLoggerInfoCount(2)
        .build();
    xmlOccurrencesAsserter.assertOccurrences(s);
    assertEquals(2, countOccurences(s, "get:\\books"));
    assertEquals(2, countOccurences(s, "put:\\shows"));
    assertEquals(0, countOccurences(s, "patch:\\movies"));
  }
}
