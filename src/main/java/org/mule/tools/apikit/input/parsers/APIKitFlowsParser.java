/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input.parsers;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mule.tools.apikit.input.APIKitFlow;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.misc.FlowNameUtils;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;

public class APIKitFlowsParser implements MuleConfigFileParser<Set<ResourceActionMimeTypeTriplet>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(APIKitFlowsParser.class.getName());

  private final Map<String, ApikitMainFlowContainer> includedApis;

  public APIKitFlowsParser(final Map<String, ApikitMainFlowContainer> includedApis) {
    this.includedApis = includedApis;
  }

  @Override
  public Set<ResourceActionMimeTypeTriplet> parse(Document document) {
    Set<ResourceActionMimeTypeTriplet> entries = new HashSet<>();
    XPathExpression<Element> xp = XPathFactory.instance().compile("//*/*[local-name()='flow']",
                                                                  Filters.element(XMLNS_NAMESPACE.getNamespace()));
    List<Element> elements = xp.evaluate(document);
    for (Element element : elements) {
      String name = FlowNameUtils.decode(element.getAttributeValue("name"));
      APIKitFlow flow;
      try {
        flow = APIKitFlow.buildFromName(name, includedApis.keySet());
      } catch (IllegalArgumentException iae) {
        LOGGER.info("Flow named '" + name + "' is not an APIKit Flow because it does not follow APIKit naming convention.");
        continue;
      }

      ApikitMainFlowContainer api = includedApis.get(flow.getConfigRef());

      String resource = flow.getResource();
      if (api != null) {
        if (!resource.startsWith("/")) {
          resource = "/" + resource;
        }
        if (api.getPath() == null) {
          throw new IllegalStateException("Api path is invalid");
        }
        String path = APIKitTools.getCompletePathFromBasePathAndPath(api.getHttpListenerConfig().getBasePath(), api.getPath());
        entries.add(new ResourceActionMimeTypeTriplet(api, path + resource, flow.getAction(), flow.getMimeType()));
      } else {
        throw new IllegalStateException("No APIKit entries found in Mule config");
      }
    }
    return entries;
  }
}
