/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.util.Map;

public class Configuration {

  private String environment;
  private CommonProperties commonProperties;
  private Map<String, String> properties;

  public Configuration() {}

  public Configuration(String environment, CommonProperties commonProperties, Map<String, String> properties) {
    this.environment = environment;
    this.commonProperties = commonProperties;
    this.properties = properties;
  }

  public CommonProperties getCommonProperties() {
    return commonProperties;
  }

  public void setCommonProperties(CommonProperties commonProperties) {
    this.commonProperties = commonProperties;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}
