/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model.munit3;

import amf.client.model.domain.*;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

public class TestRequestImpl implements TestRequest {

  protected static final String ACCEPT_HEADER = "Accept";
  protected static final String CONTENT_TYPE_HEADER = "Content-Type";

  private String path;
  private String method;
  private Map<String, Object> headers = new HashMap<>();
  private Map<String, Object> uriParameters = new HashMap<>();
  private Map<String, Object> queryParameters = new HashMap<>();
  private Object content;
  private String contentType;

  public TestRequestImpl(EndPoint endPoint, Operation operation, @Nullable Payload requestPayload,
                         @Nullable Payload responsePayload) {
    checkArgument(endPoint != null, "EndPoint cannot be null");
    checkArgument(operation != null, "Operation cannot be null");

    this.path = endPoint.path().value();
    this.method = operation.method().value();
    this.contentType =
        ofNullable(requestPayload).map(Payload::mediaType).map(mediaType -> mediaType.option().orElse(null)).orElse(null);
    setUriParameters(endPoint.parameters());
    setHeaders(operation.request(), requestPayload, responsePayload);
    setQueryParameters(operation.request());
    ofNullable(requestPayload).ifPresent(this::setContent);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getPath() {
    return path;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMethod() {
    return method;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Object> getHeaders() {
    return headers;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Object> getUriParameters() {
    return uriParameters;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Object> getQueryParameters() {
    return queryParameters;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getContentType() {
    return ofNullable(contentType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<Object> getContent() {
    return ofNullable(content);
  }

  private void setUriParameters(List<Parameter> parameters) {
    // this.uriParameters = parameters.stream()
    // .filter(this::isRequired)
    // .collect(toMap(this::paramName, ExampleUtils::getParameterValue));
  }

  private void setHeaders(@Nullable Request request, @Nullable Payload requestPayload, @Nullable Payload responsePayload) {
    ofNullable(responsePayload)
        .ifPresent(content -> content.mediaType().option().ifPresent(mediaType -> headers.put(ACCEPT_HEADER, mediaType)));
    ofNullable(requestPayload)
        .ifPresent(content -> content.mediaType().option().ifPresent(mediaType -> headers.put(CONTENT_TYPE_HEADER, mediaType)));
    // ofNullable(request).ifPresent(req -> {
    // headers.putAll(request.headers().stream().filter(this::isRequired)
    // .collect(toMap(this::paramName, ExampleUtils::getParameterValue)));
    // });
  }

  public void setQueryParameters(Request request) {
    // ofNullable(request).ifPresent(req -> {
    // this.queryParameters = request.queryParameters().stream()
    // .filter(this::isRequired)
    // .collect(toMap(this::paramName, ExampleUtils::getParameterValue));
    // });
  }

  private void setContent(Payload payload) {
    // this.content = buildPayload(payload.schema(), payload.mediaType().option().orElse(null));
  }

  private String paramName(Parameter parameter) {
    return parameter.name().value();
  }

  private boolean isRequired(Parameter parameter) {
    return parameter.required().value();
  }
}
