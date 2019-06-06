/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model.munit3;

import java.util.Set;

/**
 * A Test Scenario for the ApikitMainFlowContainer definition
 *
 * @author Mulesoft Inc.
 */
public interface TestScenario {


  /**
   * Name identifier for the test scenario
   *
   * @return Name of the test scenario
   */
  String getName();


  /**
   * Describes the Api Test Scenario
   *
   * @return Description of the test scenario
   */
  String getDescription();

  /**
   * Request to be sent for the test scenario
   *
   * @return Request of the test scenario
   */
  TestRequest getRequest();

  /**
   * Response to expect for the test scenario result of the {@link TestScenario#getRequest()}
   *
   * @return Response of the test scenario
   */
  TestResponse getResponse();

  // TODO Check if this will still be needed MUSP-591
  /**
   * Flows being tested by the test scenario
   *
   * @return Flows being tested
   */
  Set<String> getFlows();

}
