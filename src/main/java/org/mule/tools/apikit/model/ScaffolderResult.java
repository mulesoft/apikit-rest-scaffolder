/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.util.ArrayList;
import java.util.List;

public class ScaffolderResult implements ScaffoldingResult {

  private List<MuleConfig> generatedConfigs;
  private List<ScaffolderResource> generatedResources;
  private List<ScaffoldingError> errors;

  private ScaffolderResult(List<MuleConfig> generatedConfigs, List<ScaffolderResource> generatedResources,
                           List<ScaffoldingError> errors) {
    this.generatedConfigs = generatedConfigs;
    this.generatedResources = generatedResources;
    this.errors = errors;
  }

  @Override
  public boolean isSuccess() {
    return this.errors.size() == 0;
  }

  @Override
  public List<MuleConfig> getGeneratedConfigs() {
    return generatedConfigs;
  }

  @Override
  public List<ScaffolderResource> getGeneratedResources() {
    return generatedResources;
  }

  @Override
  public List<ScaffoldingError> getErrors() {
    return errors;
  }

  public static class Builder {

    private List<MuleConfig> generatedConfigs;
    private List<ScaffolderResource> generatedResources;
    private List<ScaffoldingError> errors;

    public Builder() {
      generatedConfigs = new ArrayList<>();
      generatedResources = new ArrayList<>();
      errors = new ArrayList<>();
    }

    public Builder withGeneratedConfigs(List<MuleConfig> configs) {
      generatedConfigs = configs;
      return this;
    }

    public Builder withGeneratedResources(List<ScaffolderResource> resources) {
      generatedResources = resources;
      return this;
    }

    public Builder withErrors(List<ScaffoldingError> errors) {
      this.errors = errors;
      return this;
    }

    public ScaffolderResult build() {
      return new ScaffolderResult(generatedConfigs, generatedResources, errors);
    }
  }
}
