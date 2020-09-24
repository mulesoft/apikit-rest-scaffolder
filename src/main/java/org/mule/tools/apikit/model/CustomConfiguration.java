/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;


import java.util.Optional;

public class CustomConfiguration {

  private Optional<String> externalConfigurationFile;
  private Optional<String> apiAutodiscoveryID;
  private Optional<ConfigurationGroup> configurationGroup;

  public CustomConfiguration() {
    this.externalConfigurationFile = Optional.empty();
    this.apiAutodiscoveryID = Optional.empty();
    this.configurationGroup = Optional.empty();
  }

  public CustomConfiguration(String externalConfigurationFile,
                             String apiAutodiscoveryID, ConfigurationGroup configurationGroup) {
    this.externalConfigurationFile = Optional.ofNullable(externalConfigurationFile);
    this.apiAutodiscoveryID = Optional.ofNullable(apiAutodiscoveryID);
    this.configurationGroup = Optional.ofNullable(configurationGroup);
  }

  public Optional<String> getExternalConfigurationFile() {
    return externalConfigurationFile;
  }

  public void setExternalConfigurationFile(String externalConfigurationFile) {
    this.externalConfigurationFile = Optional.ofNullable(externalConfigurationFile);
  }

  public Optional<String> getApiAutodiscoveryID() {
    return apiAutodiscoveryID;
  }

  public void setApiAutodiscoveryID(String apiAutodiscoveryID) {
    this.apiAutodiscoveryID = Optional.ofNullable(apiAutodiscoveryID);
  }

  public Optional<ConfigurationGroup> getConfigurationGroup() {
    return configurationGroup;
  }

  public void setConfigurationGroup(ConfigurationGroup configurationGroup) {
    this.configurationGroup = Optional.ofNullable(configurationGroup);
  }
}
