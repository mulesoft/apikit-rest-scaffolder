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
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mule.tools.apikit.model.APIAutodiscoveryConfig;
import org.mule.tools.apikit.model.ConfigurationPropertiesConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfigurationPropertiesConfigParser implements MuleConfigFileParser<List<ConfigurationPropertiesConfig>> {

  private static final XPathExpression<Element> CONFIGURATION_PROPERTIES_EXPRESSION =
      XPathFactory.instance().compile("//*/*[local-name()='" + ConfigurationPropertiesConfig.ELEMENT_NAME + "']",
                                      new ElementFilter());

  @Override
  public List<ConfigurationPropertiesConfig> parse(Document document) {
    List<ConfigurationPropertiesConfig> configurations = new ArrayList<>();
    List<Element> elements = CONFIGURATION_PROPERTIES_EXPRESSION.evaluate(document);

    for (Element element : elements) {
      ConfigurationPropertiesConfig config = new ConfigurationPropertiesConfig();
      Attribute fileAttribute = element.getAttribute(ConfigurationPropertiesConfig.FILE_ATTRIBUTE);

      if (fileAttribute != null) {
        config.setFile(fileAttribute.getValue());
      } else {
        throw new RuntimeException("file is a mandatory field");
      }
      configurations.add(config);
    }
    return configurations;
  }
}
