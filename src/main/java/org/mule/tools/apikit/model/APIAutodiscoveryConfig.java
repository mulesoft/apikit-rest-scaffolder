/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mule.tools.apikit.output.NamespaceWithLocation;
import org.mule.tools.apikit.output.scopes.APIAutodiscoveryScope;

import org.mule.tools.apikit.output.scopes.Scope;

public class APIAutodiscoveryConfig implements Scope {

  public static final NamespaceWithLocation API_AUTODISCOVERY_NAMESPACE = new NamespaceWithLocation(
                                                                                                    Namespace
                                                                                                        .getNamespace("api-gateway",
                                                                                                                      "http://www.mulesoft.org/schema/mule/api-gateway"),
                                                                                                    "http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd");

  public static final String ELEMENT_NAME = "autodiscovery";
  public static final String API_ID_ATTRIBUTE = "apiId";
  public static final String IGNORE_BASE_PATH_ATTRIBUTE = "ignoreBasePath";
  public static final String IGNORE_BASE_PATH_DEFAULT = "true";
  public static final String FLOW_REF_ATTRIBUTE = "flowRef";

  private String apiId;
  private Boolean ignoreBasePath;
  private String flowRef;

  public APIAutodiscoveryConfig(String apiId, Boolean ignoreBasePath, String flowRef) {
    this.apiId = apiId;
    this.ignoreBasePath = ignoreBasePath;
    this.flowRef = flowRef;
  }

  public APIAutodiscoveryConfig() {

  }

  public String getApiId() {
    return apiId;
  }

  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  public Boolean getIgnoreBasePath() {
    return ignoreBasePath;
  }

  public void setIgnoreBasePath(Boolean ignoreBasePath) {
    this.ignoreBasePath = ignoreBasePath;
  }

  public String getFlowRef() {
    return flowRef;
  }

  public void setFlowRef(String flowRef) {
    this.flowRef = flowRef;
  }

  @Override
  public Element generate() {
    APIAutodiscoveryScope apiKitConfigScope = new APIAutodiscoveryScope(this);
    return apiKitConfigScope.generate();
  }
}
