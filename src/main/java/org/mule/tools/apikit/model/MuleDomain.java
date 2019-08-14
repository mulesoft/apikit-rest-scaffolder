/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.mule.tools.apikit.input.parsers.HttpListenerConfigParser;

public class MuleDomain implements NamedContent, WithConfigs {

  private List<HttpListenerConfig> configurations;

  MuleDomain(List<HttpListenerConfig> configurations) {
    this.configurations = configurations;
  }

  public String getName() {
    return "APP_DOMAIN";
  }

  @Override
  public InputStream getContent() {
    return null;
  }

  @Override
  public List<HttpListenerConfig> getHttpListenerConfigs() {
    return Collections.unmodifiableList(configurations);
  }

  public static MuleDomain fromInputStream(InputStream content) throws Exception {
    Document contentAsDocument = new SAXBuilder().build(content);
    List<HttpListenerConfig> httpListenerConfigs = new HttpListenerConfigParser().parse(contentAsDocument);
    return new MuleDomain(httpListenerConfigs);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private InputStream content;

    public Builder() {}

    public Builder withContent(InputStream content) {
      this.content = content;
      return this;
    }

    public MuleDomain build() {
      if (content == null) {
        return new MuleDomain(new ArrayList<>());
      } else {
        try {
          return fromInputStream(content);
        } catch (Exception e) {
          e.printStackTrace();
          return new MuleDomain(new ArrayList<>());
        }
      }
    }
  }
}
