/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.apikit.output.scopes;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;

import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;

public class ConsoleFlowScope implements Scope {

  private final Element consoleFlow;


  public ConsoleFlowScope(ApikitMainFlowContainer api, boolean isMuleEE) {
    consoleFlow = new Element("flow", XMLNS_NAMESPACE.getNamespace());
    consoleFlow.setAttribute("name", api.getId() + "-" + "console");

    String httpListenerConfigRef = api.getHttpListenerConfig().getName();
    consoleFlow.addContent(MainFlowsUtils.generateListenerSource(httpListenerConfigRef, ApikitMainFlowContainer.DEFAULT_CONSOLE_PATH));

    Element restProcessor = new Element("console", APIKitTools.API_KIT_NAMESPACE.getNamespace());
    String configRef = api.getConfig() != null ? api.getConfig().getName() : null;
    if (!StringUtils.isEmpty(configRef)) {
      restProcessor.setAttribute("config-ref", configRef);
    }
    consoleFlow.addContent(restProcessor);

    Element errorHandler = ErrorHandlerScope.createForConsoleFlow(isMuleEE).generate();
    consoleFlow.addContent(errorHandler);
  }

  @Override
  public Element generate() {
    return consoleFlow;
  }
}
