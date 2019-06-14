/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.jdom2.Document;
import org.mule.tools.apikit.input.parsers.APIKitFlowsParser;
import org.mule.tools.apikit.input.parsers.APIKitRoutersParser;
import org.mule.tools.apikit.model.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MuleConfigParser {

  private Set<ResourceActionMimeTypeTriplet> entries = new HashSet<>();
  private Map<String, ApikitMainFlowContainer> includedApis = new HashMap<>();
  private Map<String, APIKitConfig> apikitConfigs = new HashMap<>();
  private final APIFactory apiFactory;

  public MuleConfigParser(APIFactory apiFactory) {
    this.apiFactory = apiFactory;
  }

  public MuleConfigParser parse(String apiLocation, List<MuleConfig> muleConfigs) {
    for (MuleConfig config : muleConfigs) {
      parseConfigs(config);
    }

    for (MuleConfig config : muleConfigs) {
      parseApis(config, apiLocation);
    }
    parseFlows(muleConfigs);

    return this;
  }

  protected void parseConfigs(MuleConfig config) {
    apikitConfigs.putAll(config.getApikitConfigs());

    config.getHttpListenerConfigs().stream().forEach(httpConfig -> {
      if (!apiFactory.getHttpListenerConfigs().contains(httpConfig)) {
        apiFactory.getHttpListenerConfigs().add(httpConfig);
      }
    });
  }

  protected void parseApis(MuleConfig muleConfig, String apiFilePath) {
    includedApis.putAll(new APIKitRoutersParser(apikitConfigs, apiFactory, apiFilePath, muleConfig)
        .parse(muleConfig.getContentAsDocument()));
  }

  protected void parseFlows(List<MuleConfig> configs) {
    for (MuleConfig config : configs) {
      entries.addAll(new APIKitFlowsParser(includedApis).parse(config.getContentAsDocument()));
    }
  }

  public Map<String, APIKitConfig> getApikitConfigs() {
    return apikitConfigs;
  }

  public Set<ResourceActionMimeTypeTriplet> getEntries() {
    return entries;
  }

  public Set<ApikitMainFlowContainer> getIncludedApis() {
    return new HashSet<>(includedApis.values());
  }
}
