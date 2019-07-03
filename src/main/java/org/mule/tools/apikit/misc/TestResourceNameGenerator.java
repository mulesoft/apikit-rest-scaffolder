/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.misc;

import org.apache.commons.lang.StringUtils;
import org.mule.tools.apikit.output.GenerationModel;

public class TestResourceNameGenerator {

  private static final String FILE_REGEX = "[^a-zA-Z0-9\\}\\{.-]";
  private static final String NAME_SPLIT_CHAR = "_";

  public static String generate(GenerationModel flowEntry, String mimeType, String statusCode) {
    String statusStr = StringUtils.isBlank(statusCode) ? "" : NAME_SPLIT_CHAR + statusCode;
    String name = flowEntry.getVerb() + statusStr + flowEntry.getResource().getUri() + NAME_SPLIT_CHAR + mimeType;

    name = name.replaceAll(FILE_REGEX, NAME_SPLIT_CHAR) + getExtension(mimeType);
    return StringUtils.lowerCase(name);
  }

  public static String getExtension(String mimeType) {
    switch (mimeType) {
      case "application/json":
        return ".json";
      case "text/xml":
        return ".xml";
      default:
        return ".txt";
    }
  }
}
