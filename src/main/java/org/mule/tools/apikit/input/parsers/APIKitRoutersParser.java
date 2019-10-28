/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input.parsers;

import com.google.common.collect.Sets;
import org.mule.apikit.common.ApiSyncUtils;
import org.mule.tools.apikit.input.APIKitFlow;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.HttpListenerConfig;
import org.mule.tools.apikit.model.MuleConfig;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class APIKitRoutersParser implements MuleConfigFileParser {

  private static final XPathExpression<Element> COMPILED_ROUTER_EXPRESSION = getCompiledExpression();

  private static XPathExpression<Element> getCompiledExpression() {
    return XPathFactory.instance().compile("//*/*[local-name()='router']",
                                           Filters.element(APIKitTools.API_KIT_NAMESPACE.getNamespace()));
  }

  private final List<APIKitConfig> apikitConfigs;
  private final List<HttpListenerConfig> httpListenerConfigs;
  private final String apiFilePath;
  private final APIFactory apiFactory;
  private MuleConfig muleConfig;

  public APIKitRoutersParser(List<APIKitConfig> apikitConfigs, APIFactory apiFactory, String apiFilePath, MuleConfig config) {
    this.apikitConfigs = apikitConfigs;
    this.httpListenerConfigs = apiFactory.getHttpListenerConfigs();
    this.apiFilePath = apiFilePath;
    this.apiFactory = apiFactory;
    this.muleConfig = config;
  }

  @Override
  public Map<String, ApikitMainFlowContainer> parse(Document document) {
    Set<String> allApisPathsInApplication = getAllApisPathsInApplication();
    Map<String, ApikitMainFlowContainer> includedApis = new HashMap<>();

    List<Element> elements = COMPILED_ROUTER_EXPRESSION.evaluate(document);
    for (Element element : elements) {
      APIKitConfig config = getApikitConfig(element);

      String currentApiPath = config.getApi() == null ? Paths.get(config.getRaml()).toString() : config.getApi();
      for (String apiPath : allApisPathsInApplication) {
        if (compareApisLocation(currentApiPath, apiPath)) {
          Element source = findListenerOrInboundEndpoint(element.getParentElement().getChildren());
          String configId = config.getName() != null ? config.getName() : APIKitFlow.UNNAMED_CONFIG_NAME;

          if ("listener".equals(source.getName())) {
            includedApis.put(configId, handleListenerSource(source, apiPath, config));
          } else if ("inbound-endpoint".equals(source.getName())) {
            includedApis.put(configId, handleInboundEndpointSource(source, apiPath, config));
          } else {
            throw new IllegalStateException("The first element of the main flow must be an " +
                "inbound-endpoint or listener");
          }
        }
      }
    }
    return includedApis;
  }

  private boolean compareApisLocation(String configRaml, String currentRootRaml) {
    if (ApiSyncUtils.isSyncProtocol(configRaml) && ApiSyncUtils.isSyncProtocol(currentRootRaml)) {
      return ApiSyncUtils.compareResourcesLocation(configRaml, currentRootRaml, false);
    }

    return currentRootRaml.endsWith(configRaml);
  }

  private APIKitConfig getApikitConfig(Element element) throws IllegalStateException {
    Attribute configRef = element.getAttribute("config-ref");
    String configId = configRef != null ? configRef.getValue() : APIKitFlow.UNNAMED_CONFIG_NAME;

    return apikitConfigs.stream()
        .filter(c -> c.getName().equals(configId)).findFirst()
        .orElseThrow(() -> new IllegalStateException("An Apikit configuration is mandatory."));
  }

  private ApikitMainFlowContainer handleListenerSource(Element source, String apiFilePath, APIKitConfig config) {
    HttpListenerConfig httpListenerConfig = getHTTPListenerConfig(source);
    String path = getPathFromInbound(source);
    // TODO PARSE HTTPSTATUSVARNAME AND OUTBOUNDHEADERSMAPNAME
    return apiFactory.createAPIBinding(apiFilePath, null, path, config, httpListenerConfig, muleConfig);
  }

  private ApikitMainFlowContainer handleInboundEndpointSource(Element source, String apiFilePath, APIKitConfig config) {
    String baseUri = null;
    String path = source.getAttributeValue("path");

    // Case the user is specifying baseURI using address attribute
    if (path == null) {
      baseUri = source.getAttributeValue("address");

      if (baseUri == null) {
        throw new IllegalStateException("Neither 'path' nor 'address' attribute was used. " +
            "Cannot retrieve base URI.");
      }

      path = APIKitTools.getPathFromUri(baseUri, false);
    } else if (!path.startsWith("/")) {
      path = "/" + path;
    }
    return apiFactory.createAPIBinding(apiFilePath, baseUri, path, config, null, muleConfig);
  }

  private Element findListenerOrInboundEndpoint(List<Element> elements) {
    for (Element element : elements) {
      if ("listener".equals(element.getName()) || "inbound-endpoint".equals(element.getName())) {
        return element;
      }
    }
    throw new IllegalStateException("The main flow must have an inbound-endpoint or listener");
  }

  private HttpListenerConfig getHTTPListenerConfig(Element inbound) {
    Attribute httpListenerConfigRef = inbound.getAttribute("config-ref");
    String httpListenerConfigId =
        httpListenerConfigRef != null ? httpListenerConfigRef.getValue() : HttpListenerConfig.DEFAULT_CONFIG_NAME;

    HttpListenerConfig httpListenerConfig = httpListenerConfigs.stream()
        .filter(config -> config.getName().equals(httpListenerConfigId))
        .findFirst().orElse(null);
    if (httpListenerConfig == null) {
      throw new IllegalStateException("An HTTP Listener configuration is mandatory.");
    }
    return httpListenerConfig;
  }

  private String getPathFromInbound(Element inbound) {
    String address = inbound.getAttributeValue("address");
    if (address != null) {
      return APIKitTools.getPathFromUri(address, false);
    }
    String path = inbound.getAttributeValue("path");
    if (path == null) {
      path = "";
    } else if (!path.startsWith("/")) {
      path = "/" + path;
    }
    return path;
  }

  private Set<String> getAllApisPathsInApplication() {
    Set<String> paths = Sets.newHashSet(apiFilePath);
    apikitConfigs.forEach(config -> {
      String apiPath = config.getApi() != null ? config.getApi() : Paths.get(config.getRaml()).toString();
      if (paths.stream().noneMatch(path -> compareApisLocation(apiPath, path))) {
        paths.add(apiPath);
      }
    });
    return paths;
  }
}
