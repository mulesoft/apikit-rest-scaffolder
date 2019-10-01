/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;
import org.mule.apikit.common.ApiSyncUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.io.File.separator;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isNumeric;
import static org.mule.tools.apikit.model.APIKitConfig.DEFAULT_CONFIG_NAME;
import static org.mule.tools.apikit.model.ApikitMainFlowContainer.DEFAULT_BASE_PATH;
import static org.mule.tools.apikit.model.ApikitMainFlowContainer.DEFAULT_HOST;
import static org.mule.tools.apikit.model.ApikitMainFlowContainer.DEFAULT_PROTOCOL;

public class APIFactory {

  private static final String RESOURCE_API_FOLDER = "src/main/resources/api/".replace("/", separator);

  private Map<String, ApikitMainFlowContainer> apis = new HashMap<>();
  private List<HttpListenerConfig> httpListenerConfigs;

  public APIFactory(List<HttpListenerConfig> httpListenerConfigs) {
    this.httpListenerConfigs = new ArrayList<>(httpListenerConfigs);
  }

  public ApikitMainFlowContainer createAPIBindingInboundEndpoint(String apiFileName, String baseUri, String path,
                                                                 APIKitConfig config) {
    return createAPIBinding(apiFileName, baseUri, path, config, null, null);
  }

  public ApikitMainFlowContainer createAPIBinding(String apiFilePath, String baseUri, String path, APIKitConfig config,
                                                  HttpListenerConfig httpListenerConfig, MuleConfig muleConfig) {

    Validate.notNull(apiFilePath);
    final String relativePath = getRelativePath(apiFilePath);

    if (apis.containsKey(relativePath)) {
      ApikitMainFlowContainer api = apis.get(relativePath);
      if (api.getMuleConfig() == null && muleConfig != null) {
        api.setMuleConfig(muleConfig);
      }
      return api;
    }

    final String id = buildApiId(relativePath);
    ApikitMainFlowContainer api = new ApikitMainFlowContainer(id, relativePath, baseUri, path, config, muleConfig);
    if (httpListenerConfig == null) {
      final HttpListenerConfig availableConfig = getAvailableLCForPath(path, httpListenerConfigs);
      if (availableConfig != null) {
        api.setHttpListenerConfig(availableConfig);
      } else {
        final HttpListenerConfig defaultHttpListenerConfig = buildDefaultHttpListenerConfig(id);
        httpListenerConfigs.add(defaultHttpListenerConfig);
        api.setHttpListenerConfig(defaultHttpListenerConfig);
      }
    } else {
      api.setHttpListenerConfig(httpListenerConfig);
    }
    if (config != null) {
      config.setApi(apiFilePath);
    }
    api.setConfig(config);
    apis.put(relativePath, api);
    return api;
  }

  private HttpListenerConfig buildDefaultHttpListenerConfig(String id) {
    final String nextPort = getNextPort(httpListenerConfigs);
    final HttpListenerConnection listenerConnection = buildDefaultHttpListenerConnection(nextPort);
    String httpListenerConfigName =
        id == null ? HttpListenerConfig.DEFAULT_CONFIG_NAME : id + "-" + HttpListenerConfig.DEFAULT_CONFIG_NAME;
    return new HttpListenerConfig(httpListenerConfigName, DEFAULT_BASE_PATH, listenerConnection);
  }

  private String getNextPort(List<HttpListenerConfig> httpListenerConfigs) {
    final List<HttpListenerConfig> numericPortListeners = getNumericPortListenersAsList(httpListenerConfigs);
    if (numericPortListeners.size() > 0) {
      final String greaterPort = numericPortListeners.get(numericPortListeners.size() - 1).getPort();
      return String.valueOf(Integer.parseInt(greaterPort) + 1);
    }

    return "8081";
  }

  private HttpListenerConnection buildDefaultHttpListenerConnection(String port) {
    return new HttpListenerConnection.Builder()
        .setHost(DEFAULT_HOST)
        .setPort(port)
        .setProtocol(DEFAULT_PROTOCOL)
        .build();
  }

  private String getUriLastSegment(String ramlPathUri) {
    return ramlPathUri.substring(ramlPathUri.lastIndexOf("/") + 1);
  }

  private String sanitizeApiId(String fileName) {
    return fileName.replaceAll(" ", "-").replaceAll("%20", "-");
  }

  private String buildApiId(String ramlFilePath) {
    String apiId;

    if (ApiSyncUtils.isSyncProtocol(ramlFilePath))
      apiId = ApiSyncUtils.getFileName(ramlFilePath);
    else
      apiId = getUriLastSegment(ramlFilePath);
    apiId = FilenameUtils.removeExtension(sanitizeApiId(apiId)).trim();
    final List<String> apiIds = apis.values().stream().map(ApikitMainFlowContainer::getId).collect(toList());

    final List<String> configNames = apis.values().stream()
        .filter(a -> a.getConfig() != null)
        .map(a -> a.getConfig().getName()).collect(toList());

    final List<String> httpConfigNames = apis.values().stream()
        .filter(a -> a.getHttpListenerConfig() != null)
        .map(a -> a.getHttpListenerConfig().getName()).collect(toList());

    int count = 0;
    String id;
    do {
      count++;
      id = (count > 1 ? apiId + "-" + count : apiId);
    } while (apiIds.contains(id) || configNames.contains(id + "-" + DEFAULT_CONFIG_NAME)
        || httpConfigNames.contains(id + "-" + HttpListenerConfig.DEFAULT_CONFIG_NAME));

    return id;
  }

  private String getRelativePath(String path) {
    if (!ApiSyncUtils.isSyncProtocol(path)
        && !(path.startsWith("http://") || path.startsWith("https://"))
        && path.contains(RESOURCE_API_FOLDER))
      return path.substring(path.lastIndexOf(RESOURCE_API_FOLDER) + RESOURCE_API_FOLDER.length()).replace(separator, "/");

    return path.replace(separator, "/");
  }

  public List<HttpListenerConfig> getHttpListenerConfigs() {
    return httpListenerConfigs;
  }

  private HttpListenerConfig getAvailableLCForPath(String path, List<HttpListenerConfig> httpListenerConfigs) {
    if (httpListenerConfigs.size() <= 0)
      return null;

    final List<HttpListenerConfig> usedListeners = apis.entrySet().stream()
        .filter(e -> {
          String apiPath = e.getValue().getPath();
          return path.equals(apiPath) || (path + "/*").equals(apiPath);
        })
        .map(e -> e.getValue().getHttpListenerConfig())
        .collect(toList());


    final List<HttpListenerConfig> availableListeners = new ArrayList<>();
    availableListeners.addAll(getNumericPortListenersAsList(httpListenerConfigs));
    availableListeners.addAll(getNonNumericPortListeners(httpListenerConfigs));

    return availableListeners.stream()
        .filter(config -> !usedListeners.contains(config))
        .findFirst()
        .orElse(null);
  }

  private List<HttpListenerConfig> getNumericPortListenersAsList(List<HttpListenerConfig> httpListenerConfigs) {
    return httpListenerConfigs.stream()
        .filter(config -> isNumeric(config.getPort()))
        .sorted((o1, o2) -> {
          Integer i1 = Integer.parseInt(o1.getPort());
          Integer i2 = Integer.parseInt(o2.getPort());
          return i1.compareTo(i2);
        }).collect(toList());
  }

  private List<HttpListenerConfig> getNonNumericPortListeners(List<HttpListenerConfig> httpListenerConfigs) {
    return httpListenerConfigs.stream()
        .filter(config -> !isNumeric(config.getPort()))
        .collect(toList());
  }
}
