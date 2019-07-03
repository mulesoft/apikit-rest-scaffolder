/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import org.jdom2.Element;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;

import static org.mule.tools.apikit.output.MuleConfigGenerator.HTTP_NAMESPACE;

public class HttpRequestConfigScope implements Scope {

  private ApikitMainFlowContainer api;
  private final String APIKIT_DEFAULT_BASE_URI = "0.0.0.0";
  private final String REQUEST_DEFAULT_BASE_URI = "localhost";

  public HttpRequestConfigScope(ApikitMainFlowContainer api) {
    this.api = api;
  }

  @Override
  public Element generate() {
    Element element = new Element("request-config", HTTP_NAMESPACE.getNamespace());
    element.setAttribute("name", "HTTP_Request_Configuration");


    String uri = api.getBaseUri().replace(APIKIT_DEFAULT_BASE_URI, REQUEST_DEFAULT_BASE_URI);
    String hostWithOutProtocol = uri.split("http://")[1];
    String hostAndPort = hostWithOutProtocol.substring(0, hostWithOutProtocol.indexOf("/"));
    String host = hostAndPort.substring(0, hostAndPort.indexOf(":"));
    String port = hostAndPort.substring(hostAndPort.indexOf(":") + 1);
    String path = hostWithOutProtocol.substring(hostWithOutProtocol.indexOf("/"));

    Element connection = new Element("request-connection", HTTP_NAMESPACE.getNamespace());
    connection.setAttribute("host", host);
    connection.setAttribute("port", port);

    element.setAttribute("basePath", path);
    element.addContent(connection);

    return element;
  }
}
