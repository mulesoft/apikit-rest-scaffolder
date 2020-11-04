/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.junit.Test;
import org.mule.tools.apikit.TestUtils;
import org.mule.tools.apikit.input.parsers.APIAutodiscoveryConfigParser;
import org.mule.tools.apikit.input.parsers.ConfigurationPropertiesConfigParser;
import org.mule.tools.apikit.model.APIAutodiscoveryConfig;
import org.mule.tools.apikit.model.ConfigurationPropertiesConfig;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ConfigurationPropertiesConfigParserTest {


  @Test
  public void test() throws JDOMException, IOException {
    Document documentFromStream = TestUtils
        .getDocumentFromStream(TestUtils.getResourceAsStream("configuration-properties-parser/file-with-configuration.xml"));
    List<ConfigurationPropertiesConfig> configurationPropertiesConfigs =
        new ConfigurationPropertiesConfigParser().parse(documentFromStream);
    ConfigurationPropertiesConfig configurationPropertiesConfig = configurationPropertiesConfigs.stream().findFirst().get();
    assertEquals(configurationPropertiesConfig.getFile(), "dev-configuration.yaml");
  }

  @Test(expected = RuntimeException.class)
  public void testFailure() throws JDOMException, IOException, RuntimeException {
    Document documentFromStream = TestUtils
        .getDocumentFromStream(TestUtils.getResourceAsStream("configuration-properties-parser/file-without-configuration.xml"));
    try {
      new ConfigurationPropertiesConfigParser().parse(documentFromStream);
    } catch (RuntimeException ex) {
      assertEquals(ex.getMessage(), "file is a mandatory field");
      throw ex;
    }
  }

}
