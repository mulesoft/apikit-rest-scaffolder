/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.mule.apikit.model.ApiSpecification;

import java.util.ArrayList;
import java.util.List;

public class ScaffoldingConfiguration {

  private final ApiSpecification api;
  private final List<MuleConfig> configurations;
  private final MuleDomain domain;
  private String apiSyncResource;
  private final ScaffoldingAccessories scaffoldingAccessories;

  private ScaffoldingConfiguration(ApiSpecification api, List<MuleConfig> configs, MuleDomain domain,
                                   ScaffoldingAccessories scaffoldingAccessories, String apiSyncResource) {
    this.api = api;
    this.configurations = configs;
    this.domain = domain;
    this.scaffoldingAccessories = scaffoldingAccessories;
    this.apiSyncResource = apiSyncResource;
  }

  public ScaffoldingAccessories getScaffoldingAccessories() {
    return scaffoldingAccessories;
  }

  public ApiSpecification getApi() {
    return api;
  }

  public List<MuleConfig> getMuleConfigurations() {
    return configurations;
  }

  public MuleDomain getDomain() {
    return domain;
  }

  public String getApiSyncResource() {
    return apiSyncResource;
  }

  public void setApiSyncResource(String apiSyncResource) {
    this.apiSyncResource = apiSyncResource;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private ApiSpecification api;
    private List<MuleConfig> muleConfigurations;
    private MuleDomain domain;
    private ScaffoldingAccessories scaffoldingAccessories;
    private String apiSyncResource;

    public Builder() {
      this.muleConfigurations = new ArrayList<>();
      this.scaffoldingAccessories = new ScaffoldingAccessories();
      this.apiSyncResource = null;
      domain = MuleDomain.builder().build();
    }

    public Builder withApi(ApiSpecification api) {
      this.api = api;
      return this;
    }

    public Builder withMuleConfigurations(List<MuleConfig> configurations) {
      this.muleConfigurations.addAll(configurations);
      return this;
    }

    public Builder withDomain(MuleDomain domain) {
      this.domain = domain;
      return this;
    }

    public Builder withAccessories(ScaffoldingAccessories scaffoldingAccessories) {
      this.scaffoldingAccessories = scaffoldingAccessories;
      return this;
    }

    public Builder withApiSyncResource(String apiSyncResource) {
      this.apiSyncResource = apiSyncResource;
      return this;
    }

    public ScaffoldingConfiguration build() {
      return new ScaffoldingConfiguration(api, muleConfigurations, domain, scaffoldingAccessories, apiSyncResource);
    }
  }

}
