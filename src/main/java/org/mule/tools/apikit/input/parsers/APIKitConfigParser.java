/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input.parsers;

import static org.mule.tools.apikit.input.APIKitFlow.UNNAMED_CONFIG_NAME;

import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.APIKitConfig;

import java.util.LinkedList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class APIKitConfigParser implements MuleConfigFileParser<List<APIKitConfig>> {

  private static final XPathExpression<Element> APIKIT_CONFIG_EXPRESSION =
      XPathFactory.instance().compile("//*/*[local-name()='" + APIKitConfig.ELEMENT_NAME + "']",
                                      Filters.element(APIKitTools.API_KIT_NAMESPACE.getNamespace()));

  @Override
  public List<APIKitConfig> parse(Document document) {
    List<APIKitConfig> apikitConfigs = new LinkedList<>();

    List<Element> elements = APIKIT_CONFIG_EXPRESSION.evaluate(document);

    for (Element element : elements) {
      Attribute name = element.getAttribute(APIKitConfig.NAME_ATTRIBUTE);
      Attribute api = element.getAttribute(APIKitConfig.API_ATTRIBUTE);
      Attribute raml = element.getAttribute(APIKitConfig.RAML_ATTRIBUTE);
      Attribute extensionEnabled = element.getAttribute(APIKitConfig.EXTENSION_ENABLED_ATTRIBUTE);
      Attribute outboundHeadersMapName = element.getAttribute(APIKitConfig.OUTBOUND_HEADERS_MAP_ATTRIBUTE);
      Attribute httpStatusVarName = element.getAttribute(APIKitConfig.HTTP_STATUS_VAR_ATTRIBUTE);

      final APIKitConfig apiKitConfig = new APIKitConfig();
      if (api != null) {
        apiKitConfig.setApi(api.getValue());
      } else if (raml != null) {
        apiKitConfig.setRaml(raml.getValue());
      } else {
        throw new IllegalArgumentException(APIKitConfig.API_ATTRIBUTE + " attribute is required on apikit configuration");
      }

      apiKitConfig.setName(name != null ? name.getValue() : UNNAMED_CONFIG_NAME);

      if (outboundHeadersMapName != null) {
        apiKitConfig.setOutboundHeadersMapName(outboundHeadersMapName.getValue());
      }

      if (extensionEnabled != null) {
        apiKitConfig.setExtensionEnabled(Boolean.valueOf(extensionEnabled.getValue()));
      }

      if (httpStatusVarName != null) {
        apiKitConfig.setHttpStatusVarName(httpStatusVarName.getValue());
      }
      apikitConfigs.add(apiKitConfig);
    }

    return apikitConfigs;
  }

}
