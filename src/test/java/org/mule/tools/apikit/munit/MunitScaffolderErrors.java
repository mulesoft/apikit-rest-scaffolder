/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.munit;

import org.junit.Test;
import org.mule.tools.apikit.MunitScaffolder;
import org.mule.tools.apikit.model.ScaffoldingResult;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.mule.tools.apikit.MunitScaffolder.*;
import static org.mule.tools.apikit.MunitScaffolder.NO_APIKIT_CONFIGS_ERROR_TEMPLATE;
import static org.mule.tools.apikit.MunitScaffolder.NO_MAIN_FLOWS_ERROR_TEMPLATE;

public class MunitScaffolderErrors extends AbstractMunitScaffolderTest {

  private static final String RAML_NAME = "simple";
  private static final String RESOURCE_FOLDER = "scaffolder";

  @Test
  public void twoApikitConfigsReferencingTheSameAPI() throws Exception {
    String muleConfigLocation = "two-apikit-configs-referencing-same-raml.xml";
    String expectedErrorMessage = String.format(MULTIPLE_APIKIT_CONFIGS_ERROR_TEMPLATE, "scaffolder/simple.yaml");
    makeAssertion(muleConfigLocation, expectedErrorMessage);
  }

  @Test
  public void twoMainFlowsReferencingTheSameConfig() throws Exception {
    String muleConfigLocation = "two-main-flows-referencing-same-config.xml";
    String expectedErrorMessage = String.format(MULTIPLE_MAIN_FLOWS_ERROR_TEMPLATE, "api-config");
    makeAssertion(muleConfigLocation, expectedErrorMessage);
  }

  @Test
  public void noApikitConfigFound() throws Exception {
    String muleConfigLocation = "missing-apikit-config-for-raml.xml";
    String expectedErrorMessage = String.format(NO_APIKIT_CONFIGS_ERROR_TEMPLATE, "scaffolder/simple.yaml");
    makeAssertion(muleConfigLocation, expectedErrorMessage);
  }

  @Test
  public void noMainFlowForApikitConfig() throws Exception {
    String muleConfigLocation = "missing-main-flow-for-apikit-config.xml";
    String expectedErrorMessage = String.format(NO_MAIN_FLOWS_ERROR_TEMPLATE, "api-config");
    makeAssertion(muleConfigLocation, expectedErrorMessage);
  }

  @Test
  public void mainFlowMissingName() throws Exception {
    String muleConfigLocation = "main-flow-missing-name.xml";
    String expectedErrorMessage = "Flow name is required";
    makeAssertion(muleConfigLocation, expectedErrorMessage);
  }

  public void makeAssertion(String muleConfigLocation, String expectedErrorMessage) throws Exception {
    ScaffoldingResult result = scaffold(RAML_NAME, RESOURCE_FOLDER, false, muleConfigLocation);
    assertFalse(result.isSuccess());
    assertEquals(result.getErrors().get(0).getReason(), expectedErrorMessage);
  }
}
