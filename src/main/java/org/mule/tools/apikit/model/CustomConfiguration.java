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

  public CustomConfiguration(Optional<String> externalConfigurationFile, Optional<List<String>> environments,
                             Optional<String> apiAutodiscoveryID) {
    this.externalConfigurationFile = externalConfigurationFile;
    this.environments = environments;
    this.apiAutodiscoveryID = apiAutodiscoveryID;
  }

  public Optional<String> getExternalConfigurationFile() {
    return externalConfigurationFile;
  }

  public void setExternalConfigurationFile(Optional<String> externalConfigurationFile) {
    this.externalConfigurationFile = externalConfigurationFile;
  }

  public Optional<List<String>> getEnvironments() {
    return environments;
  }

  public void setEnvironments(Optional<List<String>> environments) {
    this.environments = environments;
  }

  public Optional<String> getApiAutodiscoveryID() {
    return apiAutodiscoveryID;
  }

  public void setApiAutodiscoveryID(Optional<String> apiAutodiscoveryID) {
    this.apiAutodiscoveryID = apiAutodiscoveryID;
  }

  @Override
  public String toString() {
    return "CustomConfiguration{" +
        "externalConfigurationFile=" + externalConfigurationFile +
        ", environments=" + environments +
        ", apiAutodiscoveryID=" + apiAutodiscoveryID +
        '}';
  }
}
