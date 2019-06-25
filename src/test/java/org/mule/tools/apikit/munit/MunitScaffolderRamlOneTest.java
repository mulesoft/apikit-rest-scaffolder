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
import org.mule.tools.apikit.model.MuleConfig;

import java.io.File;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class MunitScaffolderRamlOneTest extends AbstractMunitScaffolderTest {

  public static final String RAML_ONE = "raml-1";
  public static final String RAML_ONE_FOLDER = "scaffolder" + File.separator + RAML_ONE;

  private MuleConfig simpleGenerationRamlOne(String name) {
    return simpleGeneration(name, RAML_ONE_FOLDER, false).getGeneratedConfigs().get(0);
  }

  @Test
  public void testHeadersGenerate() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("headers");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedContent =
        "<http:headers>#[{\"Accept\":\"*/*\",\"X-waiting-period\":\"34\",\"Content-Type\":\"application/x-www-form-urlencoded\"}]</http:headers>";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedContent));
  }

  @Test
  public void testMultiplesExamplesResponses() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("multiple-examples-responses");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedContent =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;name&quot;:&quot;Snowball&quot;,&quot;nickname&quot;:&quot;Snow&quot;}')]\"";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedContent));
  }

  @Test
  public void testTypesWithExamplesForResponsesAndRequests() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("types-with-examples");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpressionEqualToJson =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;name&quot;:&quot;Acme&quot;}')]\"";
    String expectedExpressionEqualToOk =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('&quot;OK&quot;')]\"";
    String expectedSetPayload =
        "<set-payload value=\"#['{&quot;name&quot;:&quot;Doe Enterprise&quot;,&quot;value&quot;:&quot;Silver&quot;}']\" />";
    assertTrue(generatedContent.contains(expectedExpressionEqualToJson));
    assertTrue(generatedContent.contains(expectedExpressionEqualToOk));
    assertTrue(generatedContent.contains(expectedSetPayload));
  }

  @Test
  public void testDatatypesDefinedInTypesSection() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("datatypes");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;name&quot;:{&quot;firstname&quot;:&quot;John&quot;,&quot;lastname&quot;:&quot;Silver&quot;},&quot;age&quot;:33}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
  }

  @Test
  public void testMultipleExamplesQueryParameters() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("multiple-examples-query-parameters");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedQueryParams = "<http:query-params>#[{\"qty\":\"20\"}]</http:query-params>";
    assertTrue(generatedContent.contains(expectedQueryParams));
  }

  @Test
  public void testMultipleExamplesRequests() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("multiple-examples-requests");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedSetPayload =
        "<set-payload value=\"#['{&quot;firstName&quot;:&quot;Bruce&quot;,&quot;lastName&quot;:&quot;Banner&quot;}']\" />";
    assertTrue(generatedContent.contains(expectedSetPayload));
  }

  @Test
  public void testJsonSchema() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("json-schema");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpressionJsonName =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;name&quot;:&quot;&quot;}')]\"";
    String expectedExpressionJsonId =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;id&quot;:&quot;&quot;,&quot;value&quot;:&quot;&quot;}')]\"";
    String expectedSetPayload = "<set-payload value=\"#['{&quot;id&quot;:&quot;&quot;,&quot;value&quot;:&quot;&quot;}']\" />";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedExpressionJsonName));
    assertEquals(2, TestUtils.countOccurrences(generatedContent, expectedExpressionJsonId));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedSetPayload));
  }

  @Test
  public void testTypesWithJsonSchema() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("types-json-schema");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;id&quot;:&quot;&quot;,&quot;value&quot;:&quot;&quot;}')]\"";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedExpression));
  }

  @Test
  public void testIncludeExternalTypes() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("include-types");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;firstname&quot;:&quot;John&quot;,&quot;lastname&quot;:&quot;Silver&quot;,&quot;age&quot;:33}')]\"";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedExpression));
  }

  @Test
  public void testIncludeScalarExample() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("include-scalar-example");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;firstname&quot;:&quot;John&quot;,&quot;lastname&quot;:&quot;Smith&quot;,&quot;age&quot;:25}')]\"";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedExpression));
  }

  @Test
  public void testTraits() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("traits");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHttpHeaders =
        "<http:headers>#[{\"access_token\":\"5757gh76\",\"Accept\":\"application/json\"}]</http:headers>";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedHttpHeaders));
  }

  @Test
  public void testIncludeJsonSchema() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("include-json-schema");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;songId&quot;:&quot;&quot;,&quot;songTitle&quot;:&quot;&quot;,&quot;albumId&quot;:&quot;&quot;}')]\"";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedExpression));
  }

  @Test
  public void testResourceTypes() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("resource-types");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHttpQueryParams =
        "<http:query-params>#[{\"digest_all_fields\":\"fallback\",\"title\":\"resource\"}]</http:query-params>";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedHttpQueryParams));
  }

  @Test
  public void testResourcesAndTraits() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("traits-and-resource-types");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHttpQueryParams =
        "<http:query-params>#[{\"access_token\":\"tokenExample\",\"numPages\":\"10\",\"digest_all_fields\":\"fallbackParam\",\"title\":\"queryParam\"}]</http:query-params>";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedHttpQueryParams));
  }

  @Test
  public void testIncludeResourceTypesAndTraits() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("include-resource-types-and-traits");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHttpQueryParams = "<http:query-params>#[{\"start\":\"25\"}]</http:query-params>";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedHttpQueryParams));
  }

  @Test
  public void testFragmentResourceTypes() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("fragment-resource-type");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHttpHeaders = "<http:headers>#[{\"Accept\":\"application/json\",\"Location\":\"22\"}]</http:headers>";
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('&quot;44W&quot;')]\"";
    assertTrue(generatedContent.contains(expectedHttpHeaders));
    assertTrue(generatedContent.contains(expectedExpression));
  }

  @Test
  public void testFragmentDataType() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("fragment-data-type");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;firstName&quot;:&quot;John&quot;,&quot;lastName&quot;:&quot;Smith&quot;}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
  }

  @Test
  public void testFragmentTrait() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("fragment-trait");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHttpQueryParam = "<http:query-params>#[{\"start\":\"25\"}]</http:query-params>";
    assertTrue(generatedContent.contains(expectedHttpQueryParam));
  }

  @Test
  public void testFragmentExtension() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("fragment-extension");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;id&quot;:&quot;JohnS&quot;}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
  }

  @Test
  public void testFragmentNamedExample() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("fragment-named-example");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('&quot;John&quot;')]\"";
    Assert.assertTrue(generatedContent.contains(expectedExpression));
  }

  @Test
  public void testNoRequireValueQueryParameters() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("no-required-query-parameter");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHttpQueryParams = "<http:query-params>#[{\"airline\":\"all\"}]</http:query-params>";
    assertTrue(generatedContent.contains(expectedHttpQueryParams));
  }

  @Test
  public void testNoRequireValueHeads() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("no-required-headers");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHttpHeaders = "<http:headers>#[{\"status-code\":\"XXX\",\"Accept\":\"*/*\"}]</http:headers>";
    assertTrue(generatedContent.contains(expectedHttpHeaders));
  }

  @Test
  public void testNilType() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("nil-type");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;name&quot;:&quot;Fred&quot;,&quot;comment&quot;:null}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
  }

  @Test
  public void testMultipleUriParameters() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("multiple-uri-parameters");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedSetVariableTicketShow =
        "<set-variable variableName=\"ticketShow\" value=\"#['The Who']\" doc:name=\"ticketShow\" />";
    String expectedSetVariableTicketDate =
        "<set-variable variableName=\"ticketDate\" value=\"#['1/1/2015']\" doc:name=\"ticketDate\" />";
    String expectedPath = "path=\"#['/$(vars.ticketShow)/$(vars.ticketDate)']\"";
    assertTrue(generatedContent.contains(expectedSetVariableTicketShow));
    assertTrue(generatedContent.contains(expectedSetVariableTicketDate));
    assertTrue(generatedContent.contains(expectedPath));
  }

  @Test
  public void testExampleInTypesSection() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("examples-in-types");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;firstName&quot;:&quot;John&quot;,&quot;lastName&quot;:&quot;Doe&quot;}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
  }

  /**
   * The following tests are only to validate that the Scaffolder works correctly with the new features of RAML 1.0, we don't make
   * usage of them
   */

  @Test
  public void testOverlays() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("overlays");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;name&quot;:&quot;Bestiario&quot;}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
  }

  @Test
  public void testLibraries() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("libraries");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;name&quot;:&quot;myfile&quot;,&quot;length&quot;:4000}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
  }

  @Test
  public void testAnnotations() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("annotations");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('&quot;OK&quot;')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
  }

  @Test
  public void testSecuritySchemes() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("security-schemes");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression = "expression=\"#[attributes.statusCode]\" is=\"#[MunitTools::equalTo(200)]\"";
    assertTrue(generatedContent.contains(expectedExpression));
  }

  @Test
  public void testComplexApi() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("complex-api");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String munitTestTag = "<munit:test";
    String assertThatTag = "<munit-tools:assert-that";
    assertEquals(216, TestUtils.countOccurrences(generatedContent, munitTestTag));
    assertEquals(432, TestUtils.countOccurrences(generatedContent, assertThatTag));
  }

  @Test
  public void testExchangeApi() throws Exception {
    MuleConfig muleConfig = simpleGenerationRamlOne("exchange-api");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHttpHeaders =
        "<http:headers>#[{\"Accept\":\"application/json\",\"client_secret\":\"AAAA\",\"client_id\":\"BBBB\"}]</http:headers>";
    String expcetedExpression = "expression=\"#[attributes.statusCode]\" is=\"#[MunitTools::equalTo(200)]\"";
    assertTrue(generatedContent.contains(expectedHttpHeaders));
    assertTrue(generatedContent.contains(expcetedExpression));
  }
}
