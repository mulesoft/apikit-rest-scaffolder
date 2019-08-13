/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mule.runtime.api.deployment.meta.MuleDomainModel;
import org.mule.runtime.api.deployment.persistence.MuleDomainModelJsonSerializer;
import org.mule.tools.apikit.input.parsers.HttpListenerConfigParser;

public class MuleDomain implements NamedContent, WithConfigs {

  private static final String MULE_ARTIFACT_LOCATION_IN_JAR = "META-INF/mule-artifact/mule-artifact.json";
  private static final String MULE_DOMAIN_DEFAULT_CONFIG_FILE_NAME = "mule-domain-config.xml";

  private List<HttpListenerConfig> configurations;

  private MuleDomain(List<HttpListenerConfig> configurations) {
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

  public static MuleDomain fromDeployableArtifact(File artifact) throws Exception {
    JarFile jarArtifact = new JarFile(artifact);
    InputStream muleArtifacts = jarArtifact.getInputStream(jarArtifact.getEntry(MULE_ARTIFACT_LOCATION_IN_JAR));
    MuleDomainModelJsonSerializer serializer = new MuleDomainModelJsonSerializer();
    MuleDomainModel domainModel = serializer.deserialize(IOUtils.toString(muleArtifacts));

    Set<String> configs = domainModel.getConfigs();

    if (configs.isEmpty()) {
      List<HttpListenerConfig> httpListenerConfigs = new ArrayList<>();
      parseHttpListenerConfigsFromConfigFile(jarArtifact, MULE_DOMAIN_DEFAULT_CONFIG_FILE_NAME, httpListenerConfigs);
      return new MuleDomain(httpListenerConfigs);
    } else {
      List<HttpListenerConfig> httpListenerConfigs = new ArrayList<>();
      for (String config : configs) {
        parseHttpListenerConfigsFromConfigFile(jarArtifact, config, httpListenerConfigs);
      }
      return new MuleDomain(httpListenerConfigs);
    }
  }

  private static void parseHttpListenerConfigsFromConfigFile(JarFile artifact, String configFile,
                                                             List<HttpListenerConfig> httpListenerConfigs)
      throws JDOMException, IOException {
    try (InputStream content = artifact.getInputStream(artifact.getEntry(configFile))) {
      Document contentAsDocument = new SAXBuilder().build(content);
      httpListenerConfigs.addAll(new HttpListenerConfigParser().parse(contentAsDocument));
    }
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
