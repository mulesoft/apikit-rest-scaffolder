/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.apikit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.Helper.countOccurences;

import org.apache.commons.io.FileUtils;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.apikit.model.*;

public class ConsoleFlowTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    folder.newFolder("console-flow");
  }

  @Test
  public void scaffoldWithoutMuleConfigs() throws Exception {
    File ramlFile = getFile("console-flow/simple-console.raml");
    MainAppScaffolder mainAppScaffolder = getScaffolder();
    ScaffoldingConfiguration configuration = getScaffolderConfiguration(getFile("console-flow/simple.xml"), ramlFile);
    ScaffoldingResult result = mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
  }

  @Test
  public void testAlreadyExistWithConsole() throws Exception {
    File xmlFile = getFile("console-flow/simple.xml");
    File ramlFile = getFile("console-flow/simple-console.raml");

    MainAppScaffolder mainAppScaffolder = getScaffolder();
    ScaffoldingConfiguration configuration = getScaffolderConfiguration(xmlFile, ramlFile);
    ScaffoldingResult result = mainAppScaffolder.run(configuration);

    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());

    MuleConfig generatedConfig = result.getGeneratedConfigs().get(0);
    assertEquals("HTTP_Listener_Configuration", generatedConfig.getHttpListenerConfigs().get(0).getName());

    String s = IOUtils.toString(generatedConfig.getContent());

    assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\""));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
    assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
    assertEquals(1, countOccurences(s, "get:\\pet:simpleV10-config"));
    assertEquals(2, countOccurences(s, "post:\\pet:simpleV10-config"));
    assertEquals(1, countOccurences(s, "get:\\:simpleV10-config\""));
    assertEquals(2, countOccurences(s, "get:\\users"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(0, countOccurences(s, "<flow name=\"simple-enabled-console\">"));
    assertEquals(0, countOccurences(s, "apikit:console"));
    assertEquals(2, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private MainAppScaffolder getScaffolder() {
    ScaffolderContext context = new ScaffolderContext.Builder()
        .withRuntimeEdition(RuntimeEdition.EE)
        .build();
    return new MainAppScaffolder(context);
  }

  private ScaffoldingConfiguration getScaffolderConfiguration(File xmlFile, File ramlFile) throws Exception {
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFile.toURI()));

    if (!parseResult.success()) {
      throw new RuntimeException("Could not parse " + ramlFile.getName());
    }

    List<MuleConfig> muleConfigs = new ArrayList<>();

    if (xmlFile != null) {
      InputStream is = FileUtils.openInputStream(xmlFile);
      muleConfigs.add(MuleConfigBuilder.fromStream(is));
    }

    return new ScaffoldingConfiguration.Builder()
        .withApi(parseResult.get())
        .withMuleConfigurations(muleConfigs)
        .build();
  }

  private File getFile(String s) throws Exception {
    if (s == null) {
      return null;
    }
    File file = folder.newFile(s);
    file.createNewFile();
    InputStream resourceAsStream = ScaffolderMule4Test.class.getClassLoader().getResourceAsStream(s);
    IOUtils.copy(resourceAsStream,
                 new FileOutputStream(file));
    return file;
  }
}
