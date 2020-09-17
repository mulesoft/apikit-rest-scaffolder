/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input.parsers;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import org.mule.tools.apikit.model.APIAutodiscoveryConfig;

import java.util.Optional;

public class APIAutodiscoveryConfigParser implements MuleConfigFileParser<APIAutodiscoveryConfig> {

  private static final XPathExpression<Element> API_AUTODISCOVERY_EXPRESSION = getCompiledExpression();

  private static XPathExpression<Element> getCompiledExpression() {
    return XPathFactory.instance().compile("//*/*[local-name()='" + APIAutodiscoveryConfig.ELEMENT_NAME + "']",
                                           Filters.element(APIAutodiscoveryConfig.API_AUTODISCOVERY_NAMESPACE.getNamespace()));
  }

  @Override
  public APIAutodiscoveryConfig parse(Document document) {

    APIAutodiscoveryConfig config = null;
    Optional<Element> optionalElement = API_AUTODISCOVERY_EXPRESSION.evaluate(document).stream().findFirst();

    if (optionalElement.isPresent()) {
      config = new APIAutodiscoveryConfig();
      Element element = optionalElement.get();
      Attribute apiIdAttribute = element.getAttribute(APIAutodiscoveryConfig.API_ID_ATTRIBUTE);
      Attribute ignoreBasePathAttribute = element.getAttribute(APIAutodiscoveryConfig.IGNORE_BASE_PATH_ATTRIBUTE);
      Attribute flowRefAttribute = element.getAttribute(APIAutodiscoveryConfig.FLOW_REF_ATTRIBUTE);

      if (apiIdAttribute != null) {
        config.setApiId(apiIdAttribute.getValue());
      } else {
        throw new RuntimeException("apiId is a mandatory field");
      }

      if (ignoreBasePathAttribute != null) {
        config.setIgnoreBasePath(Boolean.valueOf(ignoreBasePathAttribute.getValue()));
      }

      if (flowRefAttribute != null) {
        config.setFlowRef(flowRefAttribute.getValue());
      }
    }
    return config;
  }
}

