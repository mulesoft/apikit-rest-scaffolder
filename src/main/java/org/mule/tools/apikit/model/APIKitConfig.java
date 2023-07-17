/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.mule.tools.apikit.output.scopes.APIKitConfigScope;
import org.mule.tools.apikit.output.scopes.Scope;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class APIKitConfig implements Scope {

  public static final String ELEMENT_NAME = "config";
  public static final String NAME_ATTRIBUTE = "name";
  public static final String API_ATTRIBUTE = "api";
  public static final String RAML_ATTRIBUTE = "raml";
  public static final String EXTENSION_ENABLED_ATTRIBUTE = "extensionEnabled";
  public static final String DEFAULT_CONFIG_NAME = "config";
  public static final String OUTBOUND_HEADERS_MAP_ATTRIBUTE = "outboundHeadersMapName";
  public static final String DEFAULT_OUTBOUND_HEADERS_MAP_NAME = "outboundHeaders";
  public static final String HTTP_STATUS_VAR_ATTRIBUTE = "httpStatusVarName";
  public static final String DEFAULT_HTTP_STATUS_NAME = "httpStatus";
  public static final String DISABLE_VALIDATIONS = "disableValidations";
  public static final String QUERY_PARAMS_STRICT_VALIDATION = "queryParamsStrictValidation";
  public static final String HEADERS_STRICT_VALIDATION = "headersStrictValidation";
  public static final String KEEP_API_BASE_URI = "keepApiBaseUri";
  public static final String KEEP_RAML_BASE_URI = "keepRamlBaseUri";


  public static final List<String> ADDITIONAL_ATTRIBUTES = Arrays.asList(
                                                                         DISABLE_VALIDATIONS,
                                                                         QUERY_PARAMS_STRICT_VALIDATION,
                                                                         HEADERS_STRICT_VALIDATION,
                                                                         KEEP_API_BASE_URI,
                                                                         KEEP_RAML_BASE_URI);

  private String name;
  private String api;
  private String raml;
  private Boolean extensionEnabled = null;
  private String outboundHeadersMapName = DEFAULT_OUTBOUND_HEADERS_MAP_NAME;
  private String httpStatusVarName = DEFAULT_HTTP_STATUS_NAME;
  private Map<String, String> additionalAttributes = new HashMap<>();

  public APIKitConfig(final String name,
                      final String api,
                      final Boolean extensionEnabled,
                      final String outboundHeadersMapName,
                      final String httpStatusVarName) {
    this.name = name;
    this.api = api;
    this.extensionEnabled = extensionEnabled;
    this.outboundHeadersMapName = outboundHeadersMapName;
    this.httpStatusVarName = httpStatusVarName;
  }

  public APIKitConfig() {}

  public String getName() {
    return name;
  }

  public String getApi() {
    return api;
  }

  public String getRaml() {
    return raml;
  }


  public Boolean isExtensionEnabled() {
    return extensionEnabled;
  }

  public void setExtensionEnabled(Boolean enabled) {
    this.extensionEnabled = enabled;
  }

  public void setName(String name) {
    this.name = name;
    if (name == null) {
      this.name = APIKitConfig.DEFAULT_CONFIG_NAME;
    }
  }

  public void setExtensionEnabled(boolean extensionEnabled) {
    this.extensionEnabled = extensionEnabled;
  }

  public void setOutboundHeadersMapName(String outboundHeadersMapName) {
    this.outboundHeadersMapName = outboundHeadersMapName;
  }

  public void setHttpStatusVarName(String httpStatusVarName) {
    this.httpStatusVarName = httpStatusVarName;
  }

  public String getOutboundHeadersMapName() {
    return outboundHeadersMapName;
  }

  public String getHttpStatusVarName() {
    return httpStatusVarName;
  }

  public void setApi(String api) {
    this.api = api.replaceAll("%20", " ");
  }

  public void setRaml(String raml) {
    this.raml = raml;
  }

  public String getApiSpecificationLocation() {
    return api != null ? api : raml;
  }

  public void addAdditionalAttribute(Attribute attribute) {
    additionalAttributes.put(attribute.getName(), attribute.getValue());
  }

  public Set<Map.Entry<String, String>> getAdditionalAttributes() {
    return additionalAttributes.entrySet();
  }

  public Element generate() {
    APIKitConfigScope apiKitConfigScope = new APIKitConfigScope(this);
    return apiKitConfigScope.generate();
  }
}
