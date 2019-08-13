/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.jdom2.Attribute;
import org.jdom2.Element;

public class ApikitRouter {

  private Element content;

  public ApikitRouter(Element content) {
    this.content = content;
  }

  public Element getContent() {
    return content;
  }

  public String getConfigRef() {
    Attribute configRefAttribute = content.getAttribute("config-ref");

    if (configRefAttribute == null) {
      throw new RuntimeException("Apikit router not found");
    }
    if (configRefAttribute.getValue().isEmpty()) {
      throw new RuntimeException("Apikit router is not referencing any config");
    }

    return configRefAttribute.getValue();
  }
}
