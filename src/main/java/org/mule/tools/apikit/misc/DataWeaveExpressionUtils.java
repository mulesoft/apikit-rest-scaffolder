/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.misc;

/**
 * Provides functionality to handle expressions
 *
 * @author Mulesoft Inc.
 */
public class DataWeaveExpressionUtils {

  private static final String EXP_WRAPPER = "#[%s]";
  private static final String STRING_EXP_WRAPPER = "#['%s']";
  private static final String EQUAL_TO_WRAPPER = "#[MunitTools::equalTo(%s)]";
  private static final String READ_EQUAL_TO_PAYLOAD_WRAPPER = "#[output application/java ---write(%s, '%s') as String]";

  public static String wrapInExpression(Object value) {
    return String.format(EXP_WRAPPER, value);
  }

  public static String wrapInStringExpression(String value) {
    return String.format(STRING_EXP_WRAPPER, value);
  }

  public static String wrapInEqualTo(Object value) {
    return String.format(EQUAL_TO_WRAPPER, value);
  }

  public static String wrapInWriteToString(Object value, String mimeType) {
    return String.format(READ_EQUAL_TO_PAYLOAD_WRAPPER, value, mimeType);
  }

}
