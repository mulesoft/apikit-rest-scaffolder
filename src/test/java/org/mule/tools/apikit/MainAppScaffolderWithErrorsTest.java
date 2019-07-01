/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.model.*;

import java.io.File;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MainAppScaffolderWithErrorsTest {

  private static final String xmlsDirectory = "scaffolder-with-errors";
  private static final String apiPath = "scaffolder" + File.separator + "simple.raml";
  private ApiSpecification apiSpec;

  @Before
  public void setUp() {
    ApiReference apiReference = ApiReference.create(apiPath);
    ParseResult parseResult = new ParserService().parse(apiReference);
    assertTrue(parseResult.success());
    this.apiSpec = parseResult.get();
  }

  @Test
  public void shouldFailDueToMissingListenersOnMainFlow() throws Exception {
    ScaffoldingResult result = scaffoldApi("main-flow-without-listeners");
    String expectedErrorReason = "The main flow must have an inbound-endpoint or listener";
    assertFalse(result.isSuccess());
    assertEquals(expectedErrorReason, result.getErrors().get(0).getReason());
  }

  @Test
  public void shouldFailDueToMissingApikitEntriesInMuleConfig() throws Exception {
    ScaffoldingResult result = scaffoldApi("config-without-apikit-entries");
    String expectedErrorReason = "No APIKit entries found in Mule config";
    assertFalse(result.isSuccess());
    assertEquals(expectedErrorReason, result.getErrors().get(0).getReason());
  }

  @Test
  public void shouldFailDueToMissingAddressOnInboundEndpoint() throws Exception {
    ScaffoldingResult result = scaffoldApi("missing-address-on-inbound-endpoint");
    String expectedErrorReason = "Neither 'path' nor 'address' attribute was used. Cannot retrieve base URI.";
    assertFalse(result.isSuccess());
    assertEquals(expectedErrorReason, result.getErrors().get(0).getReason());
  }

  @Test
  public void shouldFailDueToInvalidHttpListenerConfigReference() throws Exception {
    ScaffoldingResult result = scaffoldApi("invalid-http-listener-config-ref");
    String expectedErrorReason = "An HTTP Listener configuration is mandatory.";
    assertFalse(result.isSuccess());
    assertEquals(expectedErrorReason, result.getErrors().get(0).getReason());
  }

  private ScaffoldingResult scaffoldApi(String existingMuleConfigName) throws Exception {
    String existingMuleConfigPath = xmlsDirectory + File.separator + existingMuleConfigName + ".xml";
    ScaffolderContext context = ScaffolderContext.builder().build();
    MuleConfig existingMuleConfig = MuleConfigBuilder.fromStream(TestUtils.getResourceAsStream(existingMuleConfigPath));
    ScaffoldingConfiguration scaffoldingConfiguration = ScaffoldingConfiguration.builder()
        .withApi(apiSpec)
        .withMuleConfigurations(Lists.newArrayList(existingMuleConfig))
        .build();

    Scaffolder mainAppScaffolder = new MainAppScaffolder(context);
    return mainAppScaffolder.run(scaffoldingConfiguration);
  }
}
