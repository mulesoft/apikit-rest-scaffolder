/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input.parsers;

import org.apache.commons.collections.CollectionUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import org.mule.tools.apikit.model.APIAutodiscoveryConfig;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.mule.tools.apikit.model.APIAutodiscoveryConfig.*;

public class APIAutodiscoveryConfigParser implements MuleConfigFileParser<List<APIAutodiscoveryConfig>> {

  private static final XPathExpression<Element> API_AUTODISCOVERY_EXPRESSION =
      XPathFactory.instance().compile("//*/*[local-name()='" + ELEMENT_NAME + "']",
                                      Filters.element(API_AUTODISCOVERY_NAMESPACE.getNamespace()));

  @Override
  public List<APIAutodiscoveryConfig> parse(Document document) {
    List<APIAutodiscoveryConfig> configurations = new LinkedList<>();
    List<Element> elements = API_AUTODISCOVERY_EXPRESSION.evaluate(document);

    if (CollectionUtils.isNotEmpty(elements)) {
      for (Element element : elements) {
        APIAutodiscoveryConfig configuration = new APIAutodiscoveryConfig();
        Attribute apiIdAttribute = element.getAttribute(API_ID_ATTRIBUTE);
        Attribute ignoreBasePathAttribute = element.getAttribute(IGNORE_BASE_PATH_ATTRIBUTE);
        Attribute flowRefAttribute = element.getAttribute(FLOW_REF_ATTRIBUTE);

        if (apiIdAttribute != null) {
          configuration.setApiId(apiIdAttribute.getValue());
        } else {
          throw new RuntimeException("apiId is a mandatory field");
        }

        if (ignoreBasePathAttribute != null) {
          configuration.setIgnoreBasePath(Boolean.valueOf(ignoreBasePathAttribute.getValue()));
        }

        if (flowRefAttribute != null) {
          configuration.setFlowRef(flowRefAttribute.getValue());
        }
        configurations.add(configuration);
      }
    }
    return configurations;
  }
}

