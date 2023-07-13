/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import static java.lang.String.valueOf;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.mule.tools.apikit.misc.APIKitTools.API_KIT_NAMESPACE;
import static org.mule.tools.apikit.model.APIKitConfig.API_ATTRIBUTE;
import static org.mule.tools.apikit.model.APIKitConfig.DISABLE_VALIDATIONS;
import static org.mule.tools.apikit.model.APIKitConfig.ELEMENT_NAME;
import static org.mule.tools.apikit.model.APIKitConfig.EXTENSION_ENABLED_ATTRIBUTE;
import static org.mule.tools.apikit.model.APIKitConfig.HTTP_STATUS_VAR_ATTRIBUTE;
import static org.mule.tools.apikit.model.APIKitConfig.NAME_ATTRIBUTE;
import static org.mule.tools.apikit.model.APIKitConfig.OUTBOUND_HEADERS_MAP_ATTRIBUTE;
import static org.mule.tools.apikit.model.APIKitConfig.QUERY_PARAMS_STRICT_VALIDATION;
import static org.mule.tools.apikit.model.APIKitConfig.RAML_ATTRIBUTE;

import org.mule.tools.apikit.model.APIKitConfig;

import org.jdom2.Element;

public class APIKitConfigScope implements Scope {

  private final APIKitConfig config;

  public APIKitConfigScope(APIKitConfig config) {
    this.config = config;
  }

  @Override
  public Element generate() {

    if (config == null) {
      return null;
    }

    Element apikitConfig = new Element(ELEMENT_NAME,
                                       API_KIT_NAMESPACE.getNamespace());

    if (!isEmpty(config.getName())) {
      apikitConfig.setAttribute(NAME_ATTRIBUTE, config.getName());
    }

    if (config.getApi() != null) {
      apikitConfig.setAttribute(API_ATTRIBUTE, config.getApi());
    }
    if (config.getRaml() != null) {
      apikitConfig.setAttribute(RAML_ATTRIBUTE, config.getRaml());
    }
    if (config.isExtensionEnabled() != null) {
      apikitConfig.setAttribute(EXTENSION_ENABLED_ATTRIBUTE, valueOf(config.isExtensionEnabled()));
    }
    if (config.getOutboundHeadersMapName() != null) {
      apikitConfig.setAttribute(OUTBOUND_HEADERS_MAP_ATTRIBUTE, config.getOutboundHeadersMapName());
    }
    if (config.getHttpStatusVarName() != null) {
      apikitConfig.setAttribute(HTTP_STATUS_VAR_ATTRIBUTE, config.getHttpStatusVarName());
    }

    config.getAdditionalAttributes().stream().forEach(entry -> apikitConfig.setAttribute(entry.getKey(), entry.getValue()));

    return apikitConfig;
  }
}
