/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.util.Map;

public class Properties {

  private String format;
  private Map<String, Map<String, Object>> files;

  public Properties(String format, Map<String, Map<String, Object>> files) {
    this.format = format;
    this.files = files;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public Map<String, Map<String, Object>> getFiles() {
    return files;
  }

  public void setFiles(Map<String, Map<String, Object>> files) {
    this.files = files;
  }
}
