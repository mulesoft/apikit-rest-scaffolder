package org.mule.tools.apikit.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

public class MuleDomainFactory {

  private static final String MULE_ARTIFACT_LOCATION_IN_JAR = "META-INF/mule-artifact/mule-artifact.json";
  private static final String MULE_DOMAIN_DEFAULT_CONFIG_FILE_NAME = "mule-domain-config.xml";

  public static MuleDomain fromDeployableArtifact(File artifact) throws Exception {
    JarFile jarArtifact = new JarFile(artifact);
    InputStream muleArtifacts = jarArtifact.getInputStream(jarArtifact.getEntry(MULE_ARTIFACT_LOCATION_IN_JAR));
    MuleDomainModelJsonSerializer serializer = new MuleDomainModelJsonSerializer();
    MuleDomainModel domainModel = serializer.deserialize(IOUtils.toString(muleArtifacts));

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
    List<HttpListenerConfig> httpListenerConfigs = new ArrayList<>();
    try (InputStream content = artifact.getInputStream(artifact.getEntry(configFile))) {
      Document contentAsDocument = new SAXBuilder().build(content);
      httpListenerConfigs.addAll(new HttpListenerConfigParser().parse(contentAsDocument));
    }
    return httpListenerConfigs;
  }

}
