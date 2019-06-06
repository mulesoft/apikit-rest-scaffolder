/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.mule.tools.apikit.input.parsers.HttpListenerConfigParser;

import java.io.InputStream;
import java.util.List;

public class MuleDomain implements NamedContent, WithConfigs {

  private String name;
  private InputStream content;
  private List<HttpListenerConfig> configurations;

  private MuleDomain(InputStream content, List<HttpListenerConfig> configurations) {
    this.content = content;
    this.configurations = configurations;
  }

  public String getName() {
    return name;
  }

  @Override
  public InputStream getContent() {
    return content;
  }

  @Override
  public List<HttpListenerConfig> getHttpListenerConfigs() {
    return configurations;
  }

  public static MuleDomain fromInputStream(InputStream content) throws Exception {
    Document contentAsDocument = new SAXBuilder().build(content);
    List<HttpListenerConfig> httpListenerConfigs = new HttpListenerConfigParser().parse(contentAsDocument);
    return new MuleDomain(content, httpListenerConfigs);
  }
}
