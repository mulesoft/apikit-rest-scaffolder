/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import static org.mule.tools.apikit.output.MuleConfigGenerator.HTTP_NAMESPACE;

import org.mule.tools.apikit.model.HttpListenerConfig;

import org.jdom2.Element;


public class HttpListenerConfigScope implements Scope {

  private final Element httpListenerConfig;

  public HttpListenerConfigScope(HttpListenerConfig httpListenerConfig) {
    if (httpListenerConfig != null) {
      this.httpListenerConfig = new Element(HttpListenerConfig.ELEMENT_NAME, HTTP_NAMESPACE.getNamespace());
      this.httpListenerConfig.setAttribute("name", httpListenerConfig.getName());
      String basePath = httpListenerConfig.getBasePath();
      if (basePath != null && basePath != "/" && basePath != "") {
        this.httpListenerConfig.setAttribute("basePath", httpListenerConfig.getBasePath());
      }
      Element connection = new Element("listener-connection", HTTP_NAMESPACE.getNamespace());
      connection.setAttribute("host", httpListenerConfig.getHost());
      connection.setAttribute("port", httpListenerConfig.getPort());
      this.httpListenerConfig.addContent(connection);
      httpListenerConfig.setPersisted(true);
    } else {
      this.httpListenerConfig = null;
    }
  }

  @Override
  public Element generate() {
    return httpListenerConfig;
  }
}
