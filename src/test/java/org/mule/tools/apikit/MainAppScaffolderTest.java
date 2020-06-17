/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;
import static org.mule.tools.apikit.Helper.countOccurences;
import static org.mule.tools.apikit.TestUtils.assertXmls;
import static org.mule.tools.apikit.TestUtils.getResourceAsStream;
import static org.mule.tools.apikit.TestUtils.getResourceAsString;

public class MainAppScaffolderTest extends AbstractScaffolderTestCase {


  @Test
  public void testSimpleGenerateV08() throws Exception {
    simpleGenerate("scaffolder/simple.raml");
  }

  @Test
  public void testSimpleGenerateV10() throws Exception {
    simpleGenerate("scaffolder/simpleV10.raml");
  }

  @Test
  public void testSimpleGenerateV10HideConsole() throws Exception {
    simpleGenerateHideConsole("scaffolder/simpleV10.raml");
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
  public void testSimpleGenerateWithExtensionWithOldParser() throws Exception {
    simpleGenerateWithExtension();
  }

  @Test
  public void generateWithIncludes08() throws Exception {

    // TODO(APIMF-1705): ignoring this test when running on windows until AMF fixes the referenced issue
    assumeThat(SystemUtils.IS_OS_WINDOWS && isAmf(), is(false));

    String apiLocation = "scaffolder-include-08/api.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);
    assertEquals(1, result.getGeneratedConfigs().size());

    String content = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(2, countOccurences(content, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(content, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(7, countOccurences(content, "<on-error-propagate"));
    assertEquals(7, countOccurences(content, "<ee:message>"));
    assertEquals(7, countOccurences(content, "<ee:variables>"));
    assertEquals(7, countOccurences(content, "<ee:set-variable"));
    assertEquals(7, countOccurences(content, "<ee:set-payload>"));
    assertEquals(4, countOccurences(content, "http:body"));
    assertEquals(2, countOccurences(content, "#[payload]"));
    assertEquals(0, countOccurences(content, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(content, "post:\\Queue:application\\json:api-config"));
    assertEquals(2, countOccurences(content, "post:\\Queue:text\\xml:api-config"));
    assertEquals(0, countOccurences(content, "#[NullPayload.getInstance()]"));
    assertEquals(2, countOccurences(content, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testSimpleGenerateWithExtensionWithNewParser() throws Exception {
    simpleGenerateWithExtension();
  }

  @Test
  public void testSimpleGenerateWithExtensionInNullWithOldParser() throws Exception {
    simpleGenerateWithExtensionInNull();
  }

  @Test
  public void testSimpleGenerateWithExtensionInNullWithNewParser() throws Exception {

    simpleGenerateWithExtensionInNull();
  }

  @Test
  public void generateWithIncludes10() throws Exception {
    // TODO(APIMF-1705): ignoring this test when running on windows until AMF fixes the referenced issue
    assumeThat(SystemUtils.IS_OS_WINDOWS && isAmf(), is(false));

    String apiLocation = "scaffolder-include-10/api.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);

    assertEquals(1, result.getGeneratedConfigs().size());

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(7, countOccurences(s, "<ee:variables>"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "post:\\Queue:application\\json:api-config"));
    assertEquals(2, countOccurences(s, "post:\\Queue:text\\xml:api-config"));
    assertEquals(2, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void generateWithExamples() throws Exception {
    String apiLocation = "scaffolder-with-examples/src/main/resources/api/api.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);
    assertEquals(1, result.getGeneratedConfigs().size());
    String actual = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    String expected = getResourceAsString("scaffolder-with-examples/api.xml");
    assertXmls(actual, expected);
  }

  @Test
  public void testSimpleGenerateWithListenerAndExtensionWithOldParser() throws Exception {
    simpleGenerateWithListenerAndExtension();
  }

  @Test
  public void testSimpleGenerateWithListenerAndExtensionWithNewParser() throws Exception {

    simpleGenerateWithListenerAndExtension();
  }

  @Test
  public void testSimpleGenerateWithCustomDomainWithOldParser() throws Exception {
    simpleGenerateWithCustomDomain();
  }

  @Test
  public void testSimpleGenerateWithCustomDomainWithNewParser() throws Exception {

    simpleGenerateWithCustomDomain();
  }

  @Test
  public void testSimpleGenerateWithCustomExternalDomainWithOldParser() throws Exception {
    simpleGenerateWithCustomExternalDomain();
  }

  @Test
  public void testSimpleGenerateWithCustomExternalDomainWithNewParser() throws Exception {

    simpleGenerateWithCustomExternalDomain();
  }

  @Test
  public void testSimpleGenerateWithCustomDomainAndExtensionWithOldParser() throws Exception {
    simpleGenerateWithCustomDomainAndExtension();
  }

  @Test
  public void testSimpleGenerateWithCustomDomainAndExtensionWithNewParser() throws Exception {

    simpleGenerateWithCustomDomainAndExtension();
  }

  @Test
  public void testSimpleGenerateWithCustomDomainWithMultipleLCWithOldParser() throws Exception {
    simpleGenerateWithCustomDomainWithMultipleLC();
  }

  @Test
  public void testSimpleGenerateWithCustomDomainWithMultipleLCWithNewParser() throws Exception {

    simpleGenerateWithCustomDomainWithMultipleLC();
  }

  @Test
  public void testSimpleGenerateWithCustomExternalDomainWithMultipleConfigsWithOldParser() throws Exception {
    simpleGenerateWithCustomExternalDomainWithMultipleConfigs();
  }

  @Test
  public void testSimpleGenerateWithCustomExternalDomainWithMultipleConfigsWithNewParser() throws Exception {

    simpleGenerateWithCustomExternalDomainWithMultipleConfigs();
  }

  @Test
  public void testSimpleGenerateWithEmptyDomainWithOldParser() throws Exception {
    simpleGenerateWithEmptyDomain();
  }

  @Test
  public void testSimpleGenerateWithEmptyDomainWithNewParser() throws Exception {

    simpleGenerateWithEmptyDomain();
  }

  @Test
  public void testTwoResourceGenerateWithOldParser() throws Exception {
    testTwoResourceGenerate();
  }

  @Test
  public void testTwoResourceGenerateWithNewParser() throws Exception {

    testTwoResourceGenerate();
  }

  @Test
  public void testTwoResourceGenerate() throws Exception {
    String apiLocation = "scaffolder/two.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);

    assertEquals(1, result.getGeneratedConfigs().size());

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(7, countOccurences(s, "<ee:variables>"));
    assertEquals(7, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));

    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));

    assertEquals(2, countOccurences(s, "get:\\pet:two-config"));
    assertEquals(2, countOccurences(s, "post:\\pet:two-config"));

    assertEquals(2, countOccurences(s, "get:\\car:two-config"));
    assertEquals(2, countOccurences(s, "post:\\car:two-config"));

    assertEquals(4, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testNestedGenerateWithOldParser() throws Exception {
    nestedGenerate();
  }

  @Test
  public void testNestedGenerateWithNewParser() throws Exception {

    nestedGenerate();
  }

  @Test
  public void testSimpleGenerationWithRamlInsideAFolder() throws Exception {
    String apiLocation = "raml-inside-folder/folder/api.raml";
    List<String> muleConfigsLocation = Arrays.asList("raml-inside-folder/api.xml");
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation, muleConfigsLocation);

    assertEquals(1, result.getGeneratedConfigs().size());

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(1, countOccurences(s, "<error-handler name="));
    assertEquals(1, countOccurences(s, "<flow name=\"post:\\oneResource:api-config\">"));
    assertEquals(1, countOccurences(s, "<http:listener-config name="));
  }

  @Test
  public void testNoNameGenerateWithOldParser() throws Exception {
    noNameGenerate();
  }

  @Test
  public void testNoNameGenerateWithNewParser() throws Exception {

    noNameGenerate();
  }

  @Test
  public void testExampleGenerateWithOldParser() throws Exception {
    exampleGenerate();
  }

  @Test
  public void testExampleGenerateWithNewParser() throws Exception {

    exampleGenerate();
  }

  @Test
  public void testExampleGenerateWithRamlType() throws Exception {
    String apiLocation = "scaffolder/example-v10.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
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
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.CE, apiLocation);
    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    String name = fileNameWhithOutExtension(apiLocation);

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
  public void doubleRootRamlWithOldParser() throws Exception {
    doubleRootRaml();
  }

  @Test
  public void doubleRootRamlWithNewParser() throws Exception {

    doubleRootRaml();
  }

  @Test
  public void testGenerateWithExchangeModules() throws Exception {
    String apiLocation = "scaffolder-exchange/api.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(1, countOccurences(s, "get:\\resource1:api-config"));
    assertEquals(1, countOccurences(s, "get:\\resource2:api-config"));
    assertEquals(1, countOccurences(s, "get:\\resource3:api-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
  }

  @Test
  public void testGenerateWithRecursiveApi() throws Exception {
    if (!isAmf())
      return;
    String apiLocation = "scaffolder/api-with-resource-type.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(2, countOccurences(s, "post:\\v4\\items:application\\json:api-with-resource-type-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
  }

  @Test
  public void testGenerateWithAMF() throws Exception {
    if (!isAmf())
      return;
    String apiLocation = "parser/amf-only.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(2, countOccurences(s, "get:\\test:amf-only-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
  }

  @Test
  public void testGenerateWithRAML() throws Exception {
    if (isAmf())
      return;
    String apiLocation = "parser/raml-parser-only.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(2, countOccurences(s, "get:\\test:raml-parser-only-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
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

  private void testScaffoldTwoApis(String testFolder, List<MuleConfig> existingMuleConfigs, MuleDomain domainFile)
      throws Exception {
    // The new Scaffolder API doesn't support scaffollding more than one ApiSpecification at a time.
    // If you want to scaffold more than one ApiSpec you have to call the Scaffolder N times.
    final String basePath = testFolder + "src/main/resources/api/";
    final String api1 = basePath + "api1/api.raml";
    final String api2 = basePath + "api2/api.raml";
    XMLUnit.setIgnoreWhitespace(true);

    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    ScaffoldingConfiguration firstScaffoldingConfiguration = getScaffoldingConfiguration(api1, existingMuleConfigs, domainFile);
    ScaffoldingResult result = mainAppScaffolder.run(firstScaffoldingConfiguration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());
    MuleConfig generatedMuleConfig = result.getGeneratedConfigs().get(0);

    String firstGeneratedMuleConfigContent = APIKitTools.readContents(generatedMuleConfig.getContent());
    Diff firstMuleConfigDiff = XMLUnit.compareXML(firstGeneratedMuleConfigContent,
                                                  APIKitTools.readContents(getResourceAsStream(testFolder + "api.xml")));

    existingMuleConfigs.add(generatedMuleConfig);
    ScaffoldingConfiguration secondScaffoldingConfiguration = getScaffoldingConfiguration(api2, existingMuleConfigs, domainFile);
    result = mainAppScaffolder.run(secondScaffoldingConfiguration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());

    String secondGeneratedMuleConfigContent = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    Diff secondMuleConfigDiff = XMLUnit.compareXML(secondGeneratedMuleConfigContent,
                                                   APIKitTools.readContents(getResourceAsStream(testFolder + "api-2.xml")));

    assertTrue(firstMuleConfigDiff.identical());
    assertTrue(secondMuleConfigDiff.identical());
  }


  private void simpleGenerateForCE(final String apiPath) throws Exception {
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.CE, apiPath);

    assertEquals(1, result.getGeneratedConfigs().size());

    final String name = fileNameWhithOutExtension(apiPath);
    final String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(1, countOccurences(s, "http:listener-config name=\"simple"));
    assertEquals(1, countOccurences(s, "http:listener-connection host=\"0.0.0.0\" port=\"8081\""));
    assertEquals(2, countOccurences(s, "http:listener "));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "#[vars.outboundHeaders default {}]"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(0, countOccurences(s, "<ee:"));
    assertEquals(7,
                 countOccurences(s,
                                 "<set-variable variableName=\"outboundHeaders\" value=\"#[{'Content-Type':'application/json'}]\" />"));
    assertEquals(7, countOccurences(s, "<set-variable variableName=\"httpStatus\""));
    assertEquals(2,
                 countOccurences(s, "<set-variable value=\"#[attributes.uriParams.'name']\" variableName=\"name\" />"));
    assertEquals(1,
                 countOccurences(s, "<set-variable value=\"#[attributes.uriParams.'owner']\" variableName=\"owner\""));
    assertEquals(7, countOccurences(s, "<set-payload"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(8, countOccurences(s, "http:headers"));
    assertEquals(2, countOccurences(s, "get:\\:" + name + "-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:" + name + "-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:" + name + "-config"));
    assertEquals(1, countOccurences(s, "apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
    assertEquals(0, countOccurences(s, "#[null]"));
    assertEquals(0,
                 countOccurences(s,
                                 "expression-component>mel:flowVars['variables.outboundHeaders default {}'].put('Content-Type', 'application/json')</expression-component>"));
    assertEquals(0,
                 countOccurences(s,
                                 "set-variable variableName=\"variables.outboundHeaders default {}\" value=\"#[mel:new java.util.HashMap()]\" />"));
    assertEquals(0, countOccurences(s, "exception-strategy"));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerateHideConsole(final String apiPath) throws Exception {
    ScaffoldingResult result = scaffoldApiHiddenConsole(RuntimeEdition.EE, apiPath);

    assertEquals(1, result.getGeneratedConfigs().size());

    final String name = fileNameWhithOutExtension(apiPath);
    final String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(1, countOccurences(s, "http:listener-config name=\"simple"));
    assertEquals(1, countOccurences(s, "http:listener-connection host=\"0.0.0.0\" port=\"8081\""));
    assertEquals(1, countOccurences(s, "http:listener "));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(1, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(1, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(2, countOccurences(s, "#[vars.outboundHeaders default {}]"));
    assertEquals(6, countOccurences(s, "<on-error-propagate"));
    assertEquals(6, countOccurences(s, "<ee:message>"));
    assertEquals(8, countOccurences(s, "<ee:variables>"));
    assertEquals(9, countOccurences(s, "<ee:set-variable"));
    assertEquals(2, countOccurences(s, "<ee:set-variable variableName=\"name\">attributes.uriParams.'name'</ee:set-variable>"));
    assertEquals(1, countOccurences(s, "<ee:set-variable variableName=\"owner\">attributes.uriParams.'owner'</ee:set-variable>"));
    assertEquals(6, countOccurences(s, "<ee:set-payload>"));
    assertEquals(2, countOccurences(s, "http:body"));
    assertEquals(1, countOccurences(s, "#[payload]"));
    assertEquals(4, countOccurences(s, "http:headers"));
    assertEquals(2, countOccurences(s, "get:\\:" + name + "-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:" + name + "-config"));
    assertEquals(0, countOccurences(s, "apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
    assertEquals(0, countOccurences(s, "#[null]"));
    assertEquals(0,
                 countOccurences(s,
                                 "expression-component>mel:flowVars['variables.outboundHeaders default {}'].put('Content-Type', 'application/json')</expression-component>"));
    assertEquals(0,
                 countOccurences(s,
                                 "set-variable variableName=\"variables.outboundHeaders default {}\" value=\"#[mel:new java.util.HashMap()]\" />"));
    assertEquals(0, countOccurences(s, "exception-strategy"));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerate(final String apiPath) throws Exception {
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiPath);

    assertEquals(1, result.getGeneratedConfigs().size());

    final String name = fileNameWhithOutExtension(apiPath);
    final String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(1, countOccurences(s, "http:listener-config name=\"simple"));
    assertEquals(1, countOccurences(s, "http:listener-connection host=\"0.0.0.0\" port=\"8081\""));
    assertEquals(2, countOccurences(s, "http:listener "));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "#[vars.outboundHeaders default {}]"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(2, countOccurences(s, "<ee:set-variable variableName=\"name\">attributes.uriParams.'name'</ee:set-variable>"));
    assertEquals(1, countOccurences(s, "<ee:set-variable variableName=\"owner\">attributes.uriParams.'owner'</ee:set-variable>"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(8, countOccurences(s, "http:headers"));
    assertEquals(2, countOccurences(s, "get:\\:" + name + "-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:" + name + "-config"));
    assertEquals(1, countOccurences(s, "apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
    assertEquals(0, countOccurences(s, "#[null]"));
    assertEquals(0,
                 countOccurences(s,
                                 "expression-component>mel:flowVars['variables.outboundHeaders default {}'].put('Content-Type', 'application/json')</expression-component>"));
    assertEquals(0,
                 countOccurences(s,
                                 "set-variable variableName=\"variables.outboundHeaders default {}\" value=\"#[mel:new java.util.HashMap()]\" />"));
    assertEquals(0, countOccurences(s, "exception-strategy"));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerateWithExtension() throws Exception {
    String apiLocation = "scaffolder/simple.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);

    assertEquals(1, result.getGeneratedConfigs().size());

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerateWithExtensionInNull() throws Exception {
    String apiLocation = "scaffolder/simple.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);

    assertEquals(1, result.getGeneratedConfigs().size());

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerateWithListenerAndExtension() throws Exception {
    String apiLocation = "scaffolder/simple.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);

    assertEquals(1, result.getGeneratedConfigs().size());

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(0, countOccurences(s, "<http:inbound"));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerateWithCustomDomain() throws Exception {
    String apiLocation = "scaffolder/simple.raml";
    String muleDomainLocation = "custom-domain-4/mule-domain-config.xml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation, muleDomainLocation);

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(0, countOccurences(s, "<http:listener-config"));
    assertEquals(2, countOccurences(s, "<http:listener "));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "<http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(2, countOccurences(s, "config-ref=\"http-lc-0.0.0.0-8081\""));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerateWithCustomExternalDomain() throws Exception {
    String apiLocation = "scaffolder/simple.raml";
    URL fileUrl = Thread.currentThread().getContextClassLoader().getResource("custom-domain-4/external-domain.jar");
    File artifact = new File(fileUrl.getFile());
    MuleDomain muleDomain = MuleDomainFactory.fromDeployableArtifact(artifact);
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation, Collections.emptyList(), muleDomain);

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(0, countOccurences(s, "<http:listener-config"));
    assertEquals(2, countOccurences(s, "<http:listener "));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "<http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(2, countOccurences(s, "config-ref=\"http-lc-0.0.0.0-8081\""));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerateWithCustomDomainAndExtension() throws Exception {
    String apiLocation = "scaffolder/simple.raml";
    String muleDomainLocation = "custom-domain-4/mule-domain-config.xml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation, muleDomainLocation);

    assertEquals(1, result.getGeneratedConfigs().size());

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());

    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(0, countOccurences(s, "<http:listener-config"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "config-ref=\"http-lc-0.0.0.0-8081\""));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerateWithCustomDomainWithMultipleLC() throws Exception {
    String apiLocation = "scaffolder/simple.raml";
    String muleDomainLocation = "custom-domain-multiple-lc-4/mule-domain-config.xml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation, muleDomainLocation);

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(0, countOccurences(s, "<http:listener-config"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "config-ref=\"abcd\""));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerateWithCustomExternalDomainWithMultipleConfigs() throws Exception {
    String apiLocation = "scaffolder/simple.raml";
    URL fileUrl = Thread.currentThread().getContextClassLoader()
        .getResource("custom-external-domain-multiple-configs/external-domain-2-configs.jar");
    File artifact = new File(fileUrl.getFile());
    MuleDomain muleDomain = MuleDomainFactory.fromDeployableArtifact(artifact);
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation, Collections.emptyList(), muleDomain);

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(0, countOccurences(s, "<http:listener-config"));
    assertEquals(2, countOccurences(s, "<http:listener "));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "<http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(2, countOccurences(s, "config-ref=\"http-lc-0.0.0.0-8081\""));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerateWithEmptyDomain() throws Exception {
    String apiLocation = "scaffolder/simple.raml";
    String muleDomainLocation = "empty-domain/mule-domain-config.xml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation, muleDomainLocation);

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }


  private void nestedGenerate() throws Exception {
    String apiLocation = "scaffolder/nested.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(7, countOccurences(s, "<ee:variables>"));
    assertEquals(7, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "get:\\pet:nested-config"));
    assertEquals(2, countOccurences(s, "post:\\pet:nested-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\owner:nested-config"));
    assertEquals(2, countOccurences(s, "get:\\car:nested-config"));
    assertEquals(2, countOccurences(s, "post:\\car:nested-config"));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void twoUriParamas() throws Exception {
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, "scaffolder/twoUriParams.raml");
    String s = IOUtils.toString(result.getGeneratedConfigs().get(0).getContent());

    assertEquals(1, countOccurences(s, "<ee:set-variable variableName=\"id\">attributes.uriParams.'id'</ee:set-variable>"));
    assertEquals(1,
                 countOccurences(s,
                                 "<ee:set-variable variableName=\"mediaTypeExtension\">attributes.uriParams.'mediaTypeExtension'</ee:set-variable>"));
  }


  private void noNameGenerate() throws Exception {
    String apiLocation = "scaffolder/no-name.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(7, countOccurences(s, "<ee:variables>"));
    assertEquals(7, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(1, countOccurences(s, "http:listener-config name=\"no-name-httpListenerConfig\">"));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"no-name-httpListenerConfig\" path=\"/api/*\""));
  }

  private void exampleGenerate() throws Exception {
    String apiLocation = "scaffolder/example.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(9, countOccurences(s, "<ee:message>"));
    assertEquals(7, countOccurences(s, "<ee:variables>"));
    assertEquals(7, countOccurences(s, "<ee:set-variable"));
    assertEquals(9, countOccurences(s, "<ee:set-payload>"));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
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


  private void doubleRootRaml() throws Exception {
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
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "<logger level=\"INFO\" message="));
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(7, countOccurences(s, "<ee:variables>"));
    assertEquals(7, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));

    result = mainAppScaffolder.run(secondScaffoldingConfiguration);
    assertTrue(result.isSuccess());
    assertTrue(result.getGeneratedConfigs().size() == 1);

    String s2 = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals(2, countOccurences(s2, "get:\\pet:two-config"));
    assertEquals(2, countOccurences(s2, "post:\\pet:two-config"));
    assertEquals(2, countOccurences(s2, "get:\\car:two-config"));
    assertEquals(2, countOccurences(s2, "post:\\car:two-config"));
    assertEquals(0, countOccurences(s2, "interpretRequestErrors=\"true\""));
    assertEquals(4, countOccurences(s2, "<logger level=\"INFO\" message="));
    assertEquals(2, countOccurences(s2, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s2, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s2, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s2, "<on-error-propagate"));
    assertEquals(7, countOccurences(s2, "<ee:message>"));
    assertEquals(7, countOccurences(s2, "<ee:variables>"));
    assertEquals(7, countOccurences(s2, "<ee:set-variable"));
    assertEquals(7, countOccurences(s2, "<ee:set-payload>"));

  }

  @Test
  public void scaffoldEmptyAPI() throws Exception {
    String apiLocation = "scaffolder/without-resources.raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, apiLocation);

    assertEquals(1, result.getGeneratedConfigs().size());

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    assertEquals("Files are different",
                 APIKitTools.readContents(getResourceAsStream("scaffolder/expected-result-without-resources.xml"))
                     .replaceAll("api=(.*)raml\"", "api=\"\"").replaceAll("\\s+", ""),
                 s.replaceAll("api=(.*)raml\"", "api=\"\"").replaceAll("\\s+", ""));
  }

  @Test
  public void multipleContentTypesWithoutSchemaDefinition() throws Exception {
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, "scaffolder/body-with-mime-types-without-schema/testing-api.raml");
    String muleConfig = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());

    assertEquals(APIKitTools.readContents(getResourceAsStream("scaffolder/body-with-mime-types-without-schema/output.xml"))
        .replaceAll("\\s+", ""), muleConfig.replaceAll("\\s+", ""));

  }
}
