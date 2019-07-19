/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

public class ScaffolderContextBuilder {

  private RuntimeEdition runtimeEdition = RuntimeEdition.CE;
  private boolean shouldCreateMunitResources = false;
  private String munitSuiteName;

  public static ScaffolderContextBuilder builder() {
    return new ScaffolderContextBuilder();
  }

  public ScaffolderContextBuilder withRuntimeEdition(RuntimeEdition runtimeEdition) {
    this.runtimeEdition = runtimeEdition;
    return this;
  }

  public ScaffolderContextBuilder shouldCreateMunitResources(boolean shouldCreateMunitResources) {
    this.shouldCreateMunitResources = shouldCreateMunitResources;
    return this;
  }

  public ScaffolderContextBuilder withMunitSuiteName(String munitSuiteName) {
    this.munitSuiteName = munitSuiteName;
    return this;
  }

  public ScaffolderContext build() {
    if (!shouldCreateMunitResources && munitSuiteName == null) {
      return new ScaffolderContext(runtimeEdition);
    }
    return new MunitScaffolderContext(runtimeEdition, shouldCreateMunitResources, munitSuiteName);
  }
}
