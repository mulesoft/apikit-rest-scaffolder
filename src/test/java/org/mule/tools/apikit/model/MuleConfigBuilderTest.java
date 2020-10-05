/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.jdom2.Document;
import org.junit.Test;
import org.mule.tools.apikit.output.scopes.MuleScope;

public class MuleConfigBuilderTest {

  @Test
  public void testFromDoc() {
    Document document = new Document();
    document.setRootElement(new MuleScope(false, false, false).generate());
    MuleConfigBuilder.fromDoc(document);
  }

}
