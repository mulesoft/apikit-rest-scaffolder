/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.munit;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mule.tools.apikit.TestUtils;
import org.mule.tools.apikit.model.MuleConfig;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.TestUtils.ENABLE_FLOW_SOURCES_TEMPLATE;
import static org.mule.tools.apikit.TestUtils.generateMainFlowNameForApi;

public class MunitScaffolderRamlTest extends AbstractMunitScaffolderTest {

  private MuleConfig simpleGenerationRaml(String name) throws Exception {
    return simpleGeneration(name, "scaffolder", false).getGeneratedConfigs().get(0);
  }

  @Test
  public void testSimpleGenerate() throws Exception {
    String apiSpecFile = "simple";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml("simple");
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedEndpointJson = "name=\"get:\\pet:simple-config-200-application\\json-FlowTest\"";
    String expectedEndpointXml = "name=\"get:\\pet:simple-config-200-text\\xml-FlowTest\"";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedEndpointJson));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedEndpointXml));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testTraitsGenerate() throws Exception {
    String apiSpecFile = "traits";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedGetStatusCode200 = "get:\\pet:traits-config-200-application\\json-FlowTest";
    String expectedGetStatusCode404 = "get:\\pet:traits-config-404-application\\json-FlowTest";
    String expectedExpressionStatusCode200 =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;itemId&quot;:1,&quot;itemName&quot;:&quot;aName&quot;}')]\"";
    String expectedExpressionStatusCode404 =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;ll&quot;:&quot;not found&quot;}')]\"";

    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedGetStatusCode200));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedGetStatusCode404));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedExpressionStatusCode200));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedExpressionStatusCode404));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testContentTypeGenerate() throws Exception {
    String apiSpecFile = "content-type";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHeaders =
        "<http:headers>#[{\"Accept\":\"application/json\",\"Content-Type\":\"application/json\"}]</http:headers>";
    String expectedHeaders2 = "<http:headers>#[{\"Accept\":\"text/xml\",\"Content-Type\":\"application/json\"}]</http:headers>";
    String expectedHeaders3 = "<http:headers>#[{\"Accept\":\"application/json\",\"Content-Type\":\"text/xml\"}]</http:headers>";
    String expectedHeaders4 = "<http:headers>#[{\"Accept\":\"text/xml\",\"Content-Type\":\"text/xml\"}]</http:headers>";

    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedHeaders));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedHeaders2));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedHeaders3));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedHeaders4));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testHeadersGenerate() throws Exception {
    String apiSpecFile = "headers";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHeaders =
        "<http:headers>#[{\"Accept\":\"*/*\",\"X-waiting-period\":\"34\",\"Content-Type\":\"application/x-www-form-urlencoded\"}]</http:headers>";
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedHeaders));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testTwoResourceGenerate() throws Exception {
    String apiSpecFile = "two";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String getCarJson = "get:\\car:two-config-200-application\\json-FlowTest";
    String getCarXml = "get:\\car:two-config-200-text\\xml-FlowTest";
    String postCarJson = "post:\\car:two-config-200-application\\json-FlowTest";
    String postCarXml = "post:\\car:two-config-200-text\\xml-FlowTest";
    String getPetJson = "get:\\pet:two-config-200-application\\json-FlowTest";
    String getPetXml = "get:\\pet:two-config-200-text\\xml-FlowTest";
    String postPetJson = "post:\\pet:two-config-200-application\\json-FlowTest";
    String postPetXml = "post:\\pet:two-config-200-text\\xml-FlowTest";

    assertEquals(1, TestUtils.countOccurrences(generatedContent, getCarJson));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, getCarXml));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, postCarJson));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, postCarXml));

    assertEquals(1, TestUtils.countOccurrences(generatedContent, getPetJson));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, getPetXml));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, postPetJson));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, postPetXml));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));

  }

  @Test
  public void testNestedGenerate() throws Exception {
    String apiSpecFile = "nested";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());

    String expectedGetCarJson = "get:\\car:nested-config-200-application\\json-FlowTest";
    String expectedGetCarXml = "get:\\car:nested-config-200-text\\xml-FlowTest";
    String expectedGetPetOwnerJson = "get:\\pet\\owner:nested-config-200-application\\json-FlowTest";
    String expectedGetPetJson = "get:\\pet:nested-config-200-application\\json-FlowTest";
    String expectedGetPetXml = "get:\\pet:nested-config-200-text\\xml-FlowTest";
    String expectedPostCarJson = "post:\\car:nested-config-200-application\\json-FlowTest";
    String expectedPostCarXml = "post:\\car:nested-config-200-text\\xml-FlowTest";
    String expectedPostPetJson = "post:\\pet:nested-config-200-application\\json-FlowTest";
    String expectedPostPetXml = "post:\\pet:nested-config-200-text\\xml-FlowTest";

    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedGetCarJson));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedGetCarXml));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedGetPetOwnerJson));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedGetPetJson));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedGetPetXml));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedPostCarJson));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedPostCarXml));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedPostPetJson));
    assertEquals(1, TestUtils.countOccurrences(generatedContent, expectedPostPetXml));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testDefaultValueWhenExampleNotSetForQueryParameter() throws Exception {
    String apiSpecFile = "no-example-query-parameter";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHttpQueryParams = "<http:query-params>#[{\"stock\":\"1\"}]</http:query-params>";
    assertTrue(generatedContent.contains(expectedHttpQueryParams));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testNoPropertyForSchema() throws Exception {
    String apiSpecFile = "no-property-for-schema";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testNoPropertyInsideObjectForSchema() throws Exception {
    String apiSpecFile = "no-property-inside-object-schema";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{&quot;albumId&quot;:{}}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testNoType() throws Exception {
    String apiSpecFile = "no-type";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('{}')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testMultiplePostBodyCombinations() throws Exception {
    String apiSpecFile = "multiple-post-body-combination";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedSetPayloadJson = "<set-payload value=\"#['{&quot;message&quot;:&quot;This is an album json post&quot;}']\" />";
    String expectedSetPayloadXml = "<set-payload value=\"#['&lt;test&gt;This is an album xml post&lt;/test&gt;&#xA;']\" />";
    assertEquals(2, TestUtils.countOccurrences(generatedContent, expectedSetPayloadJson));
    assertEquals(2, TestUtils.countOccurrences(generatedContent, expectedSetPayloadXml));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testMultiplePostBodyCombinationsSchema() throws Exception {
    String apiSpecFile = "multiple-post-body-combination-with-schema";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedSetPayloadJson = "<set-payload value=\"#['{&quot;message&quot;:&quot;&quot;}']\" />";
    String expectedSetPayloadXml = "<set-payload value=\"#['&lt;test&gt;This is an album xml post&lt;/test&gt;&#xA;']\" />";
    assertEquals(2, TestUtils.countOccurrences(generatedContent, expectedSetPayloadJson));
    assertEquals(2, TestUtils.countOccurrences(generatedContent, expectedSetPayloadXml));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testBodyWithXmlSchema() throws Exception {
    String apiSpecFile = "body-xml-schema";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedSetPayload = "<set-payload value=\"#['']\" />";
    assertTrue(generatedContent.contains(expectedSetPayload));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testNoBodyResponse() throws Exception {
    String apiSpecFile = "no-body";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedAssertion = "is=\"#[MunitTools::equalTo('']\"\"";
    assertFalse(generatedContent.contains(expectedAssertion));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testIncludedExternalExample() throws Exception {
    String apiSpecFile = "includes-external-example";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedExpression =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo('[{&quot;name&quot;:&quot;Athletic Bilbao&quot;,&quot;score&quot;:3}]')]\"";
    assertTrue(generatedContent.contains(expectedExpression));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void optionalQueryParameterIsNotAdded() throws Exception {
    String apiSpecFile = "optional-query-parameter";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHTtpQueryParams = "<http:query-params>#[{\"stock\":\"10\"}]</http:query-params>";
    assertFalse(generatedContent.contains(expectedHTtpQueryParams));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testNoRequireValueQueryParameters() throws Exception {
    String apiSpecFile = "no-required-query-parameter";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedSetVariable = "<set-variable variableName=\"airline\" value=\"#['all']\" doc:name=\"airline\" />";
    assertFalse(generatedContent.contains(expectedSetVariable));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testNoRequireValueHeads() throws Exception {
    String apiSpecFile = "no-required-headers";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHeaderName = "<http:header headerName=\"status-code\" value=\"XXX\" />";
    assertFalse(generatedContent.contains(expectedHeaderName));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testDateQueryParameters() throws Exception {
    String apiSpecFile = "date-query-parameter";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHttpQueryParams = "<http:query-params>#[{\"createdAfter\":\"1/1/2011\"}]</http:query-params>";
    assertTrue(generatedContent.contains(expectedHttpQueryParams));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testDateHeaders() throws Exception {
    String apiSpecFile = "date-headers";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedHttpHeader = "<http:header headerName=\"date\"/>";
    assertFalse(generatedContent.contains(expectedHttpHeader));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }

  @Test
  public void testDateUriParameters() throws Exception {
    String apiSpecFile = "date-uri-parameter";
    String apikitMainFlowName = generateMainFlowNameForApi(apiSpecFile);
    String expectedEnableFlowSources = String.format(ENABLE_FLOW_SOURCES_TEMPLATE, apikitMainFlowName);

    MuleConfig muleConfig = simpleGenerationRaml(apiSpecFile);
    String generatedContent = IOUtils.toString(muleConfig.getContent());
    String expectedSetVariable = "<set-variable variableName=\"ticketDate\" value=\"#['1/1/2015']\" doc:name=\"ticketDate\" />";
    String expectedPath = "path=\"#['/$(vars.ticketDate)']\"";
    assertTrue(generatedContent.contains(expectedSetVariable));
    assertTrue(generatedContent.contains(expectedPath));
    assertTrue(generatedContent.contains(expectedEnableFlowSources));
  }
}
