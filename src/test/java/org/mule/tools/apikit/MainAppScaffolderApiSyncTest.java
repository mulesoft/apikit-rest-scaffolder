/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.XmlOccurrencesAsserterBuilder.XmlOccurrencesAsserter;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffolderContextBuilder;
import org.mule.tools.apikit.model.ScaffolderResourceLoader;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.Helper.countOccurences;
import static org.mule.tools.apikit.TestUtils.getResourceAsStream;
import static org.mule.tools.apikit.model.RuntimeEdition.EE;

public class MainAppScaffolderApiSyncTest extends AbstractScaffolderTestCase {

  private final static ScaffolderResourceLoader scaffolderResourceLoaderMock = Mockito.mock(ScaffolderResourceLoader.class);

  private final static String ROOT_RAML_RESOURCE_URL = "resource::com.mycompany:raml-api:1.0.0:raml:zip:";
  private final static String DEPENDENCIES_RESOURCE_URL = "resource::com.mycompany:raml-library:1.1.0:raml-fragment:zip:";

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void testSimpleGenerationV08() throws Exception {
    final String ramlFolder = "scaffolder/";
    final String rootRaml = "simpleV10";

    testSimple(ramlFolder, rootRaml);
  }


  @Test
  public void testSimpleGenerationV10() throws Exception {
    final String ramlFolder = "scaffolder/";
    final String rootRaml = "simpleV10";

    testSimple(ramlFolder, rootRaml);
  }

  @Test
  public void testRAMLWithoutResources() throws Exception {
    MuleConfig muleConfig = generateMuleConfigForApiSync("api-sync/empty-api", "without-resources");

    InputStream expectedInputStream = getResourceAsStream("api-sync/empty-api/expected-result.xml");
    InputStream generatedInputStream = muleConfig.getContent();

    XMLUnit.setIgnoreWhitespace(true);
    Diff diff = XMLUnit.compareXML(APIKitTools.readContents(expectedInputStream), APIKitTools.readContents(generatedInputStream));
    assertTrue(diff.identical());
  }

  @Test
  public void testRAMLWithCharset() throws Exception {
    MuleConfig muleConfig = generateMuleConfigForApiSync("api-sync/api-raml-with-charset", "api");

    InputStream expectedInputStream = getResourceAsStream("api-sync/api-raml-with-charset/expected-result.xml");
    InputStream generatedInputStream = muleConfig.getContent();

    XMLUnit.setIgnoreWhitespace(true);
    Diff diff = XMLUnit.compareXML(APIKitTools.readContents(expectedInputStream), APIKitTools.readContents(generatedInputStream));
    assertTrue(diff.identical());
  }

  @Test
  public void generateWithIncludes10() throws Exception {
    String rootRaml = "api";
    String ramlFolder = "src/test/resources/api-sync/scaffolder-include-10/";

    MuleConfig muleConfig = generateMuleConfigForApiSync(ramlFolder, rootRaml);
    InputStream generatedInputStream = muleConfig.getContent();
    String s = APIKitTools.readContents(generatedInputStream);
    assertNotNull(s);
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
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(2, countOccurences(s, "post:\\Queue:application\\json:" + rootRaml + "-config"));
    assertEquals(2, countOccurences(s, "post:\\Queue:text\\xml:" + rootRaml + "-config"));
  }


  @Test
  public void libraryReferenceToRoot() throws Exception {
    final String rootRaml = "test api";
    final String ramlFolder = "src/test/resources/api-sync/library-reference-to-root/root/";
    final String libraryFolder = "src/test/resources/api-sync/library-reference-to-root/library/";
    final List<String> libraryFiles = Arrays.asList("library.raml", "reused-fragment.raml");

    final String exchangeJsonResourceURL = ROOT_RAML_RESOURCE_URL + "exchange.json";
    final String rootRamlResourceURL = ROOT_RAML_RESOURCE_URL + rootRaml + ".raml";

    mockScaffolderResourceLoader(exchangeJsonResourceURL, ramlFolder, rootRaml + ".json");
    mockScaffolderResourceLoader(rootRamlResourceURL, ramlFolder, rootRaml + ".raml");

    for (String rootRamlFile : libraryFiles) {
      mockScaffolderResourceLoader(DEPENDENCIES_RESOURCE_URL + rootRamlFile, libraryFolder, rootRamlFile);
    }

    ApiReference apiReference = ApiReference.create(ROOT_RAML_RESOURCE_URL + rootRaml + ".raml", scaffolderResourceLoaderMock);
    ParseResult parseResult = new ParserService().parse(apiReference);
    assertTrue(parseResult.success());

    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    ScaffoldingConfiguration configuration =
        new ScaffoldingConfiguration.Builder().withApi(parseResult.get()).build();
    ScaffoldingResult result = mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());

    String expectedMuleConfigContent =
        APIKitTools.readContents(getResourceAsStream("api-sync/library-reference-to-root/expected.xml"));
    String generatedMuleConfigContent = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());

    XMLUnit.setIgnoreWhitespace(true);
    Diff diff = XMLUnit.compareXML(expectedMuleConfigContent, generatedMuleConfigContent);
    assertTrue(diff.identical());
  }

  private void testSimple(String ramlFolder, String rootRaml) throws Exception {
    String ramlFilePath = ramlFolder + rootRaml + ".raml";
    ScaffoldingResult result = scaffoldApi(RuntimeEdition.EE, ramlFilePath);

    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());

    String s = APIKitTools.readContents(result.getGeneratedConfigs().get(0).getContent());
    XmlOccurrencesAsserter xmlOccurrencesAsserter = new XmlOccurrencesAsserterBuilder()
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
        .build();
    xmlOccurrencesAsserter.assertOccurrences(s);
    assertEquals(1, countOccurences(s, "http:listener-config name=\"" + rootRaml));
    assertEquals(1, countOccurences(s, "http:listener-connection host=\"0.0.0.0\" port=\"8081\""));
    assertEquals(2, countOccurences(s, "<ee:set-variable variableName=\"name\">attributes.uriParams.'name'</ee:set-variable>"));
    assertEquals(1, countOccurences(s, "<ee:set-variable variableName=\"owner\">attributes.uriParams.'owner'</ee:set-variable>"));
    assertEquals(2, countOccurences(s, "get:\\:" + rootRaml + "-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:" + rootRaml + "-config"));
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
  public void testRaml08Fallback() throws Exception {
    if (!isAmf()) {
      MuleConfig muleConfig = generateMuleConfigForApiSync("/api-sync/fallback-raml-08", "api");
      InputStream expectedInputStream = getResourceAsStream("api-sync/fallback-raml-08/expected.xml");
      String expectedString = APIKitTools.readContents(expectedInputStream);
      String generatedContentString = APIKitTools.readContents(muleConfig.getContent());

      XMLUnit.setIgnoreWhitespace(true);
      Diff diff = XMLUnit.compareXML(expectedString, generatedContentString);
      assertTrue(diff.identical());
    }
  }

  private MuleConfig generateMuleConfigForApiSync(String ramlFolder, String rootRaml) {
    ResourceLoader resourceLoader = new TestScaffolderResourceLoader(ramlFolder);
    ApiReference apiReference = ApiReference.create(ROOT_RAML_RESOURCE_URL + rootRaml + ".raml", resourceLoader);
    ParseResult parseResult = new ParserService().parse(apiReference);
    assertTrue(parseResult.success());

    ScaffolderContext context = ScaffolderContextBuilder.builder().withRuntimeEdition(EE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    ScaffoldingConfiguration configuration =
        new ScaffoldingConfiguration.Builder().withApi(parseResult.get()).build();
    ScaffoldingResult result = mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    return result.getGeneratedConfigs().get(0);
  }

  private void mockScaffolderResourceLoader(String resourceURL, String folder, String file) throws Exception {
    resourceURL = resourceURL.replaceAll(" ", "%20");
    Mockito.doReturn(getToBeReturned(folder, file)).when(scaffolderResourceLoaderMock)
        .getResource(resourceURL);
    Mockito.doReturn(getInputStream(folder + file)).doReturn(getInputStream(folder + file))
        .doReturn(getInputStream(folder + file)).when(scaffolderResourceLoaderMock)
        .getResourceAsStream(resourceURL);
  }

  private URI getToBeReturned(String folder, String file) {
    return new File(folder + file).toURI();
  }

  private InputStream getInputStream(String resourcePath) throws FileNotFoundException {
    return new FileInputStream(resourcePath);
  }
}
