/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import org.jdom2.Element;
import org.mule.tools.apikit.model.APIAutodiscoveryConfig;
import org.mule.tools.apikit.model.APIKitConfig;

public class APIAutodiscoveryScope implements Scope {

  private APIAutodiscoveryConfig apiAutodiscoveryConfig;

  public APIAutodiscoveryScope(APIAutodiscoveryConfig config) {
    this.apiAutodiscoveryConfig = config;
  }

  @Override
  public Element generate() {
    Element config = null;
    if (this.apiAutodiscoveryConfig != null) {
      config = new Element(APIAutodiscoveryConfig.ELEMENT_NAME,
                           APIAutodiscoveryConfig.API_AUTODISCOVERY_NAMESPACE.getNamespace());
      if (this.apiAutodiscoveryConfig.getApiId() != null)
        config.setAttribute(APIAutodiscoveryConfig.API_ID_ATTRIBUTE, this.apiAutodiscoveryConfig.getApiId());
      if (this.apiAutodiscoveryConfig.getFlowRef() != null)
        config.setAttribute(APIAutodiscoveryConfig.FLOW_REF_ATTRIBUTE, this.apiAutodiscoveryConfig.getFlowRef());
      if (this.apiAutodiscoveryConfig.getIgnoreBasePath() != null)
        config.setAttribute(APIAutodiscoveryConfig.IGNORE_BASE_PATH_ATTRIBUTE,
                            String.valueOf(this.apiAutodiscoveryConfig.getIgnoreBasePath()));

    }
    return config;
  }
}
