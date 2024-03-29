/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import org.jdom2.CDATA;
import org.jdom2.Element;

import java.util.Arrays;
import java.util.List;

import static org.mule.tools.apikit.output.MuleConfigGenerator.DOC_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.EE_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;

public class ErrorHandlerScope implements Scope {

  private Element errorHandler;

  public static ErrorHandlerScope createForConsoleFlow(boolean isMuleEE) {
    List<StatusCodeMapping> statusCodeMappings = Arrays.asList(
                                                               new StatusCodeMapping(404, "APIKIT:NOT_FOUND",
                                                                                     "Resource not found"));

    return new ErrorHandlerScope(statusCodeMappings, isMuleEE);
  }

  public static ErrorHandlerScope createForMainFlow(boolean isMuleEE) {
    List<StatusCodeMapping> statusCodeMappings = Arrays.asList(
                                                               new StatusCodeMapping(400, "APIKIT:BAD_REQUEST", "Bad request"),
                                                               new StatusCodeMapping(404, "APIKIT:NOT_FOUND",
                                                                                     "Resource not found"),
                                                               new StatusCodeMapping(405, "APIKIT:METHOD_NOT_ALLOWED",
                                                                                     "Method not allowed"),
                                                               new StatusCodeMapping(406, "APIKIT:NOT_ACCEPTABLE",
                                                                                     "Not acceptable"),
                                                               new StatusCodeMapping(415, "APIKIT:UNSUPPORTED_MEDIA_TYPE",
                                                                                     "Unsupported media type"),
                                                               new StatusCodeMapping(501, "APIKIT:NOT_IMPLEMENTED",
                                                                                     "Not Implemented"));

    return new ErrorHandlerScope(statusCodeMappings, isMuleEE);
  }

  private ErrorHandlerScope(List<StatusCodeMapping> statusCodeMappings, boolean isMuleEE) {
    createErrorHandlerElement(statusCodeMappings, isMuleEE);
  }

  private void createErrorHandlerElement(List<StatusCodeMapping> statusCodeMappings, boolean isMuleEE) {
    errorHandler = new Element("error-handler", XMLNS_NAMESPACE.getNamespace());

    for (StatusCodeMapping statusCodeMapping : statusCodeMappings) {
      Element errorMapping = new Element("on-error-propagate", XMLNS_NAMESPACE.getNamespace());
      errorMapping.setAttribute("type", statusCodeMapping.getErrorType());

      if (isMuleEE) {
        generateErrorHandlingForEE(statusCodeMapping, errorMapping);
      } else {
        generateErrorHandlingForCE(statusCodeMapping, errorMapping);
      }

      errorHandler.addContent(errorMapping);
    }
  }

  private void generateErrorHandlingForCE(StatusCodeMapping statusCodeMapping, Element errorMapping) {
    // Content-type
    Element contentTypeHeader = new Element("set-variable", XMLNS_NAMESPACE.getNamespace());
    contentTypeHeader.setAttribute("variableName", "outboundHeaders");
    contentTypeHeader.setAttribute("value", "#[{'Content-Type':'application/json'}]");
    errorMapping.addContent(contentTypeHeader);

    // Payload
    Element setPayload = new Element("set-payload", XMLNS_NAMESPACE.getNamespace());
    setPayload.setAttribute("value", "{ \"message\": \"" + statusCodeMapping.getMessage() + "\" }");
    errorMapping.addContent(setPayload);

    // Variables
    Element statusCodeVariable = new Element("set-variable", XMLNS_NAMESPACE.getNamespace());
    statusCodeVariable.setAttribute("variableName", "httpStatus");
    statusCodeVariable.setAttribute("value", statusCodeMapping.getStatusCode());
    errorMapping.addContent(statusCodeVariable);
  }

  private void generateErrorHandlingForEE(StatusCodeMapping statusCodeMapping, Element errorMapping) {
    // Transform Element
    Element transform = new Element("transform", EE_NAMESPACE.getNamespace());
    transform.addNamespaceDeclaration(EE_NAMESPACE.getNamespace());
    transform.setAttribute("name", "Transform Message", DOC_NAMESPACE.getNamespace());

    // Payload
    Element message = new Element("message", EE_NAMESPACE.getNamespace());
    Element setPayload = new Element("set-payload", EE_NAMESPACE.getNamespace());
    CDATA cDataSection = new CDATA(getTransformText(statusCodeMapping.getMessage()));

    // Variables
    Element variables = new Element("variables", EE_NAMESPACE.getNamespace());
    Element statusCodeVariable = new Element("set-variable", EE_NAMESPACE.getNamespace());
    statusCodeVariable.setAttribute("variableName", "httpStatus");

    setPayload.addContent(cDataSection);
    statusCodeVariable.addContent(statusCodeMapping.getStatusCode());
    message.addContent(setPayload);
    variables.addContent(statusCodeVariable);

    transform.addContent(message);
    transform.addContent(variables);

    errorMapping.addContent(transform);
  }

  @Override
  public Element generate() {
    return errorHandler;
  }

  private String getTransformText(String message) {
    return "%dw 2.0\n" +
        "output application/json\n" +
        "---\n" +
        "{message: \"" + message + "\"}\n";
  }

  public static class StatusCodeMapping {

    private final int statusCode;
    private final String errorType;
    private final String message;

    public StatusCodeMapping(int statusCode, String errorType, String message) {
      this.statusCode = statusCode;
      this.errorType = errorType;
      this.message = message;
    }

    public String getStatusCode() {
      return Integer.toString(statusCode);
    }

    public String getErrorType() {
      return errorType;
    }

    public String getMessage() {
      return message;
    }
  }
}
