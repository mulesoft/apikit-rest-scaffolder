/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mule.runtime.api.deployment.meta.MuleDomainModel;
import org.mule.runtime.api.deployment.persistence.MuleDomainModelJsonSerializer;
import org.mule.tools.apikit.input.parsers.HttpListenerConfigParser;
import org.mule.tools.apikit.misc.APIKitTools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

public class MuleDomainFactory {

  private static final String MULE_ARTIFACT_LOCATION_IN_JAR = "META-INF/mule-artifact/mule-artifact.json";
  private static final String MULE_DOMAIN_DEFAULT_CONFIG_FILE_NAME = "mule-domain-config.xml";
  private static final MuleDomainModelJsonSerializer serializer = new MuleDomainModelJsonSerializer();
  private static final HttpListenerConfigParser configParser = new HttpListenerConfigParser();

  public static MuleDomain fromDeployableArtifact(File artifact) throws Exception {
    JarFile jarArtifact = new JarFile(artifact);
    InputStream muleArtifacts = jarArtifact.getInputStream(jarArtifact.getEntry(MULE_ARTIFACT_LOCATION_IN_JAR));
    MuleDomainModel domainModel = serializer.deserialize(APIKitTools.readContents(muleArtifacts));

    Set<String> configs = domainModel.getConfigs();

    if (configs.isEmpty()) {
      List<HttpListenerConfig> httpListenerConfigs =
          parseHttpListenerConfigsFromConfigFile(jarArtifact, MULE_DOMAIN_DEFAULT_CONFIG_FILE_NAME);
      return new MuleDomain(httpListenerConfigs);
    } else {
      List<HttpListenerConfig> httpListenerConfigs = new ArrayList<>();
      for (String config : configs) {
        httpListenerConfigs.addAll(parseHttpListenerConfigsFromConfigFile(jarArtifact, config));
      }
      return new MuleDomain(httpListenerConfigs);
    }
  }

  private static List<HttpListenerConfig> parseHttpListenerConfigsFromConfigFile(JarFile artifact, String configFile)
      throws JDOMException, IOException {
    List<HttpListenerConfig> httpListenerConfigs;
    SAXBuilder builder = MuleConfigBuilder.getSaxBuilder();
    try (InputStream content = artifact.getInputStream(artifact.getEntry(configFile))) {
      Document contentAsDocument = builder.build(content);
      httpListenerConfigs = configParser.parse(contentAsDocument);
    }
    return httpListenerConfigs;
  }

}
