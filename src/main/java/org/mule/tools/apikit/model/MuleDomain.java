/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mule.tools.apikit.input.parsers.HttpListenerConfigParser;

public class MuleDomain implements WithConfigs {

  private List<HttpListenerConfig> configurations;

  MuleDomain(List<HttpListenerConfig> configurations) {
    this.configurations = configurations;
  }

  public String getName() {
    return "APP_DOMAIN";
  }

  @Override
  public List<HttpListenerConfig> getHttpListenerConfigs() {
    return Collections.unmodifiableList(configurations);
  }

  /**
   * @param content stream will be closed after parsing model
   */
  public static MuleDomain fromInputStream(InputStream content) throws Exception {
    return new MuleDomain(parseHttpListenerConfigs(content));
  }

  private static List<HttpListenerConfig> parseHttpListenerConfigs(InputStream content) throws JDOMException, IOException {
    Document contentAsDocument = new SAXBuilder().build(content);
    content.close();
    return new HttpListenerConfigParser().parse(contentAsDocument);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private InputStream content;
    private List<HttpListenerConfig> configurations;

    public Builder() {
      this.configurations = new ArrayList<>();
    }

    public Builder withContent(InputStream content) {
      this.content = content;
      return this;
    }

    public Builder withConfigurations(List<HttpListenerConfig> configurations) {
      this.configurations = configurations;
      return this;
    }

    public MuleDomain build() throws Exception {
      if (this.content != null && this.configurations == null) {
        this.configurations = MuleDomain.parseHttpListenerConfigs(content);
      }
      return new MuleDomain(configurations);
    }
  }
}
