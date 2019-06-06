/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

public class ScaffolderContext {

  private RuntimeEdition runtimeEdition;

  private ScaffolderContext(RuntimeEdition runtimeEdition) {
    this.runtimeEdition = runtimeEdition;
  }

  public RuntimeEdition getRuntimeEdition() {
    return runtimeEdition;
  }

  public static class Builder {

    private RuntimeEdition runtimeEdition;

    public Builder withRuntimeEdition(RuntimeEdition runtimeEdition) {
      this.runtimeEdition = runtimeEdition;
      return this;
    }

    public ScaffolderContext build() {
      return new ScaffolderContext(runtimeEdition);
    }
  }
}
