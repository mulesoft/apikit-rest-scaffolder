/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.apikit.TestUtils;
import org.mule.tools.apikit.input.parsers.APIAutodiscoveryConfigParser;
import org.mule.tools.apikit.model.APIAutodiscoveryConfig;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class APIAutodiscoveryConfigParserTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testCorrectParsingWithAllFields() throws JDOMException, IOException {
    Document documentFromStream =
        TestUtils.getDocumentFromStream(TestUtils.getResourceAsStream("api-autodiscovery-parser/file-with-autodiscovery.xml"));
    List<APIAutodiscoveryConfig> apiAutodiscoveryConfigs = new APIAutodiscoveryConfigParser().parse(documentFromStream);
    APIAutodiscoveryConfig apiAutodiscoveryConfig = apiAutodiscoveryConfigs.stream().findFirst().get();
    assertEquals(apiAutodiscoveryConfig.getApiId(), "123");
    assertEquals(apiAutodiscoveryConfig.getFlowRef(), "new-api-main");
    assertEquals(apiAutodiscoveryConfig.getIgnoreBasePath(), Boolean.TRUE);
  }

  @Test
  public void testFailureParsingWithoutAPIId() throws JDOMException, IOException, RuntimeException {
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("apiId is a mandatory field");
    Document documentFromStream =
        TestUtils.getDocumentFromStream(TestUtils.getResourceAsStream("api-autodiscovery-parser/file-without-autodiscovery.xml"));
    new APIAutodiscoveryConfigParser().parse(documentFromStream);
  }
}
