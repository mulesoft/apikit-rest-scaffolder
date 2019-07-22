/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.mule.tools.apikit.input.parsers.HttpListenerConfigParser;

public class MuleDomain implements NamedContent, WithConfigs {

  private static final String MULE_DOMAIN_CONFIG_FILE_NAME = "mule-domain-config.xml";

  private InputStream content;
  private List<HttpListenerConfig> configurations;

  private MuleDomain(InputStream content, List<HttpListenerConfig> configurations) {
    this.content = content;
    this.configurations = configurations;
  }

  public String getName() {
    return "APP_DOMAIN";
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

  public static MuleDomain fromDeployableArtifact(File artifact) throws Exception {
    try (URLClassLoader cl = new URLClassLoader(new URL[] {artifact.toURI().toURL()}, MuleDomain.class.getClassLoader())) {
      try (InputStream domainFileIS = cl.getResourceAsStream(MULE_DOMAIN_CONFIG_FILE_NAME)) {
        // This is necessary to prevent leaking file handles
        InputStream content = cloneInputStream(domainFileIS);
        Document contentAsDocument = new SAXBuilder().build(content);
        List<HttpListenerConfig> httpListenerConfigs = new HttpListenerConfigParser().parse(contentAsDocument);
        return new MuleDomain(content, httpListenerConfigs);
      }
    }
  }

  private static InputStream cloneInputStream(InputStream toClone) throws IOException {
    ByteArrayOutputStream middleMan = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int len;
    while ((len = toClone.read(buffer)) > -1) {
      middleMan.write(buffer, 0, len);
    }
    return new ByteArrayInputStream(middleMan.toByteArray());
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

    public MuleDomain build() {
      return new MuleDomain(content, configurations);
    }
  }
}
