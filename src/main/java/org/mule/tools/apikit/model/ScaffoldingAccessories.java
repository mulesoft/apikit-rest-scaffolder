/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

public class ScaffoldingAccessories {

  private final boolean showConsole;
  private final String externalCommonFile;
  private final String apiId;
  private final Properties properties;

  public ScaffoldingAccessories() {
    this.showConsole = true;
    this.externalCommonFile = null;
    this.apiId = null;
    this.properties = null;
  }

  public ScaffoldingAccessories(boolean showConsole,
                                String externalCommonFile,
                                String apiId,
                                Properties properties) {
    this.showConsole = showConsole;
    this.externalCommonFile = externalCommonFile;
    this.apiId = apiId;
    this.properties = properties;
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

}
