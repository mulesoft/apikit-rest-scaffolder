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
import java.util.Map;

public class ScaffoldingConfiguration {

  private final ApiSpecification api;
  private final List<MuleConfig> configurations;
  private final MuleDomain domain;
  private final boolean showConsole;
  private String externalConfigurationFile;
  private String apiAutodiscoveryID;
  private String propertiesFormat;
  private Map<String, Map<String, Object>> properties;
  private String apiSyncResource;

  private ScaffoldingConfiguration(ApiSpecification api, List<MuleConfig> configs, MuleDomain domain, boolean showConsole,
                                   String externalConfigurationFile,
                                   String apiAutodiscoveryID, String propertiesFormat,
                                   Map<String, Map<String, Object>> properties, String apiSyncResource) {
    this.api = api;
    this.configurations = configs;
    this.domain = domain;
    this.showConsole = showConsole;
    this.externalConfigurationFile = externalConfigurationFile;
    this.apiAutodiscoveryID = apiAutodiscoveryID;
    this.propertiesFormat = propertiesFormat;
    this.properties = properties;
    this.apiSyncResource = apiSyncResource;
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

  public String getExternalConfigurationFile() {
    return externalConfigurationFile;
  }

  public String getApiAutodiscoveryID() {
    return apiAutodiscoveryID;
  }

  public String getPropertiesFormat() {
    return propertiesFormat;
  }

  public Map<String, Map<String, Object>> getProperties() {
    return properties;
  }

  public String getApiSyncResource() {
    return apiSyncResource;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private ApiSpecification api;
    private List<MuleConfig> muleConfigurations;
    private MuleDomain domain;
    private boolean showConsole;
    private String externalConfigurationFile;
    private String apiAutodiscoveryID;
    private String propertiesFormat;
    private Map<String, Map<String, Object>> properties;
    private String apiSyncResource;

    public Builder() {
      this.muleConfigurations = new ArrayList<>();
      this.showConsole = true;
      this.externalConfigurationFile = null;
      this.apiAutodiscoveryID = null;
      this.propertiesFormat = null;
      this.properties = null;
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

    public Builder withShowConsole(boolean showConsole) {
      this.showConsole = showConsole;
      return this;
    }

    public Builder withExternalConfigurationFile(String externalConfigurationFile) {
      this.externalConfigurationFile = externalConfigurationFile;
      return this;
    }

    public Builder withApiAutodiscoveryId(String apiAutodiscoveryID) {
      this.apiAutodiscoveryID = apiAutodiscoveryID;
      return this;
    }

    public Builder withPropertiesFormat(String propertiesFormat) {
      this.propertiesFormat = propertiesFormat;
      return this;
    }

    public Builder withProperties(Map<String, Map<String, Object>> properties) {
      this.properties = properties;
      return this;
    }

    public Builder withApiSyncResource(String apiSyncResource) {
      this.apiSyncResource = apiSyncResource;
      return this;
    }

    public ScaffoldingConfiguration build() {
      return new ScaffoldingConfiguration(api, muleConfigurations, domain, showConsole, externalConfigurationFile,
                                          apiAutodiscoveryID, propertiesFormat, properties, apiSyncResource);
    }
  }

}
