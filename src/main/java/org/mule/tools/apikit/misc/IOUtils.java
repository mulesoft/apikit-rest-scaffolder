/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.misc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOUtils {

  public static InputStream cloneInputStream(InputStream toClone) throws IOException {
    ByteArrayOutputStream middleMan = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int len;
    while ((len = toClone.read(buffer)) > -1) {
      middleMan.write(buffer, 0, len);
    }
    return new ByteArrayInputStream(middleMan.toByteArray());
  }

  public static String readAsString(InputStream inputStream) throws IOException {
    StringBuilder sb = new StringBuilder();
    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }
    return sb.toString();
  }

}
