/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.util.Map;

public class ScaffoldingConfigurationMojo {

  private boolean showConsole;
  private String externalCommonFile;
  private String apiId;
  private String propertiesFormat;
  private Map<String, Map<String, Object>> properties;


  public ScaffoldingConfigurationMojo() {
    this.showConsole = true;
    this.externalCommonFile = null;
    this.apiId = null;
  }

  public ScaffoldingConfigurationMojo(boolean showConsole, String externalCommonFile, String apiId) {
    this.showConsole = showConsole;
    this.externalCommonFile = externalCommonFile;
    this.apiId = apiId;
  }

  public boolean isShowConsole() {
    return showConsole;
  }

  public void setShowConsole(boolean showConsole) {
    this.showConsole = showConsole;
  }

  public String getExternalCommonFile() {
    return externalCommonFile;
  }

  public void setExternalCommonFile(String externalCommonFile) {
    this.externalCommonFile = externalCommonFile;
  }

  public String getApiId() {
    return apiId;
  }

  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  public String getPropertiesFormat() {
    return propertiesFormat;
  }

  public void setPropertiesFormat(String propertiesFormat) {
    this.propertiesFormat = propertiesFormat;
  }

  public Map<String, Map<String, Object>> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Map<String, Object>> properties) {
    this.properties = properties;
  }
}
