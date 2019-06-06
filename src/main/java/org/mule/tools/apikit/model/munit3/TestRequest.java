/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model.munit3;

import java.util.Map;
import java.util.Optional;

/**
 * HTTP Request for an {@link TestScenario}
 *
 * @author Mulesoft Inc.
 */
public interface TestRequest {

  /**
   * Relative path to the {@link TestSuite#getBasePath()}
   *
   * @return Relative path for the HTTP Request
   */
  String getPath();


  /**
   * HTTP Method which is represented by one of {@link HttpMethod}
   *
   * @return Method for the HTTP Request
   */
  String getMethod();

  /**
   * HTTP Headers represented by a name and a value
   *
   * @return Headers for the HTTP Request
   */
  Map<String, Object> getHeaders();

  /**
   * Http URI Parameters represented by a name and a value
   *
   * @return Uri parameters for the HTTP Request
   */
  Map<String, Object> getUriParameters();

  /**
   * HTTP Query Parameters represented by a name and a value
   *
   * @return Query parameters for the HTTP Request
   */
  Map<String, Object> getQueryParameters();

  /**
   * Content type to be sent through the HTTP Request
   *
   * @return Content type of the body for the HTTP Request. Empty if not required
   */
  Optional<String> getContentType();

  /**
   * Content to be sent through the HTTP Request
   *
   * @return Content of the body for the HTTP Request. Empty if not required
   */
  Optional<Object> getContent();

}
