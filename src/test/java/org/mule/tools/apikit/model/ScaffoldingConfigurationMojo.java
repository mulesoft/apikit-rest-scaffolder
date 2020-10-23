/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

public class ScaffoldingConfigurationMojo {

  private boolean showConsole;
  private String externalCommonFile;
  private String apiId;
  private Properties properties;

  public ScaffoldingConfigurationMojo() {
    this.showConsole = true;
    this.externalCommonFile = null;
    this.apiId = null;
    this.properties = null;
  }

  public boolean isShowConsole() {
    return showConsole;
  }

  public String getExternalCommonFile() {
    return externalCommonFile;
  }

  public String getApiId() {
    return apiId;
  }

  public Properties getProperties() {
    return properties;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }
}
