/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.mule.tools.apikit.input.parsers.APIKitFlowsParser;
import org.mule.tools.apikit.input.parsers.APIKitRoutersParser;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MuleConfigParser {

  private Set<ResourceActionMimeTypeTriplet> entries = new HashSet<>();
  private Map<String, ApikitMainFlowContainer> includedApis = new HashMap<>();
  private List<APIKitConfig> apikitConfigs = new LinkedList<>();
  private final APIFactory apiFactory;

  public MuleConfigParser(APIFactory apiFactory, String apiLocation, List<MuleConfig> muleConfigs) {
    this.apiFactory = apiFactory;
    for (MuleConfig config : muleConfigs) {
      parseConfig(config);
    }
    for (MuleConfig config : muleConfigs) {
      parseApis(config, apiLocation);
    }
    parseFlows(muleConfigs);
  }

  void parseConfig(MuleConfig config) {
    apikitConfigs.addAll(config.getApikitConfigs());
    config.getHttpListenerConfigs().forEach(httpConfig -> {
      if (!apiFactory.getHttpListenerConfigs().contains(httpConfig)) {
        apiFactory.getHttpListenerConfigs().add(httpConfig);
      }
    });
  }

  void parseApis(MuleConfig muleConfig, String apiFilePath) {
    includedApis.putAll(new APIKitRoutersParser(apikitConfigs, apiFactory, apiFilePath, muleConfig)
        .parse(muleConfig.getContentAsDocument()));
  }

  void parseFlows(List<MuleConfig> configs) {
    for (MuleConfig config : configs) {
      entries.addAll(new APIKitFlowsParser(includedApis).parse(config.getContentAsDocument()));
    }
  }

  List<APIKitConfig> getApikitConfigs() {
    return apikitConfigs;
  }

  public Set<ResourceActionMimeTypeTriplet> getEntries() {
    return entries;
  }

  public Set<ApikitMainFlowContainer> getIncludedApis() {
    return new HashSet<>(includedApis.values());
  }
}
