/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.jdom2.Element;

public class MainFlow extends Flow {

  ApikitRouter router;

  public MainFlow(Element element) {
    super(element);
  }

  public ApikitRouter getApikitRouter() {
    return router;
  }

  public void setApikitRouter(ApikitRouter router) {
    this.router = router;
  }
}
