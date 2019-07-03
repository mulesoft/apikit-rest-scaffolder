/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.ActionType;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.Response;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.output.GenerationModel;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MunitTestFlowScopeTest {

  private static final String APIKIT_FLOW_NAME = "apikitExampleFlow";
  private static final String RESOURCES_BASE_DIR = "/output/munit-test/";

  private Response responseMock;
  private Map<String, Response> responseMap;

  private Action actionMock;
  private Resource resourceMock;
  private GenerationModel flowEntryMock;

  private MunitTestFlowScope scope;
  private APIKitConfig apiKitConfigMock;
  private ApikitMainFlowContainer apikitMainFlowContainerMock;

  @Before
  public void setUp() {
    responseMap = new HashMap<>();
    responseMock = mock(Response.class);
    when(responseMock.getBody()).thenReturn(null);
    responseMap.put("200", responseMock);

    resourceMock = mock(Resource.class);
    when(resourceMock.getRelativeUri()).thenReturn("/");

    actionMock = mock(Action.class);
    when(actionMock.getResponses()).thenReturn(responseMap);
    when(actionMock.getResource()).thenReturn(resourceMock);

    flowEntryMock = mock(GenerationModel.class);
    when(flowEntryMock.getAction()).thenReturn(actionMock);

    apiKitConfigMock = mock(APIKitConfig.class);
    when(apiKitConfigMock.getName()).thenReturn("ApikitConfigName");
    apikitMainFlowContainerMock = mock(ApikitMainFlowContainer.class);
    when(apikitMainFlowContainerMock.getConfig()).thenReturn(apiKitConfigMock);
    when(flowEntryMock.getApi()).thenReturn(apikitMainFlowContainerMock);

    when(flowEntryMock.getName()).thenReturn("retrievePet");
  }


  @Test
  public void testGenerateTests() throws Exception {
    setRequestVerb("get");

    scope = new MunitTestFlowScope(flowEntryMock, APIKIT_FLOW_NAME);
    List<Element> elements = scope.generateTests();

    assertSimilarityWithFile(elements, "generate-tests");
  }

  @Test
  public void putEmptyBody() throws Exception {
    setRequestVerb("put");

    when(actionMock.getType()).thenReturn(ActionType.PUT);
    when(actionMock.getBody()).thenReturn(null);

    scope = new MunitTestFlowScope(flowEntryMock, APIKIT_FLOW_NAME);
    List<Element> elements = scope.generateTests();

    assertSimilarityWithFile(elements, "put-empty-body");

  }

  @Test
  public void patchEmptyBody() throws Exception {
    setRequestVerb("patch");

    when(actionMock.getType()).thenReturn(ActionType.PATCH);
    when(actionMock.getBody()).thenReturn(null);

    scope = new MunitTestFlowScope(flowEntryMock, APIKIT_FLOW_NAME);
    List<Element> elements = scope.generateTests();

    assertSimilarityWithFile(elements, "patch-empty-body");

  }

  @Test
  public void postEmptyBody() throws Exception {
    setRequestVerb("post");

    when(actionMock.getType()).thenReturn(ActionType.POST);
    when(actionMock.getBody()).thenReturn(null);

    scope = new MunitTestFlowScope(flowEntryMock, APIKIT_FLOW_NAME);
    List<Element> elements = scope.generateTests();

    assertSimilarityWithFile(elements, "post-empty-body");

  }

  @Test
  public void postWithMatchingMimeTypeEmptyBody() throws Exception {
    String matchingMimeType = "application/json";

    setRequestVerb("post");

    Map<String, MimeType> responseBody =
        buildMimeTypeMapWithExample(matchingMimeType, "{\"id\":\"a_id\",\"value\":\"a_value\"}");
    when(responseMock.getBody()).thenReturn(responseBody);

    Map<String, MimeType> requestBody = new HashMap<>();

    when(actionMock.getType()).thenReturn(ActionType.POST);
    when(actionMock.getBody()).thenReturn(requestBody);

    scope = new MunitTestFlowScope(flowEntryMock, APIKIT_FLOW_NAME);
    List<Element> elements = scope.generateTests();

    assertSimilarityWithFile(elements, "post-with-matching-mimetype-empty-body");

  }

  @Test
  public void postWithMatchingMimeTypeBodyExample() throws Exception {
    String matchingMimeType = "application/json";

    setRequestVerb("post");

    Map<String, MimeType> responseBody =
        buildMimeTypeMapWithExample(matchingMimeType, "{\"id\":\"a_id\",\"value\":\"a_value\"}");
    when(responseMock.getBody()).thenReturn(responseBody);
    when(flowEntryMock.getMimeType()).thenReturn(matchingMimeType);

    Map<String, MimeType> requestBody = new HashMap<>();
    requestBody.put(matchingMimeType, buildMimeTypeWithExample(matchingMimeType, "{\"ids\":\"a_id\",\"values\":\"a_value\"}"));
    requestBody.put("application/xml",
                    buildMimeTypeWithExample("application/xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><lala></lala>"));


    when(actionMock.getType()).thenReturn(ActionType.POST);
    when(actionMock.getBody()).thenReturn(requestBody);

    scope = new MunitTestFlowScope(flowEntryMock, APIKIT_FLOW_NAME);
    List<Element> elements = scope.generateTests();

    assertSimilarityWithFile(elements, "post-with-matching-mimetype-body-example");

  }

  @Test
  public void postWithMatchingMimeTypeBodySchema() throws Exception {
    String matchingMimeType = "application/json";

    setRequestVerb("post");

    Map<String, MimeType> responseBody =
        buildMimeTypeMapWithExample(matchingMimeType, "{\"id\":\"a_id\",\"value\":\"a_value\"}");
    when(responseMock.getBody()).thenReturn(responseBody);
    when(flowEntryMock.getMimeType()).thenReturn(matchingMimeType);

    Map<String, MimeType> requestBody = new HashMap<>();
    requestBody
        .put(matchingMimeType,
             buildMimeTypeWithSchema(matchingMimeType,
                                     "{\"title\": \"League Schema\",\"type\": \"object\",\"properties\": {\"entero\": {\"type\": \"integer\"},\"cadena\": {\"type\": \"string\"}}}"));
    requestBody.put("application/xml",
                    buildMimeTypeWithExample("application/xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><lala></lala>"));


    when(actionMock.getType()).thenReturn(ActionType.POST);
    when(actionMock.getBody()).thenReturn(requestBody);

    scope = new MunitTestFlowScope(flowEntryMock, APIKIT_FLOW_NAME);
    List<Element> elements = scope.generateTests();

    assertSimilarityWithFile(elements, "post-with-matching-mimetype-body-schema");

  }

  @Test
  public void postNoMatchingMimeTypeEmptyBody() throws Exception {
    setRequestVerb("post");

    // IResponse
    Map<String, MimeType> responseBody =
        buildMimeTypeMapWithExample("application/xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><lala></lala>");
    when(responseMock.getBody()).thenReturn(responseBody);

    // Mock IAction
    Map<String, MimeType> requestBody = new HashMap<>();

    when(actionMock.getType()).thenReturn(ActionType.POST);
    when(actionMock.getBody()).thenReturn(requestBody);

    scope = new MunitTestFlowScope(flowEntryMock, APIKIT_FLOW_NAME);
    List<Element> elements = scope.generateTests();

    assertSimilarityWithFile(elements, "post-no-matching-mimetype-empty-body");

  }

  @Test
  public void postNoMatchingMimeTypeBodyExample() throws Exception {
    String matchingIMimeType = "application/json";

    setRequestVerb("post");

    Map<String, MimeType> responseBody =
        buildMimeTypeMapWithExample(matchingIMimeType, "{\"id\":\"a_id\",\"value\":\"a_value\"}");
    when(responseMock.getBody()).thenReturn(responseBody);

    Map<String, MimeType> requestBody = new HashMap<>();
    requestBody.put("application/xml",
                    buildMimeTypeWithExample("application/xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><lala></lala>"));


    when(actionMock.getType()).thenReturn(ActionType.POST);
    when(actionMock.getBody()).thenReturn(requestBody);

    scope = new MunitTestFlowScope(flowEntryMock, APIKIT_FLOW_NAME);
    List<Element> elements = scope.generateTests();

    assertSimilarityWithFile(elements, "post-no-matching-mimetype-body-example");

  }

  @Test
  public void postNoMatchingMimeTypeBodySchema() throws Exception {
    String matchingMimeType = "application/xml";

    setRequestVerb("post");

    Map<String, MimeType> responseBody =
        buildMimeTypeMapWithExample(matchingMimeType, "{\"id\":\"a_id\",\"value\":\"a_value\"}");
    when(responseMock.getBody()).thenReturn(responseBody);

    Map<String, MimeType> requestBody = new HashMap<>();
    requestBody
        .put(matchingMimeType,
             buildMimeTypeWithSchema("application/json",
                                     "{\"title\": \"League Schema\",\"type\": \"object\",\"properties\": {\"entero\": {\"type\": \"integer\"},\"cadena\": {\"type\": \"string\"}}}"));

    when(actionMock.getType()).thenReturn(ActionType.POST);
    when(actionMock.getBody()).thenReturn(requestBody);

    scope = new MunitTestFlowScope(flowEntryMock, APIKIT_FLOW_NAME);
    List<Element> elements = scope.generateTests();

    assertSimilarityWithFile(elements, "post-no-matching-mimetype-body-schema");

  }

  @Test
  public void errorCodeIResponseShouldUseSuccessStatusCodeValidator() throws Exception {
    setRequestVerb("get");

    responseMap.clear();
    responseMap.put("400", responseMock);

    scope = new MunitTestFlowScope(flowEntryMock, APIKIT_FLOW_NAME);
    List<Element> elements = scope.generateTests();

    assertSimilarityWithFile(elements, "error-code-response-use-success-status-validator");

  }

  @Test
  public void testGetResourceElementWithCorrectFileNameForResponse() throws Exception {
    setRequestVerb("get");

    Resource resource = mock(Resource.class);
    when(resource.getUri()).thenReturn("pet");
    when(flowEntryMock.getResource()).thenReturn(resource);

    scope = new MunitTestFlowScope(flowEntryMock, APIKIT_FLOW_NAME);
    scope.setCreateResourceFiles(true);

    List<Element> elements = scope.generateTests();
    assertSimilarityWithFile(elements, "get-resource-correct-file-name-for-response");

  }

  @Test
  public void testGetResourceElementWithCorrectFileNameForRequest() throws Exception {
    String matchingMimeType = "application/json";
    setRequestVerb("post");

    Resource resource = mock(Resource.class);
    when(resource.getUri()).thenReturn("/pet");
    when(flowEntryMock.getResource()).thenReturn(resource);
    when(actionMock.getType()).thenReturn(ActionType.POST);

    Map<String, MimeType> responseBody =
        buildMimeTypeMapWithExample(matchingMimeType, "{\"id\":\"a_id\",\"value\":\"a_value\"}");
    when(responseMock.getBody()).thenReturn(responseBody);
    when(flowEntryMock.getMimeType()).thenReturn(matchingMimeType);

    when(actionMock.getBody()).thenReturn(responseBody);

    scope = new MunitTestFlowScope(flowEntryMock, "apikitExampleFlow");
    scope.setCreateResourceFiles(true);

    List<Element> elements = scope.generateTests();
    assertSimilarityWithFile(elements, "get-resource-correct-file-name-for-request");

  }

  @Test
  public void testTestFlowNameWithoutCurlyBraces() throws Exception {
    setRequestVerb("get");

    Resource resource = mock(Resource.class);
    when(resource.getUri()).thenReturn("pet/{id}");
    when(flowEntryMock.getResource()).thenReturn(resource);
    when(flowEntryMock.getFlowName()).thenReturn("get:\\pet\\{id}");

    scope = new MunitTestFlowScope(flowEntryMock, "apikitExampleFlow");

    List<Element> elements = scope.generateTests();
    assertSimilarityWithFile(elements, "test-flow-name-without-curly-braces");
  }

  private void setRequestVerb(String verb) {
    when(flowEntryMock.getVerb()).thenReturn(verb.toUpperCase());
    when(flowEntryMock.getFlowName()).thenReturn(verb + ":\\pet:ApikitConfigName");
  }

  private Map<String, MimeType> buildMimeTypeMapWithExample(String mimeTypeName, String example) {
    MimeType mimeTypeMock = buildMimeTypeWithExample(mimeTypeName, example);

    Map<String, MimeType> body = new HashMap<>();
    body.put(mimeTypeName, mimeTypeMock);

    return body;
  }

  private MimeType buildMimeTypeWithExample(String mimeTypeName, String example) {
    MimeType mimeTypeMock = mock(MimeType.class);
    when(mimeTypeMock.getExample()).thenReturn(example);

    return mimeTypeMock;
  }

  private MimeType buildMimeTypeWithSchema(String mimeTypeName, String schema) {
    MimeType mimeTypeMock = mock(MimeType.class);
    when(mimeTypeMock.getType()).thenReturn(mimeTypeName);
    when(mimeTypeMock.getSchema()).thenReturn(schema);

    return mimeTypeMock;
  }

  private void assertSimilarityWithFile(List<Element> testElements, String fileName)
      throws SAXException, IOException, URISyntaxException {
    XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
    String actualXml = xout.outputString(testElements);

    URL expectedXmlUrl = this.getClass().getResource(RESOURCES_BASE_DIR + fileName + ".xml");
    FileReader expectedXmlReader = new FileReader(new File(expectedXmlUrl.toURI()));

    XMLUnit.setIgnoreWhitespace(true);
    Diff diff = XMLUnit.compareXML(expectedXmlReader, actualXml);
    assertTrue(diff.toString(), diff.similar());
  }
}
