/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model.munit3;

import amf.client.model.domain.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TestSuiteImpl implements TestSuite {

  public static final int DEFAULT_PORT = 8081;
  public static final String DEFAULT_BASE_URI = "http://localhost:" + DEFAULT_PORT + "/api";

  private String name;
  private String basePath;
  private int port;
  private String host;
  private Set<TestScenario> testScenarios = new HashSet<>();

  // TODO Check if this will still be needed MUSP-591
  private String apikitFlowName;
  private String apiName;

  public TestSuiteImpl(String name, String apikitFlowName, WebApi webApi, String apiName) {
    checkArgument(isNotBlank(name), "Name cannot be blank");
    checkArgument(isNotBlank(apikitFlowName), "APIKit flow name cannot be blank");
    checkArgument(webApi != null, "Web ApikitMainFlowContainer cannot be null");
    checkArgument(isNotBlank(apiName), "ApikitMainFlowContainer name cannot be blank");

    this.name = name;
    this.apikitFlowName = apikitFlowName;
    this.apiName = apiName;
    setHttpParameters(DEFAULT_BASE_URI);
    setApiTestScenarios(webApi.endPoints());
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
  public String getBasePath() {
    return basePath;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getPort() {
    return port;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getHost() {
    return host;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<TestScenario> getTestScenarios() {
    return testScenarios;
  }

  private void setHttpParameters(String uri) {
    String hostWithOutProtocol = uri.split("http://")[1];
    String hostAndPort = hostWithOutProtocol.substring(0, hostWithOutProtocol.indexOf("/"));

    this.host = hostAndPort.substring(0, hostAndPort.indexOf(":"));
    this.port = Integer.valueOf(hostAndPort.substring(hostAndPort.indexOf(":") + 1));
    this.basePath = hostWithOutProtocol.substring(hostWithOutProtocol.indexOf("/"));
  }

  private void setApiTestScenarios(List<EndPoint> endPoints) {
    for (EndPoint endPoint : endPoints) {
      for (Operation operation : endPoint.operations()) {
        setApiTestScenariosForOperation(endPoint, operation);
      }
    }
  }

  private void setApiTestScenariosForOperation(EndPoint endPoint, Operation operation) {
    Request request = operation.request();
    if (request == null || request.payloads().isEmpty()) {
      setApiTestScenariosForResponse(endPoint, operation, null);
    } else {
      for (Payload requestPayload : request.payloads()) {
        setApiTestScenariosForResponse(endPoint, operation, requestPayload);
      }
    }

  }

  private void setApiTestScenariosForResponse(EndPoint endPoint, Operation operation, Payload requestPayload) {
    if (operation.responses().isEmpty()) {
      addApiTestScenario(endPoint, operation, null, requestPayload, null);
    } else {
      for (Response response : operation.responses()) {
        if (response.payloads().isEmpty()) {
          addApiTestScenario(endPoint, operation, response, requestPayload, null);
        } else {
          for (Payload responsePayload : response.payloads()) {
            addApiTestScenario(endPoint, operation, response, requestPayload, responsePayload);
          }
        }
      }
    }
  }

  private void addApiTestScenario(EndPoint endPoint, Operation operation, Response response, Payload requestPayload,
                                  Payload responsePayload) {
    this.testScenarios
        .add(new TestScenarioImpl(endPoint, operation, response, requestPayload, responsePayload, apikitFlowName, apiName));
  }
}
