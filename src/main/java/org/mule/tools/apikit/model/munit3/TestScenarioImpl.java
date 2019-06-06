/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model.munit3;

import amf.client.model.domain.EndPoint;
import amf.client.model.domain.Operation;
import amf.client.model.domain.Payload;
import amf.client.model.domain.Response;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

public class TestScenarioImpl implements TestScenario {

  private static final String FLOW_NAME_SEPARATOR = ":";
  private static final String TEST_NAME_SEPARATOR = "-";

  private String name;
  private String description;
  private TestRequest request;
  private TestResponse response;
  private Set<String> flows;

  // TODO Check if this will still be needed MUSP-591
  private String flowName;

  public TestScenarioImpl(EndPoint endPoint, Operation operation, @Nullable Response response,
                                  @Nullable Payload requestPayload, @Nullable Payload responsePayload,
                                  // TODO Check if this will still be needed MUSP-591
                                  String apikitFlowName, String apiName) {
        checkArgument(endPoint != null, "EndPoint cannot be null");
        checkArgument(operation != null, "Operation cannot be null");

        setName(endPoint, operation, response, requestPayload, responsePayload, apiName);
        setDescription();
        setRequest(endPoint, operation, requestPayload, responsePayload);
        setResponse(response, responsePayload);
        setFlows(apikitFlowName);
    }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TestRequest getRequest() {
    return request;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TestResponse getResponse() {
    return response;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getFlows() {
    return flows;
  }

  private void setName(EndPoint endPoint, Operation operation, Response response, Payload requestPayload, Payload responsePayload,
                       String apiName) {
    StringBuilder testName = new StringBuilder();
    String statusCode = ofNullable(response).map(res -> res.statusCode().value()).orElse(StringUtils.EMPTY);
    String responseContentType = ofNullable(responsePayload).map(payl -> payl.mediaType().value()).orElse(StringUtils.EMPTY);
    this.flowName = getFlowName(endPoint, operation, requestPayload, apiName);
    this.name = testName.append(flowName).append(TEST_NAME_SEPARATOR)
        .append(statusCode).append(TEST_NAME_SEPARATOR)
        .append(responseContentType).append(TEST_NAME_SEPARATOR)
        .append("FlowTest").toString();
  }

  private void setDescription() {
    this.description = "Verifying functionality of [" + flowName + "]";
  }

  private void setFlows(String apikitFlowName) {
    this.flows = new TreeSet<>(asList(apikitFlowName, flowName));
  }

  private void setResponse(Response response, Payload responsePayload) {
    this.response = new TestResponseImpl(response, responsePayload);
  }

  private void setRequest(EndPoint endPoint, Operation operation, Payload requestPayload, Payload responsePayload) {
    this.request = new TestRequestImpl(endPoint, operation, requestPayload, responsePayload);
  }

  /**
   * This method tries to generate the same name APIKit uses when generating flow names
   *
   * TODO Check if this will still be needed MUSP-591
   */
  private String getFlowName(EndPoint endPoint, Operation operation, Payload requestPayload, String apiName) {
    String contentType = ofNullable(requestPayload).map(payl -> payl.mediaType().value()).orElse(StringUtils.EMPTY);

    StringBuilder flowName = new StringBuilder();
    flowName.append(operation.method().option().map(String::toLowerCase).orElse(StringUtils.EMPTY))
        .append(FLOW_NAME_SEPARATOR)
        .append(endPoint.path().option().orElse(StringUtils.EMPTY))
        .append(FLOW_NAME_SEPARATOR).append(apiName).append("-config");

    if (!StringUtils.isBlank(contentType)) {
      flowName.append(FLOW_NAME_SEPARATOR).append(contentType);
    }

    return flowName.toString();
  }
}
