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

  /**
   * This method is intended to create all properties that are common for a new mule application, by default will be http listener
   * and (if required) api autodiscovery.
   * @param configuration map of configurations
   * @param apiAutodiscoveryId api autodiscovery id
   * @return string containing all properties in a format
   */
  protected abstract String processCommon(Map<String, Object> configuration, String apiAutodiscoveryId);

  /**
   * This method is for properties that are specific for the application being created, could be connection to custom servers,
   * web services, http calls, dbs, etc.
   * @param configuration map of configurations
   * @return string containing all properties in a format
   */
  protected abstract String processCustom(Map<String, Object> configuration);

  protected String getApiAutodiscoveryID(Map<String, Object> commonProperties, String apiAutodiscoveryId,
                                         boolean hasApiAutodiscoveryIdForEnvironment) {
    return hasApiAutodiscoveryIdForEnvironment ? commonProperties.get(API_ID_KEY).toString() : apiAutodiscoveryId;
  }

  protected boolean hasApiAutodiscoveryIdForEnvironment(Map<String, Object> commonProperties) {
    return commonProperties != null && commonProperties.get(API_ID_KEY) != null;
  }

}
