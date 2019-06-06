/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.jdom2.Element;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.output.scopes.HttpListenerConfigScope;
import org.mule.tools.apikit.output.scopes.Scope;

import java.util.Objects;

import static org.mule.tools.apikit.model.ApikitMainFlowContainer.DEFAULT_BASE_PATH;
import static org.mule.tools.apikit.model.ApikitMainFlowContainer.DEFAULT_HOST;
import static org.mule.tools.apikit.model.ApikitMainFlowContainer.DEFAULT_PORT;
import static org.mule.tools.apikit.model.ApikitMainFlowContainer.DEFAULT_PROTOCOL;

public class HttpListenerConfig implements Scope {

  public static final String ELEMENT_NAME = "listener-config";
  public static final String DEFAULT_CONFIG_NAME = "httpListenerConfig";

  private String name;
  private String basePath;
  private HttpListenerConnection connection;
  private boolean isPeristed = false;

  public HttpListenerConfig(final String name,
                            final String baseUri) {
    this.name = name;
    String host = APIKitTools.getHostFromUri(baseUri);
    String port = APIKitTools.getPortFromUri(baseUri);
    String protocol = APIKitTools.getProtocolFromUri(baseUri);
    this.basePath = APIKitTools.getPathFromUri(baseUri, false);
    this.connection = new HttpListenerConnection.Builder(host, port, protocol).build();
  }

  public HttpListenerConfig(final String name) {
    this(name, DEFAULT_BASE_PATH,
         new HttpListenerConnection.Builder(DEFAULT_HOST, String.valueOf(DEFAULT_PORT), DEFAULT_PROTOCOL).build());
  }

  public HttpListenerConfig(final String name,
                            final String host,
                            final String port,
                            final String protocol,
                            final String basePath) {
    this(name, basePath, new HttpListenerConnection.Builder(host, port, protocol).build());
  }

  public HttpListenerConfig(final String name,
                            final String basePath,
                            final HttpListenerConnection httpListenerConnection) {
    this.name = name;
    this.basePath = basePath;
    this.connection = httpListenerConnection;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHost() {
    return connection.getHost();
  }

  public String getPort() {
    return connection.getPort();
  }

  public String getBasePath() {
    return basePath;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  public boolean isPeristed() {
    return isPeristed;
  }

  public void setPeristed(boolean isGenerated) {
    this.isPeristed = isGenerated;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    HttpListenerConfig that = (HttpListenerConfig) o;
    return isPeristed == that.isPeristed &&
        Objects.equals(name, that.name) &&
        Objects.equals(basePath, that.basePath) &&
        Objects.equals(connection, that.connection);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, basePath, connection, isPeristed);
  }

  public Element generate(){
    HttpListenerConfigScope httpListenerScope = new HttpListenerConfigScope(this);
    return httpListenerScope.generate();
  }
}
