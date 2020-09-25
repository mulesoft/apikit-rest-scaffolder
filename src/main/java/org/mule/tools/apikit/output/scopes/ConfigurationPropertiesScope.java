/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import org.jdom2.Element;
import org.mule.tools.apikit.model.APIAutodiscoveryConfig;
import org.mule.tools.apikit.model.ConfigurationPropertiesConfig;

public class ConfigurationPropertiesScope implements Scope {

  private ConfigurationPropertiesConfig configurationPropertiesConfig;

  public ConfigurationPropertiesScope(ConfigurationPropertiesConfig config) {
    this.configurationPropertiesConfig = config;
  }

  @Override
  public Element generate() {
    Element config = null;
    if (this.configurationPropertiesConfig != null) {
      config = new Element(ConfigurationPropertiesConfig.ELEMENT_NAME);
      if (this.configurationPropertiesConfig.getFile() != null)
        config.setAttribute(ConfigurationPropertiesConfig.FILE_ATTRIBUTE, this.configurationPropertiesConfig.getFile());
    }
    return config;
  }
}
