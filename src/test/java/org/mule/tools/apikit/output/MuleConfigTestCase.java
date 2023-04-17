/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;
import org.mule.tools.apikit.model.MuleConfig;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mule.tools.apikit.model.MuleConfigBuilder.fromDoc;

public class MuleConfigTestCase {

  @Test
  public void muleConfigFormatContent() throws Exception {
    SAXBuilder builder = new SAXBuilder();
    InputStream input = new FileInputStream("src/test/resources/test-mule-config/config-without-flows-and-indentation.xml");
    Document inputAsDocument = builder.build(input);
    input.close();
    MuleConfig muleConfig = fromDoc(inputAsDocument);
    String muleConfigPrettyFormat = IOUtils.toString(muleConfig.getContent());
    assertEquals(muleConfigPrettyFormat,
                 IOUtils.toString(new FileInputStream("src/test/resources/test-mule-config/config-without-flows.xml")));
  }

}
