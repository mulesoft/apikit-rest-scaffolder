/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import org.jdom2.Element;

import static org.mule.tools.apikit.output.MunitTestSuiteGenerator.MUNIT_NAMESPACE;

public class MunitConfigScope implements Scope {

  private String name;

  public MunitConfigScope(String name) {
    this.name = name;
  }

  @Override
  public Element generate() {
    Element element = new Element("config", MUNIT_NAMESPACE.getNamespace());
    element.setAttribute("name", name);
    return element;
  }
}
