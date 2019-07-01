/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.io.InputStream;
import java.util.Objects;

public class ScaffolderResource implements NamedContent {

  private String directory;
  private String name;
  private InputStream content;

  public ScaffolderResource(String directory, String name, InputStream content) {
    this.directory = directory;
    this.name = name;
    this.content = content;
  }

  public String getDirectory() {
    return directory;
  }

  public String getName() {
    return name;
  }

  public InputStream getContent() {
    return content;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScaffolderResource resource = (ScaffolderResource) o;
    return Objects.equals(directory, resource.directory) &&
        Objects.equals(name, resource.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(directory, name);
  }
}
