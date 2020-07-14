/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input.parsers;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.xpath.XPathExpression;
import org.mule.tools.apikit.input.APIKitFlow;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static org.jdom2.filter.Filters.element;
import static org.jdom2.xpath.XPathFactory.instance;
import static org.mule.tools.apikit.input.APIKitFlow.buildFromName;
import static org.mule.tools.apikit.misc.FlowNameUtils.decode;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;

public class APIKitFlowsParser implements MuleConfigFileParser<Set<ResourceActionMimeTypeTriplet>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(APIKitFlowsParser.class.getName());

  private final Map<String, ApikitMainFlowContainer> includedApis;

  public APIKitFlowsParser(Map<String, ApikitMainFlowContainer> includedApis) {
    this.includedApis = includedApis;
  }

  @Override
  public Set<ResourceActionMimeTypeTriplet> parse(Document document) {
    Set<ResourceActionMimeTypeTriplet> entries = new HashSet<>();
    XPathExpression<Element> xp = instance().compile("//*/*[local-name()='flow']",
                                                     element(XMLNS_NAMESPACE.getNamespace()));
    List<Element> elements = xp.evaluate(document);
    for (Element element : elements) {
      String name = decode(element.getAttributeValue("name"));
      try {
        APIKitFlow flow = buildFromName(name, includedApis.keySet());
        ApikitMainFlowContainer api = includedApis.get(flow.getConfigRef());

        if (api == null) {
          throw new IllegalStateException(format("No APIKit configuration found for flow %s", name));
        }

        if (api.getPath() == null) {
          throw new IllegalStateException("Api path is invalid");
        }

        String resource = flow.getResource().startsWith("/") ? flow.getResource() : "/" + flow.getResource();
        String path = APIKitTools.getCompletePathFromBasePathAndPath(api.getHttpListenerConfig().getBasePath(), api.getPath());
        entries.add(new ResourceActionMimeTypeTriplet(api, path + resource, flow.getAction(), flow.getMimeType()));

      } catch (IllegalArgumentException e) {
        LOGGER.info("Flow named '" + name + "' is not an APIKit Flow because it does not follow APIKit naming convention.");
      }
    }
    return entries;
  }
}
