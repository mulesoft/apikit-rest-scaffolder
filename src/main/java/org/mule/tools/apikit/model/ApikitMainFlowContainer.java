/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

public class ApikitMainFlowContainer {

  public static final String DEFAULT_HOST = "0.0.0.0";
  public static final int DEFAULT_PORT = 8081;
  public static final String DEFAULT_HOST_PLACEHOLDER = "${http.host}";
  public static final String DEFAULT_PORT_PLACEHOLDER = "${http.port}";
  public static final String DEFAULT_BASE_URI = "http://" + DEFAULT_HOST + ":" + DEFAULT_PORT + "/api";
  public static final String DEFAULT_BASE_PATH = "/";
  public static final String DEFAULT_PROTOCOL = "HTTP";
  public static final String DEFAULT_CONSOLE_PATH = "/console/*";

  private APIKitConfig config;
  private HttpListenerConfig httpListenerConfig;
  private String path;

  private String baseUri;
  private String apiFilePath;
  private String id;
  private MuleConfig muleConfig;

  public ApikitMainFlowContainer(String id, String apiFilePath, String baseUri, String path) {
    this.path = path;
    this.apiFilePath = apiFilePath;
    this.baseUri = baseUri;
    this.id = id;
  }


  public ApikitMainFlowContainer(String id, String apiFileName, String baseUri, String path, APIKitConfig config,
                                 MuleConfig muleConfig) {
    this(id, apiFileName, baseUri, path);
    this.config = config;
    this.muleConfig = muleConfig;
  }

  public String getApiFilePath() {
    return apiFilePath;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public HttpListenerConfig getHttpListenerConfig() {
    return httpListenerConfig;
  }

  public APIKitConfig getConfig() {
    return config;
  }

  public void setConfig(APIKitConfig config) {
    this.config = config;
  }

  public void setHttpListenerConfig(HttpListenerConfig httpListenerConfig) {
    this.httpListenerConfig = httpListenerConfig;
  }

  public void setDefaultAPIKitConfig() {
    config = new APIKitConfig();
    config.setApi(apiFilePath);
    config.setName(id + "-" + APIKitConfig.DEFAULT_CONFIG_NAME);
  }

  public String getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(String baseUri) {
    this.baseUri = baseUri;
  }

  public MuleConfig getMuleConfig() {
    return muleConfig;
  }

  public void setMuleConfig(MuleConfig muleConfig) {
    this.muleConfig = muleConfig;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    ApikitMainFlowContainer api = (ApikitMainFlowContainer) o;

    if (!apiFilePath.equals(api.apiFilePath) || !id.equals(api.id))
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    return apiFilePath.hashCode();
  }

  public void setApiFilePath(String apiFilePath) {
    this.apiFilePath = apiFilePath;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

}
