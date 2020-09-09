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
  private final boolean showConsole;
  private final CustomConfiguration customConfiguration;

  private ScaffoldingConfiguration(ApiSpecification api, List<MuleConfig> configs, MuleDomain domain, boolean showConsole,
                                   CustomConfiguration customConfiguration) {
    this.api = api;
    this.configurations = configs;
    this.domain = domain;
    this.showConsole = showConsole;
    this.customConfiguration = customConfiguration;
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

  public boolean isShowConsole() {
    return showConsole;
  }

  public CustomConfiguration getCustomConfiguration() {
    return this.customConfiguration;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private ApiSpecification api;
    private List<MuleConfig> muleConfigurations;
    private MuleDomain domain;
    private boolean showConsole;
    private CustomConfiguration customConfiguration;

    public Builder() {
      this.muleConfigurations = new ArrayList<>();
      this.showConsole = true;
      this.customConfiguration = new CustomConfiguration();
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

    public Builder withShowConsole(boolean showConsole) {
      this.showConsole = showConsole;
      return this;
    }

    public Builder withCustomConfiguration(CustomConfiguration customConfiguration) {
      this.customConfiguration = customConfiguration;
      return this;
    }

    public ScaffoldingConfiguration build() {
      return new ScaffoldingConfiguration(api, muleConfigurations, domain, showConsole, customConfiguration);
    }
  }

}
