/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.tools.apikit.output.GenerationModel;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.mule.tools.apikit.misc.ExpressionUtils.wrapInExpression;
import static org.mule.tools.apikit.output.MuleConfigGenerator.HTTP_NAMESPACE;

public class HttpRequestScope implements Scope {

  private String uri;
  private String mimeType;
  private String statusCode;
  private GenerationModel flowEntry;


  public HttpRequestScope(GenerationModel flowEntry, String uri, String mimeType, String statusCode) {
    this.uri = uri;
    this.mimeType = mimeType;
    this.statusCode = statusCode;
    this.flowEntry = flowEntry;
  }

  public Element generate() {
    Element element = new Element("request", HTTP_NAMESPACE.getNamespace());
    element.setAttribute("config-ref", "HTTP_Request_Configuration");
    element.setAttribute("method", flowEntry.getVerb());
    element.setAttribute("path", uri);

    addHttpRequestParameters(element, flowEntry, mimeType);

    addSuccessStatusCodeValidator(element, statusCode);


    return element;
  }

  private void addHttpRequestParameters(Element requester, GenerationModel flowEntry, String mimeType) {
    addHttpRequestHeaders(requester, flowEntry, mimeType);
    addHttpRequestQueryParameters(requester, flowEntry);
  }

  private void addHttpRequestHeaders(Element request, GenerationModel flowEntry, String mimeType) {
    try {
      Map<String, String> headers = new HashMap<>();

      addAcceptHttpRequestHeaderParameter(headers, mimeType);
      addContentTypeHttpRequestHeaderParameter(headers, flowEntry.getVerb(), flowEntry.getMimeType());
      addHeaders(headers, flowEntry);

      if (!headers.isEmpty()) {
        Element element = new Element("headers", HTTP_NAMESPACE.getNamespace());
        element.addContent(wrapInExpression(new ObjectMapper().writeValueAsString(headers)));
        request.addContent(element);
      }
    } catch (Exception e) {

    }
  }

  private void addAcceptHttpRequestHeaderParameter(Map<String, String> headers, String mimeType) {
    if (StringUtils.isNotBlank(mimeType)) {
      addHttpRequestHeaderParameter(headers, "Accept", mimeType);
    }
  }

  private void addContentTypeHttpRequestHeaderParameter(Map<String, String> headers, String verb, String mimeType) {
    if (("POST".equals(verb) || "PUT".equals(verb) || "PATCH".equals(verb)) && StringUtils.isNotBlank(mimeType)) {
      addHttpRequestHeaderParameter(headers, "Content-Type", mimeType);
    }
  }

  private void addHeaders(Map<String, String> headers, GenerationModel flowEntry) {
    flowEntry.getAction().getHeaders().entrySet().stream().filter(header -> header.getValue().isRequired()).forEach(header -> {
      String exampleValue = header.getValue().getExample();
      String defaultValue = header.getValue().getDefaultValue();
      String value = exampleValue != null ? exampleValue : (defaultValue != null ? defaultValue : StringUtils.EMPTY);

      addHttpRequestHeaderParameter(headers, header.getKey(), value);
    });
  }

  private void addHttpRequestHeaderParameter(Map<String, String> headers, String header, String value) {
    if (StringUtils.isNotBlank(header)) {
      headers.put(header, value);
    }
  }

  private void addSuccessStatusCodeValidator(Element requester, String statusCode) {
    Element responseValidatorElement = new Element("response-validator", HTTP_NAMESPACE.getNamespace());
    Element successCodeElement = new Element("success-status-code-validator", HTTP_NAMESPACE.getNamespace());
    successCodeElement.setAttribute("values", statusCode);
    responseValidatorElement.addContent(successCodeElement);

    if (StringUtils.isNotBlank(statusCode) && Integer.valueOf(statusCode) >= 400) {
      requester.addContent(responseValidatorElement);
    }
  }

  private Boolean addHttpRequestQueryParameters(Element request, GenerationModel flowEntry) {
    try {
      Map<String, Parameter> queryParameters = flowEntry.getAction().getQueryParameters();
      Element element = new Element("query-params", HTTP_NAMESPACE.getNamespace());

      if (null != queryParameters && !queryParameters.isEmpty()) {
        Map<String, Object> queryParams = queryParameters.entrySet().stream()
            .filter((queryParam) -> queryParam.getValue().isRequired())
            .collect(toMap(Map.Entry::getKey, this::getQueryParameterValue));

        element.addContent(wrapInExpression(new ObjectMapper().writeValueAsString(queryParams)));

        request.addContent(element);
        return true;
      }
    } catch (Exception e) {

    }
    return false;
  }

  private Object getQueryParameterValue(Map.Entry<String, Parameter> queryParameter) {
    String exampleValue = getParameterExampleValue(queryParameter.getValue());
    String defaultValue = queryParameter.getValue().getDefaultValue();
    String value = exampleValue != null ? exampleValue : defaultValue;
    if (StringUtils.isBlank(value)) {
      return StringUtils.EMPTY;
    }
    return value.trim();
  }

  private String getParameterExampleValue(Parameter parameter) {
    String example = parameter.getExample();
    if (StringUtils.isBlank(example) && !parameter.getExamples().isEmpty()) {
      example = parameter.getExamples().values().iterator().next();
    }
    return example;
  }
}
