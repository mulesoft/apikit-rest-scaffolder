/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model.munit3;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Models Http methods
 *
 * @author Mulesoft Inc.
 */
public enum HttpMethod {

  GET("get"), POST("post"), PUT("put"), DELETE("delete"), HEAD("head"), PATCH("patch"), OPTIONS("options"), TRACE("trace"),
  CONNECT("connect");

  private String name;

  HttpMethod(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static boolean isValidAction(String name) {
    return find(name).isPresent();
  }

  public static Optional<HttpMethod> find(String name) {
    return Stream.of(values()).filter(httpMethod -> httpMethod.getName().equals(name.toLowerCase())).findAny();
  }

  public static boolean isUpdateAction(String methodName) {
    Optional<HttpMethod> methodOptional = HttpMethod.find(methodName);
    return methodOptional.map(method -> (POST.equals(method) || PUT.equals(method) || PATCH.equals(method))).orElse(false);
  }

}
