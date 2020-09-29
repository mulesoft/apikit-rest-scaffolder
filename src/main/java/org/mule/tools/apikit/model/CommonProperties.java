/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

public class CommonProperties {

  private HttpCommonProperty http;

  public CommonProperties() {}

  public CommonProperties(HttpCommonProperty http) {
    this.http = http;
  }

  public HttpCommonProperty getHttp() {
    return http;
  }

  public void setHttp(HttpCommonProperty http) {
    this.http = http;
  }
}
