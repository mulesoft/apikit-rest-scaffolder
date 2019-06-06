/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model.munit3;

import java.util.Set;

public interface TestSuite {

  /**
   * Name corresponding to the ApikitMainFlowContainer test suite
   *
   * @return Name of the test suite
   */
  String getName();

  /**
   * Base path for all {@link TestScenario}
   *
   * @return Base path to be used for HTTP Requests
   */
  String getBasePath();

  /**
   * HTTP Port for all {@link TestScenario}
   *
   * @return Port to be used for HTTP Requests
   */
  int getPort();

  /**
   * HTTP Host for all {@link TestScenario}
   *
   * @return Host name to be used for HTTP Requests
   */
  String getHost();


  /**
   * ApikitMainFlowContainer Test Scenarios that represent the whole test suite
   *
   * @return List of {@link TestScenario}
   */
  Set<TestScenario> getTestScenarios();

}
