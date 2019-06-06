/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model.munit3;

import java.util.Optional;

/**
 * HTTP Response for the {@link TestScenario}
 *
 * @author Mulesoft Inc.
 */
public interface TestResponse {

  /**
   * Status code from response to validate
   *
   * @return Status code to expect. Empty if not specified
   */
  Optional<String> getStatusCode();

  /**
   * Content type from response to validate
   *
   * @return Content type to expect. Empty if not specified
   */
  Optional<String> getContentType();

  /**
   * Content from response to validate
   *
   * @return Content to expect. Empty if not specified
   */
  Optional<Object> getContent();

}
