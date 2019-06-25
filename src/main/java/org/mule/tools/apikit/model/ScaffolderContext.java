/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

public class ScaffolderContext {

  private RuntimeEdition runtimeEdition;
  private boolean shouldCreateMunitResources;
  private String munitSuiteName;
  private String apikitMainFlowName;

  private ScaffolderContext(RuntimeEdition runtimeEdition, boolean shouldCreateMunitResources, String munitSuiteName,
                            String apikitMainFlowName) {
    this.runtimeEdition = runtimeEdition;
    this.shouldCreateMunitResources = shouldCreateMunitResources;
    this.munitSuiteName = munitSuiteName;
    this.apikitMainFlowName = apikitMainFlowName;
  }

  public RuntimeEdition getRuntimeEdition() {
    return runtimeEdition;
  }

  public static Builder builder() {
    return new Builder();
  }

  public boolean shouldCreateMunitResources() {
    return shouldCreateMunitResources;
  }

  public String getApikitMainFlowName() {
    return apikitMainFlowName;
  }

  public String getMunitSuiteName() {
    return munitSuiteName;
  }

  public static class Builder {

    private RuntimeEdition runtimeEdition = RuntimeEdition.CE;
    private boolean shouldCreateMunitResources = false;
    private String munitSuiteName;
    private String apikitMainFlowName;

    public Builder withRuntimeEdition(RuntimeEdition runtimeEdition) {
      this.runtimeEdition = runtimeEdition;
      return this;
    }

    public Builder shouldCreateMunitResources(boolean shouldCreateMunitResources) {
      this.shouldCreateMunitResources = shouldCreateMunitResources;
      return this;
    }

    public Builder withMunitSuiteName(String suiteName) {
      munitSuiteName = suiteName;
      return this;
    }

    public Builder withApikitMainFlowName(String apikitMainFlowName) {
      this.apikitMainFlowName = apikitMainFlowName;
      return this;
    }

    public ScaffolderContext build() {
      return new ScaffolderContext(runtimeEdition, shouldCreateMunitResources, munitSuiteName, apikitMainFlowName);
    }
  }
}
