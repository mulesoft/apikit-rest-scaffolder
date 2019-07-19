/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

public class MunitScaffolderContext extends ScaffolderContext {

  private boolean shouldCreateMunitResources;
  private String munitSuiteName;
  private String apikitMainFlowName;

  protected MunitScaffolderContext(RuntimeEdition runtimeEdition, boolean shouldCreateMunitResources, String munitSuiteName) {
    super(runtimeEdition);
    this.shouldCreateMunitResources = shouldCreateMunitResources;
    this.munitSuiteName = munitSuiteName;
  }

  public boolean shouldCreateMunitResources() {
    return shouldCreateMunitResources;
  }

  public String getMunitSuiteName() {
    return munitSuiteName;
  }

  public void setApikitMainFlowName(String name) {
    apikitMainFlowName = name;
  }

  public String getApikitMainFlowName() {
    return apikitMainFlowName;
  }

}
