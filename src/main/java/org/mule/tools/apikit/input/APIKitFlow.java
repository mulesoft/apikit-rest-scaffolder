/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.mule.apikit.common.RamlUtils;
import org.mule.tools.apikit.misc.FlowNameUtils;

import java.util.Collection;
import java.util.regex.Matcher;

public class APIKitFlow {

  public static final String UNNAMED_CONFIG_NAME = "noNameConfig";
  private final String action;
  private final String resource;
  private final String configRef;
  private final String mimeType;

  public APIKitFlow(final String action, final String resource, final String mimeType, String configRef) {
    this.action = action;
    this.resource = resource;
    this.mimeType = mimeType;
    this.configRef = configRef != null ? configRef : UNNAMED_CONFIG_NAME;
  }

  public String getAction() {
    return action;
  }

  public String getResource() {
    return resource;
  }

  public String getMimeType() {
    return mimeType;
  }

  public String getConfigRef() {
    return configRef;
  }

  public static APIKitFlow buildFromName(String name, Collection<String> existingConfigs) throws IllegalArgumentException {
    Matcher matcher = FlowNameUtils.getMatcher(name);
    String action = FlowNameUtils.getAction(matcher);

    if (!RamlUtils.isValidAction(action)) {
      throw new IllegalArgumentException(action + " is not a valid action type");
    }

    String resource = FlowNameUtils.getResource(matcher);

    String mimeType = FlowNameUtils.getMimeType(matcher, existingConfigs).orElse(null);
    String config = FlowNameUtils.getConfig(matcher, existingConfigs).orElse(null);

    return new APIKitFlow(action, resource, mimeType, config);
  }
}
