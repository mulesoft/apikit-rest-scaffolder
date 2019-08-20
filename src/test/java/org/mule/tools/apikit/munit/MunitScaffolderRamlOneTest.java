/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.munit;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mule.tools.apikit.TestUtils;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.MuleConfig;

import java.io.File;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mule.tools.apikit.TestUtils.ENABLE_FLOW_SOURCES_TEMPLATE;
import static org.mule.tools.apikit.TestUtils.generateMainFlowNameForApi;

public class MunitScaffolderRamlOneTest extends AbstractMunitScaffolderTest {

  public static final String RAML_ONE = "raml-1";
  public static final String RAML_ONE_FOLDER = "scaffolder" + File.separator + RAML_ONE;

  private MuleConfig simpleGenerationRamlOne(String name) throws Exception {
    return simpleGeneration(name, RAML_ONE_FOLDER, false).getGeneratedConfigs().get(0);
  }

  @Test
  public void testHeadersGenerate() throws Exception {
    String apiSpecFile = "headers";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedContent =
        "<http:headers>#[{\"Accept\":\"*/*\",\"X-waiting-period\":\"34\",\"Content-Type\":\"application/x-www-form-urlencoded\"}]</http:headers>";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedContent));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testMultiplesExamplesResponses() throws Exception {
    String apiSpecFile = "multiple-examples-responses";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedContent =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;name&quot;:&quot;Snowball&quot;,&quot;nickname&quot;:&quot;Snow&quot;}')]\"";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedContent));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testTypesWithExamplesForResponsesAndRequests() throws Exception {
    String apiSpecFile = "types-with-examples";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedExpressionEqualToJson =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;name&quot;:&quot;Acme&quot;}')]\"";
    String expectedExpressionEqualToOk =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('&quot;OK&quot;')]\"";
    String expectedSetPayload =
        "<set-payload value=\"#['{&quot;name&quot;:&quot;Doe Enterprise&quot;,&quot;value&quot;:&quot;Silver&quot;}']\" />";
    assertTrue(generatedContent.contains(expectedExpressionEqualToJson));
    assertTrue(generatedContent.contains(expectedExpressionEqualToOk));
    assertTrue(generatedContent.contains(expectedSetPayload));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testDatatypesDefinedInTypesSection() throws Exception {
    String apiSpecFile = "datatypes";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;name&quot;:{&quot;firstname&quot;:&quot;John&quot;,&quot;lastname&quot;:&quot;Silver&quot;},&quot;age&quot;:33}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testMultipleExamplesQueryParameters() throws Exception {
    String apiSpecFile = "multiple-examples-query-parameters";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedQueryParams = "<http:query-params>#[{\"qty\":\"20\"}]</http:query-params>";
    assertTrue(generatedContent.contains(expectedQueryParams));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testMultipleExamplesRequests() throws Exception {
    String apiSpecFile = "multiple-examples-requests";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedSetPayload =
        "<set-payload value=\"#['{&quot;firstName&quot;:&quot;Bruce&quot;,&quot;lastName&quot;:&quot;Banner&quot;}']\" />";
    assertTrue(generatedContent.contains(expectedSetPayload));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testJsonSchema() throws Exception {
    String apiSpecFile = "json-schema";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedExpressionJsonName =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;name&quot;:&quot;&quot;}')]\"";
    String expectedExpressionJsonId =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;id&quot;:&quot;&quot;,&quot;value&quot;:&quot;&quot;}')]\"";
    String expectedSetPayload = "<set-payload value=\"#['{&quot;id&quot;:&quot;&quot;,&quot;value&quot;:&quot;&quot;}']\" />";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedExpressionJsonName));
    assertEquals(2, TestUtils.countOccurrences(generatedContent, expectedExpressionJsonId));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedSetPayload));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testTypesWithJsonSchema() throws Exception {
    String apiSpecFile = "types-json-schema";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;id&quot;:&quot;&quot;,&quot;value&quot;:&quot;&quot;}')]\"";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testIncludeExternalTypes() throws Exception {
    String apiSpecFile = "include-types";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;firstname&quot;:&quot;John&quot;,&quot;lastname&quot;:&quot;Silver&quot;,&quot;age&quot;:33}')]\"";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testIncludeScalarExample() throws Exception {
    String apiSpecFile = "include-scalar-example";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;firstname&quot;:&quot;John&quot;,&quot;lastname&quot;:&quot;Smith&quot;,&quot;age&quot;:25}')]\"";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testTraits() throws Exception {
    String apiSpecFile = "traits";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedHttpHeaders =
        "<http:headers>#[{\"access_token\":\"5757gh76\",\"Accept\":\"application/json\"}]</http:headers>";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedHttpHeaders));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testIncludeJsonSchema() throws Exception {
    String apiSpecFile = "include-json-schema";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;songId&quot;:&quot;&quot;,&quot;songTitle&quot;:&quot;&quot;,&quot;albumId&quot;:&quot;&quot;}')]\"";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testResourceTypes() throws Exception {
    String apiSpecFile = "resource-types";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedHttpQueryParams =
        "<http:query-params>#[{\"digest_all_fields\":\"fallback\",\"title\":\"resource\"}]</http:query-params>";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedHttpQueryParams));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testResourcesAndTraits() throws Exception {
    String apiSpecFile = "traits-and-resource-types";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedHttpQueryParams =
        "<http:query-params>#[{\"access_token\":\"tokenExample\",\"numPages\":\"10\",\"digest_all_fields\":\"fallbackParam\",\"title\":\"queryParam\"}]</http:query-params>";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedHttpQueryParams));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testIncludeResourceTypesAndTraits() throws Exception {
    String apiSpecFile = "include-resource-types-and-traits";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedHttpQueryParams = "<http:query-params>#[{\"start\":\"25\"}]</http:query-params>";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedHttpQueryParams));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testFragmentResourceTypes() throws Exception {
    String apiSpecFile = "fragment-resource-type";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedHttpHeaders = "<http:headers>#[{\"Accept\":\"application/json\",\"Location\":\"22\"}]</http:headers>";
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('&quot;44W&quot;')]\"";
    assertTrue(generatedContent.contains(expectedHttpHeaders));
    assertTrue(generatedContent.contains(expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testFragmentDataType() throws Exception {
    String apiSpecFile = "fragment-data-type";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;firstName&quot;:&quot;John&quot;,&quot;lastName&quot;:&quot;Smith&quot;}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testFragmentTrait() throws Exception {
    String apiSpecFile = "fragment-trait";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedHttpQueryParam = "<http:query-params>#[{\"start\":\"25\"}]</http:query-params>";
    assertTrue(generatedContent.contains(expectedHttpQueryParam));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testFragmentExtension() throws Exception {
    String apiSpecFile = "fragment-extension";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;id&quot;:&quot;JohnS&quot;}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testFragmentNamedExample() throws Exception {
    String apiSpecFile = "fragment-named-example";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('&quot;John&quot;')]\"";
    Assert.assertTrue(generatedContent.contains(expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testNoRequireValueQueryParameters() throws Exception {
    String apiSpecFile = "no-required-query-parameter";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedHttpQueryParams = "<http:query-params>#[{\"airline\":\"all\"}]</http:query-params>";
    assertTrue(generatedContent.contains(expectedHttpQueryParams));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testNoRequireValueHeads() throws Exception {
    String apiSpecFile = "no-required-headers";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedHttpHeaders = "<http:headers>#[{\"status-code\":\"XXX\",\"Accept\":\"*/*\"}]</http:headers>";
    assertTrue(generatedContent.contains(expectedHttpHeaders));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testNilType() throws Exception {
    String apiSpecFile = "nil-type";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;name&quot;:&quot;Fred&quot;,&quot;comment&quot;:null}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testMultipleUriParameters() throws Exception {
    String apiSpecFile = "multiple-uri-parameters";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedSetVariableTicketShow =
        "<set-variable variableName=\"ticketShow\" value=\"#['The Who']\" doc:name=\"ticketShow\" />";
    String expectedSetVariableTicketDate =
        "<set-variable variableName=\"ticketDate\" value=\"#['1/1/2015']\" doc:name=\"ticketDate\" />";
    String expectedPath = "path=\"#['/$(vars.ticketShow)/$(vars.ticketDate)']\"";
    assertTrue(generatedContent.contains(expectedSetVariableTicketShow));
    assertTrue(generatedContent.contains(expectedSetVariableTicketDate));
    assertTrue(generatedContent.contains(expectedPath));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testExampleInTypesSection() throws Exception {
    String apiSpecFile = "examples-in-types";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;firstName&quot;:&quot;John&quot;,&quot;lastName&quot;:&quot;Doe&quot;}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  /**
   * The following tests are only to validate that the Scaffolder works correctly with the new features of RAML 1.0, we don't make
   * usage of them
   */

  @Test
  public void testOverlays() throws Exception {
    String apiSpecFile = "overlays";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;name&quot;:&quot;Bestiario&quot;}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testLibraries() throws Exception {
    String apiSpecFile = "libraries";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = APIKitTools.readContents(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;name&quot;:&quot;myfile&quot;,&quot;length&quot;:4000}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testAnnotations() throws Exception {
    String apiSpecFile = "annotations";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('&quot;OK&quot;')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testSecuritySchemes() throws Exception {
    String apiSpecFile = "security-schemes";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression = "expression=\"#[attributes.statusCode]\" is=\"#[MunitTools::equalTo(200)]\"";
    assertTrue(generatedContent.contains(expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testComplexApi() throws Exception {
    String apiSpecFile = "complex-api";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String munitTestTag = "<munit:test";
    String assertThatTag = "<munit-tools:assert-that";
    assertEquals(216, TestUtils.countOccurrences(generatedContent, munitTestTag));
    assertEquals(432, TestUtils.countOccurrences(generatedContent, assertThatTag));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testExchangeApi() throws Exception {
    String apiSpecFile = "exchange-api";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRamlOne(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHttpHeaders =
        "<http:headers>#[{\"Accept\":\"application/json\",\"client_secret\":\"AAAA\",\"client_id\":\"BBBB\"}]</http:headers>";
    String expcetedExpression = "expression=\"#[attributes.statusCode]\" is=\"#[MunitTools::equalTo(200)]\"";
    assertTrue(generatedContent.contains(expectedHttpHeaders));
    assertTrue(generatedContent.contains(expcetedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }
}
