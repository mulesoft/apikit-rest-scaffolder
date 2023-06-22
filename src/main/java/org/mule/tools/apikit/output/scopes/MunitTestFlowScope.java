/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.mule.apikit.model.ActionType;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.tools.apikit.misc.FlowNameUtils;
import org.mule.tools.apikit.misc.TestResourceNameGenerator;
import org.mule.tools.apikit.model.ScaffolderResource;
import org.mule.tools.apikit.output.GenerationModel;
import org.mule.tools.apikit.schemas.JsonSchemaDataGenerator;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.strip;
import static org.mule.tools.apikit.misc.DataWeaveExpressionUtils.wrapInEqualTo;
import static org.mule.tools.apikit.misc.DataWeaveExpressionUtils.wrapInStringExpression;
import static org.mule.tools.apikit.misc.DataWeaveExpressionUtils.wrapInWriteToString;
import static org.mule.tools.apikit.output.MuleConfigGenerator.DOC_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;
import static org.mule.tools.apikit.output.MunitTestSuiteGenerator.MUNIT_NAMESPACE;
import static org.mule.tools.apikit.output.MunitTestSuiteGenerator.MUNIT_TOOLS_NAMESPACE;

public class MunitTestFlowScope {

  private static final String GET_RESOURCE_WRAPPER = "MunitTools::getResourceAsString('%s')";
  private static final String REQUEST_DIRECTORY = "request";
  private static final String RESPONSE_DIRECTORY = "response";

  private GenerationModel flowEntry;

  private String apikitFlowName;
  private boolean createResourceFiles;
  private Set<ScaffolderResource> generatedResources;

  public MunitTestFlowScope(GenerationModel flowEntry, String apikitFlowName) {
    this.flowEntry = flowEntry;
    this.apikitFlowName = apikitFlowName;
    generatedResources = new HashSet<>();
  }

  @Deprecated
  public Element generate() {
    return new Element("test", MUNIT_NAMESPACE.getNamespace());
  }

  public List<Element> generateTests() {
    List<Element> elements = new ArrayList<>();

    Map<String, Response> flowIResponses = flowEntry.getAction().getResponses();
    for (Map.Entry<String, Response> responseEntry : flowIResponses.entrySet()) {
      String statusCode = responseEntry.getKey();

      Map<String, MimeType> responseBodies = responseEntry.getValue().getBody();
      if (null != responseBodies && !responseBodies.isEmpty()) {
        for (Map.Entry<String, MimeType> body : responseBodies.entrySet()) {
          String mimeType = body.getKey();
          String examplePayload = buildExamplePayload(body.getValue());

          Element testFlow = generateTest(flowEntry, statusCode, mimeType, examplePayload);

          elements.add(testFlow);
        }
      } else {
        Element testFlow = generateTest(flowEntry, statusCode, "", "");
        elements.add(testFlow);
      }
    }

    if (flowIResponses.isEmpty()) {
      Element testFlow = generateTest(flowEntry, "", "", "");
      elements.add(testFlow);
    }

    return elements;
  }

  public void setCreateResourceFiles(boolean createResourceFiles) {
    this.createResourceFiles = createResourceFiles;
  }

  private String buildExamplePayload(MimeType response) {
    String examplePayload = response.getExample() == null ? StringUtils.EMPTY : response.getExample();

    if (isBlank(examplePayload)) {
      String schemaString = response.getSchema();
      if (StringUtils.isNotBlank(schemaString) && "application/json".equals(response.getType())) {
        JsonParser parser = new JsonParser();
        try {
          // TODO: If we can cast then it wasn't a valid json we require features from the parser
          JsonObject jsonObj = (JsonObject) parser.parse(schemaString);
          return new JsonSchemaDataGenerator().buildExamplePayloadFromJsonSchema(jsonObj);
        } catch (ClassCastException e) {
          return StringUtils.EMPTY;
        }
      }

      if (StringUtils.isNotBlank(schemaString) && "application/xml".equals(response.getType())) {
        return StringUtils.EMPTY;
      }
    } else if ("application/json".equals(response.getType())) {
      return transformYamlExampleIntoJSON(examplePayload);
    }

    return examplePayload;
  }

  private static String transformYamlExampleIntoJSON(String example) {
    try {
      Yaml yaml = new Yaml();
      Object yamlObject = yaml.load(example);
      return new ObjectMapper().disableDefaultTyping().writeValueAsString(yamlObject);

    } catch (Throwable e) {
      // If example couldn't have been processed, we return a null JSON.
      return example;
    }
  }

  private Element generateTest(GenerationModel flowEntry, String statusCode, String mimeType, String examplePayload) {

    Element testFlow = generateMunitTestFlow(flowEntry, statusCode, mimeType);

    generateEnableFlowSources(flowEntry, testFlow);

    String uri = generateBehaviorSection(flowEntry, testFlow);

    generateExecutionSection(flowEntry, statusCode, mimeType, testFlow, uri);

    generateValidationSection(flowEntry, statusCode, mimeType, examplePayload, testFlow);

    return testFlow;
  }

  private void generateEnableFlowSources(GenerationModel flowEntry, Element testFlow) {
    Element enableFlowSources = new Element("enable-flow-sources", MUNIT_NAMESPACE.getNamespace());
    Element enableApikitFlow = new Element("enable-flow-source", MUNIT_NAMESPACE.getNamespace());
    enableApikitFlow.setAttribute("value", apikitFlowName);
    Element enableTestedFlow = new Element("enable-flow-source", MUNIT_NAMESPACE.getNamespace());
    enableTestedFlow.setAttribute("value", flowEntry.getFlowName());

    enableFlowSources.addContent(enableApikitFlow);
    enableFlowSources.addContent(enableTestedFlow);

    testFlow.addContent(enableFlowSources);
  }

  private String generateBehaviorSection(GenerationModel flowEntry, Element flow) {
    Element behavior = new Element("behavior", MUNIT_NAMESPACE.getNamespace());
    if (null != flowEntry.getAction() && isUpdateAction(flowEntry.getAction().getType())) {
      addRequestSetPayload(behavior, flowEntry);
    }
    String uri = addUriParametersVariables(behavior, flowEntry);
    if (!behavior.getChildren().isEmpty()) {
      flow.addContent(behavior);
    }
    return uri;
  }

  private void generateExecutionSection(GenerationModel flowEntry, String statusCode, String mimeType, Element flow,
                                        String uri) {
    Element execution = new Element("execution", MUNIT_NAMESPACE.getNamespace());
    addHttpRequest(execution, flowEntry, uri, statusCode, mimeType);
    if (!execution.getChildren().isEmpty()) {
      flow.addContent(execution);
    }
  }

  private void generateValidationSection(GenerationModel flowEntry, String statusCode, String mimeType, String examplePayload,
                                         Element flow) {
    Element validation = new Element("validation", MUNIT_NAMESPACE.getNamespace());

    addAssertTrueStatusCode(validation, statusCode);

    if (!isBlank(mimeType)) {
      addAssertOnEqualsPayload(validation, flowEntry, examplePayload, mimeType, statusCode);
    }
    if (!validation.getChildren().isEmpty()) {
      flow.addContent(validation);
    }
  }

  private Element generateMunitTestFlow(GenerationModel flowEntry, String statusCode, String mimeType) {
    String name = generateFlowName(flowEntry, mimeType, statusCode);
    Element flow = new Element("test", MUNIT_NAMESPACE.getNamespace());
    flow.setAttribute("name", name + "-FlowTest");
    flow.setAttribute("description", "Verifying functionality of [" + name + "]");

    return flow;
  }

  private String generateFlowName(GenerationModel flowEntry, String mimeType, String statusCode) {
    String flowName = flowEntry.getFlowName();

    if (flowEntry.getMimeType() != null) {
      String configName = flowEntry.getApi().getConfig().getName();
      String flowMimeType = flowEntry.getMimeType().replace("/", "\\");
      String expectedEndsWith = String.format("%s:%s", flowMimeType, configName);

      if (!flowName.endsWith(expectedEndsWith)) {
        flowName = flowEntry.getFlowName().replace(configName, expectedEndsWith);
      }
    }

    String name = flowName + "-" + statusCode + "-" + mimeType;
    return FlowNameUtils.encode(name);
  }

  private String addUriParametersVariables(Element flow, GenerationModel flowEntry) {
    Resource resource = flowEntry.getAction().getResource();
    return generateUriParameters(resource, flow);
  }

  private String generateUriParameters(Resource resource, Element flow) {
    if (null == resource.getParentUri()) {
      return resource.getRelativeUri();
    }

    String uri = resource.getUri();
    try {
      Map<String, Parameter> uriParameters = resource.getResolvedUriParameters();
      if (null != uriParameters && !uriParameters.isEmpty()) {
        for (Map.Entry<String, Parameter> uriParameterEntry : uriParameters.entrySet()) {
          String parameterName = uriParameterEntry.getKey();
          String parameterValue = uriParameterEntry.getValue().getDefaultValue();
          if (isBlank(parameterValue)) {
            parameterValue = uriParameterEntry.getValue().getExample();
          }

          Element element = buildSetVariable(parameterName, parameterValue);
          flow.addContent(element);

          uri = replaceUriParamsWithVariables(uri, parameterName);
        }
        return wrapInStringExpression(uri);
      }
    } catch (Exception e) {

    }

    return uri;
  }

  private Element buildSetVariable(String name, String value) {
    Element element = new Element("set-variable", XMLNS_NAMESPACE.getNamespace());
    element.setAttribute("variableName", name);
    element.setAttribute("value", wrapInStringExpression(value));
    element.setAttribute("name", name, DOC_NAMESPACE.getNamespace());
    return element;
  }

  private String replaceUriParamsWithVariables(String uri, String parameterName) {
    String uriParamSegment = "{" + parameterName + "}";
    return uri.replace(uriParamSegment, "$(vars." + parameterName + ")");
  }

  private void addRequestSetPayload(Element flow, GenerationModel flowEntry) {
    Map<String, MimeType> requests = flowEntry.getAction().getBody();
    String requestType = flowEntry.getMimeType();
    if (null != requests && !requests.isEmpty()) {
      MimeType request;
      if (requests.containsKey(requestType)) {
        request = requests.get(requestType);
      } else {
        // Random right :P
        request = requests.get(requests.keySet().iterator().next());
      }

      String requestPayload = buildExamplePayload(request);
      addSetPayload(flow, buildSetPayloadContent(flowEntry, requestPayload, requestType));
    }
  }

  private String buildSetPayloadContent(GenerationModel flowEntry, String requestPayload, String requestType) {
    String setPayloadContent = String.format("'%s'", requestPayload);
    if (createResourceFiles) {
      String fileName = TestResourceNameGenerator.generate(flowEntry, requestType, "");
      ScaffolderResource resource = new ScaffolderResource(REQUEST_DIRECTORY, fileName, IOUtils.toInputStream(requestPayload));
      generatedResources.add(resource);
      setPayloadContent = buildGetResourceFunction(resource);
    }

    return "#[" + setPayloadContent + "]";
  }

  private String buildGetResourceFunction(ScaffolderResource resource) {
    String relativePath = join("/", "scaffolder", strip(resource.getDirectory(), "/"), resource.getName());
    return String.format(GET_RESOURCE_WRAPPER, relativePath);
  }

  private void addSetPayload(Element flow, String payload) {
    Element element = new Element("set-payload", XMLNS_NAMESPACE.getNamespace());

    element.setAttribute("value", payload);

    flow.addContent(element);
  }

  private void addHttpRequest(Element flow, GenerationModel flowEntry, String uri, String statusCode, String mimeType) {
    HttpRequestScope requestScope = new HttpRequestScope(flowEntry, uri, mimeType, statusCode);
    flow.addContent(requestScope.generate());
  }

  private void addAssertTrueStatusCode(Element flow, String statusCode) {
    Element element = new Element("assert-that", MUNIT_TOOLS_NAMESPACE.getNamespace());
    element.setAttribute("expression", "#[attributes.statusCode]");
    element.setAttribute("is", wrapInEqualTo(statusCode));
    element.setAttribute("message", "The HTTP Status code is not correct!");

    String displayName = "Assert That Status Code is " + statusCode;
    element.setAttribute("name", displayName, DOC_NAMESPACE.getNamespace());

    flow.addContent(element);
  }

  private void addAssertOnEqualsPayload(Element flow, GenerationModel flowEntry, String expectedPayload, String mimeType,
                                        String statusCode) {
    Element element = new Element("assert-that", MUNIT_TOOLS_NAMESPACE.getNamespace());

    expectedPayload = isBlank(expectedPayload) ? "" : expectedPayload.trim();
    element.setAttribute("expression", wrapInWriteToString("payload", mimeType));
    element.setAttribute("is", wrapInEqualTo(buildExpectedContent(flowEntry, expectedPayload, mimeType, statusCode)));
    element.setAttribute("message", "The response payload is not correct!");

    String displayName = "Assert That - Payload is Expected";
    element.setAttribute("name", displayName, DOC_NAMESPACE.getNamespace());

    flow.addContent(element);
  }

  private String buildExpectedContent(GenerationModel flowEntry, String payload, String mimeType, String statusCode) {
    String expectedContent = String.format("'%s'", payload);
    if (createResourceFiles) {
      String fileName = TestResourceNameGenerator.generate(flowEntry, mimeType, statusCode);
      ScaffolderResource resource = new ScaffolderResource(RESPONSE_DIRECTORY, fileName, IOUtils.toInputStream(payload));
      generatedResources.add(resource);
      expectedContent = buildGetResourceFunction(resource);
    }
    return expectedContent;
  }

  private boolean isUpdateAction(ActionType type) {
    return (ActionType.POST.equals(type) || ActionType.PUT.equals(type) || ActionType.PATCH.equals(type));
  }

  public Set<ScaffolderResource> getGeneratedResources() {
    return Collections.unmodifiableSet((generatedResources));
  }

}
