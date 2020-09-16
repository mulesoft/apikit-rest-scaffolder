/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.util.List;
import java.util.Optional;

public class CustomConfiguration {

  private Optional<String> externalConfigurationFile;
  private Optional<List<String>> environments;
  private Optional<String> apiAutodiscoveryID;

  public CustomConfiguration() {
    this.externalConfigurationFile = Optional.empty();
    this.environments = Optional.empty();
    this.apiAutodiscoveryID = Optional.empty();
  }

  public CustomConfiguration(String externalConfigurationFile, List<String> environments,
                             String apiAutodiscoveryID) {
    this.externalConfigurationFile = Optional.ofNullable(externalConfigurationFile);
    this.environments = Optional.ofNullable(environments);
    this.apiAutodiscoveryID = Optional.ofNullable(apiAutodiscoveryID);
  }

  public Optional<String> getExternalConfigurationFile() {
    return externalConfigurationFile;
  }

  public void setExternalConfigurationFile(String externalConfigurationFile) {
    this.externalConfigurationFile = Optional.ofNullable(externalConfigurationFile);
  }

  public Optional<List<String>> getEnvironments() {
    return environments;
  }

  public void setEnvironments(List<String> environments) {
    this.environments = Optional.ofNullable(environments);
  }

  public Optional<String> getApiAutodiscoveryID() {
    return apiAutodiscoveryID;
  }

  public void setApiAutodiscoveryID(String apiAutodiscoveryID) {
    this.apiAutodiscoveryID = Optional.ofNullable(apiAutodiscoveryID);
  }
}
