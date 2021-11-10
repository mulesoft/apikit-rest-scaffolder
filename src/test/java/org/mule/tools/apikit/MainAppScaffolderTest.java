/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import com.google.common.collect.Lists;
import org.apache.commons.lang.SystemUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.XmlOccurrencesAsserterBuilder.XmlOccurrencesAsserter;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleConfigBuilder;
import org.mule.tools.apikit.model.MuleDomain;
import org.mule.tools.apikit.model.MuleDomainFactory;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffolderContextBuilder;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;
import static org.mule.tools.apikit.Helper.countOccurences;
import static org.mule.tools.apikit.ScaffolderExecutionBuilder.when;
import static org.mule.tools.apikit.TestUtils.assertXmls;
import static org.mule.tools.apikit.TestUtils.getResourceAsStream;
import static org.mule.tools.apikit.TestUtils.getResourceAsString;

public class MainAppScaffolderTest extends AbstractScaffolderTestCase {

  @Test
  public void testSimpleGenerateV08() throws Exception {
    simpleGenerate("scaffolder/simple.raml", false);
  }

  @Test
  public void testSimpleGenerateV10() throws Exception {
    simpleGenerate("scaffolder/simpleV10.raml", false);
  }

  @Test
  public void testSimpleGenerateV10HideConsole() throws Exception {
    simpleGenerate("scaffolder/simpleV10.raml", true);
  }

  @Test
  public void testSimpleGenerateForCEV08() throws Exception {
    simpleGenerateForCE("scaffolder/simple.raml");
  }

  @Test
  public void testSimpleGenerateForCEV10() throws Exception {
    simpleGenerateForCE("scaffolder/simpleV10.raml");
  }

  @Test
  public void testSimpleGenerateWithExtension() throws Exception {
    when()
        .api("scaffolder/simple.raml")
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(7)
            .withEEVariablesTagCount(9)
            .withEESetVariableTagCount(10)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withEESetPayloadTagCount(7)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerConfigCount(1)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(5)
            .build());
  }

  @Test
  public void generateWithIncludes08() throws Exception {
    // TODO(APIMF-1705): ignoring this test when running on windows until AMF fixes the referenced issue
    assumeThat(SystemUtils.IS_OS_WINDOWS && isAmf(), is(false));

    when()
        .api("scaffolder-include-08/api.raml")
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(7)
            .withEEVariablesTagCount(7)
            .withEESetVariableTagCount(7)
            .withEESetPayloadTagCount(7)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerConfigCount(1)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(2)
            .withCustomAssertionCount("post:\\Queue:application\\json:api-config", 2)
            .withCustomAssertionCount("post:\\Queue:text\\xml:api-config", 2)
            .withCustomAssertionCount("#[NullPayload.getInstance()]", 0)
            .build());
  }

  @Test
  public void testSimpleGenerateWithExtensionInNull() throws Exception {
    String configContent = when()
        .api("scaffolder/simple.raml")
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(7)
            .withEEVariablesTagCount(9)
            .withEESetVariableTagCount(10)
            .withEESetPayloadTagCount(7)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerConfigCount(1)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(5)
            .build())
        .getConfigContent();

    assertPetApiScaffoldedContent(configContent);
  }

  @Test
  public void generateWithIncludes10() throws Exception {
    // TODO(APIMF-1705): ignoring this test when running on windows until AMF fixes the referenced issue
    assumeThat(SystemUtils.IS_OS_WINDOWS && isAmf(), is(false));

    when()
        .api("scaffolder-include-10/api.raml")
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(7)
            .withEEVariablesTagCount(7)
            .withEESetVariableTagCount(7)
            .withEESetPayloadTagCount(7)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerConfigCount(1)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(2)
            .withCustomAssertionCount("post:\\Queue:application\\json:api-config", 2)
            .withCustomAssertionCount("post:\\Queue:text\\xml:api-config", 2)
            .build());

  }

  @Test
  public void generateWithExamples() throws Exception {
    String apiLocation = "scaffolder-with-examples/src/main/resources/api/api.raml";

    String configContent = when()
        .api(apiLocation)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .getConfigContent();

    String expected = getResourceAsString("scaffolder-with-examples/api.xml");
    assertXmls(configContent, expected);
  }

  @Test
  public void testSimpleGenerateWithListenerAndExtension() throws Exception {
    String apiLocation = "scaffolder/simple.raml";

    String configContent = when()
        .api(apiLocation)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(7)
            .withEEVariablesTagCount(9)
            .withEESetVariableTagCount(10)
            .withEESetPayloadTagCount(7)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerConfigCount(1)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(5)
            .build())
        .getConfigContent();

    assertPetApiScaffoldedContent(configContent);
  }

  @Test
  public void testSimpleGenerateWithCustomDomain() throws Exception {
    String configContent = when()
        .api("scaffolder/simple.raml")
        .domain("custom-domain-4/mule-domain-config.xml")
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(7)
            .withEEVariablesTagCount(9)
            .withEESetVariableTagCount(10)
            .withEESetPayloadTagCount(7)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(5)
            .build())
        .getConfigContent();

    assertEquals(2, countOccurences(configContent, "config-ref=\"http-lc-0.0.0.0-8081\""));
    assertPetApiScaffoldedContent(configContent);
  }

  @Test
  public void testSimpleGenerateWithCustomExternalDomain() throws Exception {
    String apiLocation = "scaffolder/simple.raml";
    URL fileUrl = Thread.currentThread().getContextClassLoader().getResource("custom-domain-4/external-domain.jar");
    File artifact = new File(fileUrl.getFile());
    MuleDomain muleDomain = MuleDomainFactory.fromDeployableArtifact(artifact);

    String configContent = when()
        .api(apiLocation)
        .domain(muleDomain)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(7)
            .withEEVariablesTagCount(9)
            .withEESetVariableTagCount(10)
            .withEESetPayloadTagCount(7)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(5)
            .build())
        .getConfigContent();

    assertEquals(2, countOccurences(configContent, "config-ref=\"http-lc-0.0.0.0-8081\""));
    assertPetApiScaffoldedContent(configContent);
  }

  @Test
  public void testSimpleGenerateWithCustomDomainAndExtension() throws Exception {
    String apiLocation = "scaffolder/simple.raml";
    String muleDomainLocation = "custom-domain-4/mule-domain-config.xml";

    String configContent = when()
        .api(apiLocation)
        .domain(muleDomainLocation)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(7)
            .withEEVariablesTagCount(9)
            .withEESetVariableTagCount(10)
            .withEESetPayloadTagCount(7)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(5)
            .build())
        .getConfigContent();

    assertEquals(2, countOccurences(configContent, "config-ref=\"http-lc-0.0.0.0-8081\""));
    assertPetApiScaffoldedContent(configContent);
  }

  @Test
  public void testSimpleGenerateWithCustomDomainWithMultipleLC() throws Exception {
    String apiLocation = "scaffolder/simple.raml";
    String muleDomainLocation = "custom-domain-multiple-lc-4/mule-domain-config.xml";

    String configContent = when()
        .api(apiLocation)
        .domain(muleDomainLocation)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(7)
            .withEEVariablesTagCount(9)
            .withEESetVariableTagCount(10)
            .withEESetPayloadTagCount(7)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(5)
            .build())
        .getConfigContent();

    assertEquals(2, countOccurences(configContent, "config-ref=\"abcd\""));
    assertPetApiScaffoldedContent(configContent);
  }

  @Test
  public void testSimpleGenerateWithCustomExternalDomainWithMultipleConfigs() throws Exception {
    String apiLocation = "scaffolder/simple.raml";
    URL fileUrl = Thread.currentThread().getContextClassLoader()
        .getResource("custom-external-domain-multiple-configs/external-domain-2-configs.jar");
    File artifact = new File(fileUrl.getFile());
    MuleDomain muleDomain = MuleDomainFactory.fromDeployableArtifact(artifact);

    String configContent = when()
        .api(apiLocation)
        .domain(muleDomain)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(7)
            .withEEVariablesTagCount(9)
            .withEESetVariableTagCount(10)
            .withEESetPayloadTagCount(7)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(5)
            .build())
        .getConfigContent();

    assertEquals(2, countOccurences(configContent, "config-ref=\"http-lc-0.0.0.0-8081\""));
    assertPetApiScaffoldedContent(configContent);
  }

  @Test
  public void testSimpleGenerateWithEmptyDomain() throws Exception {
    String apiLocation = "scaffolder/simple.raml";
    String muleDomainLocation = "empty-domain/mule-domain-config.xml";

    String configContent = when()
        .api(apiLocation)
        .domain(muleDomainLocation)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(7)
            .withEEVariablesTagCount(9)
            .withEESetVariableTagCount(10)
            .withEESetPayloadTagCount(7)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerConfigCount(1)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(5)
            .build())
        .getConfigContent();

    assertPetApiScaffoldedContent(configContent);
  }

  @Test
  public void testTwoResourceGenerate() throws Exception {
    String apiLocation = "scaffolder/two.raml";

    String s = when()
        .api(apiLocation)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(7)
            .withEEVariablesTagCount(7)
            .withEESetVariableTagCount(7)
            .withEESetPayloadTagCount(7)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerConfigCount(1)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(4)
            .build())
        .getConfigContent();

    assertEquals(2, countOccurences(s, "get:\\pet:two-config"));
    assertEquals(2, countOccurences(s, "post:\\pet:two-config"));
    assertEquals(2, countOccurences(s, "get:\\car:two-config"));
    assertEquals(2, countOccurences(s, "post:\\car:two-config"));
  }

  @Test
  public void testNestedGenerate() throws Exception {
    String apiLocation = "scaffolder/nested.raml";

    String s = when()
        .api(apiLocation)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(7)
            .withEEVariablesTagCount(7)
            .withEESetVariableTagCount(7)
            .withEESetPayloadTagCount(7)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerConfigCount(1)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(5)
            .build())
        .getConfigContent();

    assertEquals(2, countOccurences(s, "get:\\pet:nested-config"));
    assertEquals(2, countOccurences(s, "post:\\pet:nested-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\owner:nested-config"));
    assertEquals(2, countOccurences(s, "get:\\car:nested-config"));
    assertEquals(2, countOccurences(s, "post:\\car:nested-config"));
  }

  @Test
  public void testSimpleGenerationWithRamlInsideAFolder() throws Exception {
    String s = when()
        .api("raml-inside-folder/folder/api.raml")
        .configs("raml-inside-folder/api.xml")
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(1)
            .withHttpResponseStatusCode500Count(1)
            .withHttpHeadersOutboundHeadersDefaultCount(2)
            .withOnErrorPropagateCount(6)
            .withHttpBodyCount(2)
            .withHttpHeadersCount(4)
            .withDWPayloadExpressionCount(1)
            .withHttplListenerConfigCount(1)
            .withHttplListenerCount(1)
            .withLoggerInfoCount(1)
            .withErrorHandlerTagCount(1)
            .build())
        .getConfigContent();

    assertEquals(1, countOccurences(s, "<flow name=\"post:\\oneResource:api-config\">"));
  }

  @Test
  public void testNoNameGenerate() throws Exception {
    String apiLocation = "scaffolder/no-name.raml";
    String s = when()
        .api(apiLocation)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(7)
            .withEEVariablesTagCount(7)
            .withEESetVariableTagCount(7)
            .withEESetPayloadTagCount(7)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerConfigCount(1)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(5)
            .build())
        .getConfigContent();

    assertEquals(1, countOccurences(s, "http:listener-config name=\"no-name-httpListenerConfig\">"));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"no-name-httpListenerConfig\" path=\"/api/*\""));
  }

  @Test
  public void testExampleGenerate() throws Exception {
    String apiLocation = "scaffolder/example.raml";
    String s = when()
        .api(apiLocation)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(9)
            .withEEVariablesTagCount(7)
            .withEESetVariableTagCount(7)
            .withEESetPayloadTagCount(9)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerConfigCount(1)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .build())
        .getConfigContent();

    assertEquals(8, countOccurences(s, "application/json"));
    assertEquals(1, countOccurences(s,
                                    "{\n" +
                                        "  name: \"Bobby\",\n" +
                                        "  food: \"Ice Cream\"\n" +
                                        "}"));
    assertEquals(1, countOccurences(s, "{\n" +
        "  Person: {\n" +
        "    name: \"Underwood\",\n" +
        "    address: \"Juana Manso 999\",\n" +
        "    country: \"Argentina\"\n" +
        "  }\n" +
        "}"));
  }

  @Test
  public void testExampleWithNamespaceReScaffold() throws Exception {
    String apiLocation = "scaffolder/example.raml";
    String s = when()
        .api(apiLocation)
        .configs("scaffolder/example.xml")
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .getConfigContent();
    //assert that generated flow have the ee namespace declaration
    Matcher matcher = Pattern.compile("<flow name=\"get:\\\\pet:example-config\">\\n" +
        "\\s+<ee:transform xmlns:ee=\"http:\\/\\/www\\.mulesoft\\.org\\/schema\\/mule\\/ee\\/core\">").matcher(s);
    assertTrue(s, matcher.find());
  }

  @Test
  public void testExampleGenerateWithRamlType() throws Exception {
    String apiLocation = "scaffolder/example-v10.raml";
    String s = when()
        .api(apiLocation)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .getConfigContent();

    assertEquals(1, countOccurences(s, "{\n" +
        "  name: \"Bobby\",\n" +
        "  food: \"Ice Cream\"\n" +
        "}"));
    assertEquals(1, countOccurences(s, "{\n" +
        "  Person: {\n" +
        "    name: \"Underwood\",\n" +
        "    address: \"Juana Manso 999\",\n" +
        "    country: \"Argentina\"\n" +
        "  }\n" +
        "}"));
    assertEquals(1, countOccurences(s, "{\n" +
        "  title: \"In Cold Blood\",\n" +
        "  author: \"Truman Capote\",\n" +
        "  year: 1966\n" +
        "}"));
  }

  @Test
  public void testExampleGenerateForCE() throws Exception {
    String apiLocation = "scaffolder/example-v10.raml";
    String name = fileNameWhithOutExtension(apiLocation);

    String s = when()
        .api(apiLocation)
        .runtimeEdition(RuntimeEdition.CE)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .getConfigContent();

    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message=\"get:\\pet:" + name + "-config\" />"));
    assertEquals(0, countOccurences(s, "{\n" +
        "  name: \"Bobby\",\n" +
        "  food: \"Ice Cream\"\n" +
        "}"));

    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message=\"get:\\person:" + name + "-config\" />"));
    assertEquals(0, countOccurences(s, "{\n" +
        "  Person: {\n" +
        "    name: \"Underwood\",\n" +
        "    address: \"Juana Manso 999\",\n" +
        "    country: \"Argentina\"\n" +
        "  }\n" +
        "}"));

    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message=\"get:\\books:" + name + "-config\" />"));
    assertEquals(0, countOccurences(s, "{\n" +
        "  title: \"In Cold Blood\",\n" +
        "  author: \"Truman Capote\",\n" +
        "  year: 1966\n" +
        "}"));
  }

  @Test
  public void doubleRootRaml() throws Exception {
    // In the new Scaffolder API you can't scaffold more than one ApiSpecification at a time.
    // If you want to scaffold more than one ApiSpec, you have to call the scaffolder N times.
    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    ParserService parserService = new ParserService();
    ParseResult firstRamlParsingResult = parserService.parse(ApiReference.create("double-root-raml/simple.raml"));
    ParseResult secondRamlParsingResult = parserService.parse(ApiReference.create("double-root-raml/two.raml"));

    assertTrue(firstRamlParsingResult.success());
    assertTrue(secondRamlParsingResult.success());

    ScaffoldingConfiguration firstScaffoldingConfiguration =
        new ScaffoldingConfiguration.Builder().withApi(firstRamlParsingResult.get()).build();
    ScaffoldingConfiguration secondScaffoldingConfiguration =
        new ScaffoldingConfiguration.Builder().withApi(secondRamlParsingResult.get()).build();

    ScaffoldingResult result = mainAppScaffolder.run(firstScaffoldingConfiguration);
    assertTrue(result.isSuccess());
    assertTrue(result.getGeneratedConfigs().size() == 1);

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    XmlOccurrencesAsserter xmlOccurrencesAsserter = new XmlOccurrencesAsserterBuilder()
        .withHttpResponseStatusCode200Count(2)
        .withHttpResponseStatusCode500Count(2)
        .withHttpHeadersOutboundHeadersDefaultCount(4)
        .withOnErrorPropagateCount(7)
        .withEEMessageTagCount(7)
        .withEEVariablesTagCount(7)
        .withEESetVariableTagCount(7)
        .withEESetPayloadTagCount(7)
        .withHttpBodyCount(4)
        .withHttpHeadersCount(8)
        .withDWPayloadExpressionCount(2)
        .withHttplListenerConfigCount(1)
        .withHttplListenerCount(2)
        .withApikitConsoleCount(1)
        .withLoggerInfoCount(2)
        .build();
    xmlOccurrencesAsserter.assertOccurrences(s);
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));

    result = mainAppScaffolder.run(secondScaffoldingConfiguration);
    assertTrue(result.isSuccess());
    assertTrue(result.getGeneratedConfigs().size() == 1);

    String s2 = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    xmlOccurrencesAsserter = new XmlOccurrencesAsserterBuilder()
        .withHttpResponseStatusCode200Count(2)
        .withHttpResponseStatusCode500Count(2)
        .withHttpHeadersOutboundHeadersDefaultCount(4)
        .withOnErrorPropagateCount(7)
        .withEEMessageTagCount(7)
        .withEEVariablesTagCount(7)
        .withEESetVariableTagCount(7)
        .withEESetPayloadTagCount(7)
        .withHttpBodyCount(4)
        .withHttpHeadersCount(8)
        .withDWPayloadExpressionCount(2)
        .withHttplListenerConfigCount(1)
        .withHttplListenerCount(2)
        .withApikitConsoleCount(1)
        .withLoggerInfoCount(4)
        .build();
    xmlOccurrencesAsserter.assertOccurrences(s2);
    assertEquals(2, countOccurences(s2, "get:\\pet:two-config"));
    assertEquals(2, countOccurences(s2, "post:\\pet:two-config"));
    assertEquals(2, countOccurences(s2, "get:\\car:two-config"));
    assertEquals(2, countOccurences(s2, "post:\\car:two-config"));
  }

  @Test
  public void testGenerateWithExchangeModules() throws Exception {
    String apiLocation = "scaffolder-exchange/api.raml";
    String s = when()
        .api(apiLocation)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(10)
            .withEEVariablesTagCount(7)
            .withEESetVariableTagCount(7)
            .withEESetPayloadTagCount(10)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerConfigCount(1)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .build())
        .getConfigContent();

    assertEquals(1, countOccurences(s, "get:\\resource1:api-config"));
    assertEquals(1, countOccurences(s, "get:\\resource2:api-config"));
    assertEquals(1, countOccurences(s, "get:\\resource3:api-config"));
  }

  @Test
  public void testGenerateWithRecursiveApi() throws Exception {
    String s = when()
        .api("scaffolder/api-with-resource-type.raml")
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withEEMessageTagCount(7)
            .withEEVariablesTagCount(7)
            .withEESetVariableTagCount(7)
            .withEESetPayloadTagCount(7)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerConfigCount(1)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(1)
            .build())
        .getConfigContent();

    assertEquals(2, countOccurences(s, "post:\\v4\\items:application\\json:api-with-resource-type-config"));
  }

  @Test
  public void testGenerateFromTwoApis() throws Exception {
    final String testFolder = "scaffolder-from-two-apis/simple/";

    testScaffoldTwoApis(testFolder, new ArrayList<>(), null);
  }

  @Test
  public void testGenerateFromTwoApisWithDomain() throws Exception {
    final String testFolder = "scaffolder-from-two-apis/with-domain/";
    MuleDomain muleDomain = MuleDomain.fromInputStream(getResourceAsStream(testFolder + "domains/mule-domain-config.xml"));

    testScaffoldTwoApis(testFolder, new ArrayList<>(), muleDomain);
  }

  @Test
  public void testGenerateFromTwoApisWithExistentConfig() throws Exception {
    final String testFolder = "scaffolder-from-two-apis/with-existent-config/";
    MuleConfig muleConfig = MuleConfigBuilder.fromStream(getResourceAsStream(testFolder + "api.xml"));
    List<MuleConfig> muleConfigs = Lists.newArrayList(muleConfig);

    testScaffoldTwoApis(testFolder, muleConfigs, null);
  }

  @Test
  public void testScaffolderRouterWithinTry() throws Exception {
    when()
        .api("scaffolder-router-within-try/simple.raml")
        .configs("scaffolder-router-within-try/simple.xml")
        .then()
        .assertSuccess();
  }

  private void testScaffoldTwoApis(String testFolder, List<MuleConfig> existingMuleConfigs, MuleDomain domainFile)
      throws Exception {
    // The new Scaffolder API doesn't support scaffolding more than one ApiSpecification at a time.
    // If you want to scaffold more than one ApiSpec you have to call the Scaffolder N times.
    final String basePath = testFolder + "src/main/resources/api/";
    final String api1 = basePath + "api1/api.raml";
    final String api2 = basePath + "api2/api.raml";
    XMLUnit.setIgnoreWhitespace(true);

    MuleConfig generatedMuleConfig = when()
        .api(api1)
        .configs(existingMuleConfigs)
        .domain(domainFile)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .getGeneratedConfigs().get(0);

    String firstGeneratedMuleConfigContent = APIKitTools.readContents(generatedMuleConfig.getContent());
    Diff firstMuleConfigDiff = XMLUnit.compareXML(firstGeneratedMuleConfigContent,
                                                  APIKitTools.readContents(getResourceAsStream(testFolder + "api.xml")));

    existingMuleConfigs.add(generatedMuleConfig);
    String secondGeneratedMuleConfigContent = when()
        .api(api2)
        .configs(existingMuleConfigs)
        .domain(domainFile)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .getConfigContent();

    Diff secondMuleConfigDiff = XMLUnit.compareXML(secondGeneratedMuleConfigContent,
                                                   APIKitTools.readContents(getResourceAsStream(testFolder + "api-2.xml")));
    assertTrue(firstMuleConfigDiff.identical());
    assertTrue(secondMuleConfigDiff.identical());
  }


  private void simpleGenerateForCE(final String apiPath) throws Exception {
    final String name = fileNameWhithOutExtension(apiPath);

    String configContent = when()
        .api(apiPath)
        .runtimeEdition(RuntimeEdition.CE)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(2)
            .withHttpResponseStatusCode500Count(2)
            .withHttpHeadersOutboundHeadersDefaultCount(4)
            .withOnErrorPropagateCount(7)
            .withHttpBodyCount(4)
            .withHttpHeadersCount(8)
            .withDWPayloadExpressionCount(2)
            .withHttplListenerConfigCount(1)
            .withHttplListenerCount(2)
            .withApikitConsoleCount(1)
            .withLoggerInfoCount(5)
            .withCustomAssertionCount("http:listener-config name=\"simple", 1)
            .build())
        .getConfigContent();

    assertEquals(1, countOccurences(configContent, "http:listener-config name=\"simple"));
    assertEquals(1, countOccurences(configContent, "http:listener-connection host=\"0.0.0.0\" port=\"8081\""));
    assertEquals(0, countOccurences(configContent, "<ee:"));
    assertEquals(7,
                 countOccurences(configContent,
                                 "<set-variable variableName=\"outboundHeaders\" value=\"#[{'Content-Type':'application/json'}]\" />"));
    assertEquals(7, countOccurences(configContent, "<set-variable variableName=\"httpStatus\""));
    assertEquals(2,
                 countOccurences(configContent,
                                 "<set-variable value=\"#[attributes.uriParams.'name']\" variableName=\"name\" />"));
    assertEquals(1,
                 countOccurences(configContent,
                                 "<set-variable value=\"#[attributes.uriParams.'owner']\" variableName=\"owner\""));
    assertEquals(2, countOccurences(configContent, "get:\\:" + name + "-config"));
    assertEquals(2, countOccurences(configContent, "get:\\pet:" + name + "-config"));
    assertEquals(2, countOccurences(configContent, "get:\\pet\\v1:" + name + "-config"));
    assertEquals(0, countOccurences(configContent, "#[NullPayload.getInstance()]"));
    assertEquals(0, countOccurences(configContent, "#[null]"));
    assertEquals(0,
                 countOccurences(configContent,
                                 "expression-component>mel:flowVars['variables.outboundHeaders default {}'].put('Content-Type', 'application/json')</expression-component>"));
    assertEquals(0,
                 countOccurences(configContent,
                                 "set-variable variableName=\"variables.outboundHeaders default {}\" value=\"#[mel:new java.util.HashMap()]\" />"));
  }

  private void simpleGenerate(final String apiPath, boolean hideConsole) throws Exception {
    final String name = fileNameWhithOutExtension(apiPath);
    String s = when()
        .api(apiPath)
        .showConsole(!hideConsole)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .assertXmlOccurrences(new XmlOccurrencesAsserterBuilder()
            .withHttpResponseStatusCode200Count(hideConsole ? 1 : 2)
            .withHttpResponseStatusCode500Count(hideConsole ? 1 : 2)
            .withHttpHeadersOutboundHeadersDefaultCount(hideConsole ? 2 : 4)
            .withOnErrorPropagateCount(hideConsole ? 6 : 7)
            .withEEMessageTagCount(hideConsole ? 6 : 7)
            .withEEVariablesTagCount(hideConsole ? 8 : 9)
            .withEESetVariableTagCount(hideConsole ? 9 : 10)
            .withEESetPayloadTagCount(hideConsole ? 6 : 7)
            .withHttpBodyCount(hideConsole ? 2 : 4)
            .withHttpHeadersCount(hideConsole ? 4 : 8)
            .withDWPayloadExpressionCount(hideConsole ? 1 : 2)
            .withHttplListenerConfigCount(1)
            .withHttplListenerCount(hideConsole ? 1 : 2)
            .withApikitConsoleCount(hideConsole ? 0 : 1)
            .withLoggerInfoCount(5)
            .build())
        .getConfigContent();

    assertEquals(1, countOccurences(s, "http:listener-config name=\"simple"));
    assertEquals(1, countOccurences(s, "http:listener-connection host=\"0.0.0.0\" port=\"8081\""));
    assertEquals(2, countOccurences(s, "<ee:set-variable variableName=\"name\">attributes.uriParams.'name'</ee:set-variable>"));
    assertEquals(1, countOccurences(s, "<ee:set-variable variableName=\"owner\">attributes.uriParams.'owner'</ee:set-variable>"));
    assertEquals(2, countOccurences(s, "get:\\:" + name + "-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:" + name + "-config"));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
    assertEquals(0, countOccurences(s, "#[null]"));
    assertEquals(0,
                 countOccurences(s,
                                 "expression-component>mel:flowVars['variables.outboundHeaders default {}'].put('Content-Type', 'application/json')</expression-component>"));
    assertEquals(0,
                 countOccurences(s,
                                 "set-variable variableName=\"variables.outboundHeaders default {}\" value=\"#[mel:new java.util.HashMap()]\" />"));
  }

  @Test
  public void twoUriParams() throws Exception {
    String s = when()
        .api("scaffolder/twoUriParams.raml")
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .getConfigContent();

    assertEquals(1, countOccurences(s, "<ee:set-variable variableName=\"id\">attributes.uriParams.'id'</ee:set-variable>"));
    assertEquals(1,
                 countOccurences(s,
                                 "<ee:set-variable variableName=\"mediaTypeExtension\">attributes.uriParams.'mediaTypeExtension'</ee:set-variable>"));
  }

  @Test
  public void scaffoldEmptyAPI() throws Exception {
    String apiLocation = "scaffolder/without-resources.raml";

    String configContent = when()
        .api(apiLocation)
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .getConfigContent();

    assertEquals("Files are different",
                 APIKitTools.readContents(getResourceAsStream("scaffolder/expected-result-without-resources.xml"))
                     .replaceAll("api=(.*)raml\"", "api=\"\"").replaceAll("\\s+", ""),
                 configContent.replaceAll("api=(.*)raml\"", "api=\"\"").replaceAll("\\s+", ""));
  }

  @Test
  public void multipleContentTypesWithoutSchemaDefinition() throws Exception {
    String configContent = when()
        .api("scaffolder/body-with-mime-types-without-schema/testing-api.raml")
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .getConfigContent();

    assertEquals(APIKitTools.readContents(getResourceAsStream("scaffolder/body-with-mime-types-without-schema/output.xml"))
        .replaceAll("\\s+", ""), configContent.replaceAll("\\s+", ""));
  }

  @Test
  public void scaffoldApiWithResourcesIncludingColon() throws IOException {
    String configContent = when().api("scaffolder/resources-including-colon-api.raml")
        .then()
        .assertSuccess()
        .assertConfigsSize(1)
        .getConfigContent();
    assertEquals(2, countOccurences(configContent, "get:\\resources\\(id)%3Alist:resources-including-colon-api"));
    assertEquals(2,
                 countOccurences(configContent, "post:\\resources\\(id)%3Alist:application\\xml:resources-including-colon-api"));
    assertEquals(2, countOccurences(configContent, "get:\\resources\\%3Ainit:resources-including-colon-api"));
    assertEquals(2, countOccurences(configContent, "post:\\resources\\%3Ainit:application\\xml:resources-including-colon-api"));
    assertEquals(2, countOccurences(configContent, "get:\\(category)%3A(categoryId)\\resources:resources-including-colon-api"));
    assertEquals(2,
                 countOccurences(configContent,
                                 "post:\\(category)%3A(categoryId)\\resources:application\\xml:resources-including-colon-api"));
  }

  private void assertPetApiScaffoldedContent(String content) {
    assertEquals(2, countOccurences(content, "get:\\:simple-config"));
    assertEquals(2, countOccurences(content, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(content, "get:\\pet\\v1:simple-config"));
  }

}
