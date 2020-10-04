/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.resources;

import org.mule.tools.apikit.model.CommonProperties;
import org.mule.tools.apikit.model.Configuration;

public abstract class FileProcessor {


  public static final String HTTP_HOST_VALUE = "0.0.0.0";
  public static final String HTTP_PORT_VALUE = "8081";
  public static final String HTTP_KEY = "http";
  public static final String API_ID_KEY = "apiId";

  protected abstract String processCommon(Configuration configuration, String apiAutodiscoveryId);

  protected abstract String processCustom(Configuration configuration);

  protected String getApiAutodiscoveryID(CommonProperties commonProperties, String apiAutodiscoveryId,
                                         boolean hasApiAutodiscoveryIdForEnvironment) {
    return hasApiAutodiscoveryIdForEnvironment ? commonProperties.getApiId() : apiAutodiscoveryId;
  }

  protected boolean hasApiAutodiscoveryIdForEnvironment(CommonProperties commonProperties) {
    return commonProperties != null && commonProperties.getApiId() != null;
  }

}
