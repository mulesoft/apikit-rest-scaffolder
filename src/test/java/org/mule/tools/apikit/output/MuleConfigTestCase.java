/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;
import org.mule.tools.apikit.model.MuleConfig;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.transform.Source;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.TestUtils.getSaxBuilder;
import static org.mule.tools.apikit.model.MuleConfigBuilder.fromDoc;

public class MuleConfigTestCase {

  @Test
  public void muleConfigFormatContent() throws Exception {
    InputStream input = new FileInputStream("src/test/resources/test-mule-config/config-without-flows-and-indentation.xml");

    SAXBuilder builder = getSaxBuilder();
    Document inputAsDocument = builder.build(input);
    input.close();
    MuleConfig muleConfig = fromDoc(inputAsDocument);
    Source actualSource = Input.fromStream(muleConfig.getContent()).build();

    input = new FileInputStream("src/test/resources/test-mule-config/config-without-flows.xml");
    Source expectedSource = Input.fromStream(input).build();

    Diff diff = DiffBuilder
        .compare(expectedSource)
        .withTest(actualSource)
        .checkForIdentical()
        .build();

    assertTrue(diff.fullDescription(), !diff.hasDifferences());
  }

}
