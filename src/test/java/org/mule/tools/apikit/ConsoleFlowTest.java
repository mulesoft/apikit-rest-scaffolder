/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.apikit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleConfigBuilder;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffolderContextBuilder;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.Helper.countOccurences;

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
    ScaffoldingConfiguration configuration = getScaffolderConfiguration(getFile("console-flow/simple.xml"), ramlFile, true);
    ScaffoldingResult result = mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
  }

  @Test
  public void testAlreadyExistWithConsole() throws Exception {
    File xmlFile = getFile("console-flow/simple.xml");
    File ramlFile = getFile("console-flow/simple-console.raml");

    MainAppScaffolder mainAppScaffolder = getScaffolder();
    ScaffoldingConfiguration configuration = getScaffolderConfiguration(xmlFile, ramlFile, false);
    ScaffoldingResult result = mainAppScaffolder.run(configuration);

    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());

    MuleConfig generatedConfig = result.getGeneratedConfigs().get(0);
    assertEquals("HTTP_Listener_Configuration", generatedConfig.getHttpListenerConfigs().get(0).getName());

    String s = APIKitTools.readContents(generatedConfig.getContent());
    XmlOccurrencesAsserterBuilder.XmlOccurrencesAsserter xmlOccurrencesAsserter = new XmlOccurrencesAsserterBuilder()
        .withHttpResponseStatusCode200Count(1)
        .withHttpHeadersOutboundHeadersDefaultCount(1)
        .withEESetPayloadTagCount(2)
        .withHttpHeadersCount(2)
        .withHttplListenerConfigCount(1)
        .withHttplListenerCount(1)
        .withLoggerInfoCount(2)
        .build();
    xmlOccurrencesAsserter.assertOccurrences(s);
    assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\""));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
    assertEquals(1, countOccurences(s, "get:\\pet:simpleV10-config"));
    assertEquals(2, countOccurences(s, "post:\\pet:simpleV10-config"));
    assertEquals(1, countOccurences(s, "get:\\:simpleV10-config\""));
    assertEquals(2, countOccurences(s, "get:\\users"));
    assertEquals(0, countOccurences(s, "<flow name=\"simple-enabled-console\">"));
  }

  // revisar test
  private MainAppScaffolder getScaffolder() {
    ScaffolderContext context = ScaffolderContextBuilder.builder()
        .withRuntimeEdition(RuntimeEdition.EE)
        .build();
    return new MainAppScaffolder(context);
  }

  private ScaffoldingConfiguration getScaffolderConfiguration(File xmlFile, File ramlFile, boolean httpPersisted)
      throws Exception {
    ParseResult parseResult = new ParserService().parse(ApiReference.create(ramlFile.toURI()));

    if (!parseResult.success()) {
      throw new RuntimeException("Could not parse " + ramlFile.getName());
    }

    List<MuleConfig> muleConfigs = new ArrayList<>();

    if (xmlFile != null) {
      InputStream is = FileUtils.openInputStream(xmlFile);
      muleConfigs.add(MuleConfigBuilder.fromStream(is, httpPersisted));
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
    try (InputStream resourceAsStream = ConsoleFlowTest.class.getClassLoader().getResourceAsStream(s);
        OutputStream outputStream = new FileOutputStream(file)) {
      IOUtils.copy(resourceAsStream, outputStream);
    }
    return file;
  }
}
