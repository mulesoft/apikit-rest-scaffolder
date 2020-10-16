/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.resources;

import java.util.Map;

public abstract class FileProcessor {


  public static final String HTTP_HOST_VALUE = "0.0.0.0";
  public static final String HTTP_PORT_VALUE = "8081";
  public static final String HTTP_KEY = "http";
  public static final String API_ID_KEY = "apiId";

  protected abstract String processCommon(Map<String, Object> configuration, String apiAutodiscoveryId);

  protected abstract String processCustom(Map<String, Object> configuration);

  protected String getApiAutodiscoveryID(Map<String, Object> commonProperties, String apiAutodiscoveryId,
                                         boolean hasApiAutodiscoveryIdForEnvironment) {
    return hasApiAutodiscoveryIdForEnvironment ? commonProperties.get(API_ID_KEY).toString() : apiAutodiscoveryId;
  }

  protected boolean hasApiAutodiscoveryIdForEnvironment(Map<String, Object> commonProperties) {
    return commonProperties != null && commonProperties.get(API_ID_KEY) != null;
  }

}
