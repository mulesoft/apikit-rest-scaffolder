/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.mule.tools.apikit.output.scopes.Scope;

public class Flow implements Scope {

  protected Element content;

  public Flow(Element content) {
    this.content = content;
  }

  public Element generate() {
    return content;
  }

  public String getName() {
    Attribute nameAttribute = content.getAttribute("name");

    if(nameAttribute == null || nameAttribute.getValue().isEmpty()) {
      throw new RuntimeException("Flow name is required");
    }

    return nameAttribute.getValue();
  }
}
