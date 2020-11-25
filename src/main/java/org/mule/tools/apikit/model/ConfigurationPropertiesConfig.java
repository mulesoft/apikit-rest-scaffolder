/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mule.tools.apikit.output.NamespaceWithLocation;
import org.mule.tools.apikit.output.scopes.ConfigurationPropertiesScope;
import org.mule.tools.apikit.output.scopes.Scope;

public class ConfigurationPropertiesConfig implements Scope {

  public static final String ELEMENT_NAME = "configuration-properties";
  public static final String FILE_ATTRIBUTE = "file";

  private String file;

  public ConfigurationPropertiesConfig(String file) {
    this.file = file;
  }

  public String getFile() {
    return file;
  }

  @Override
  public Element generate() {
    ConfigurationPropertiesScope configurationPropertiesScope = new ConfigurationPropertiesScope(this);
    return configurationPropertiesScope.generate();
  }
}
