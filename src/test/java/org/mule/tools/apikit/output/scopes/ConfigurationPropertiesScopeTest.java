/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;
import org.mule.tools.apikit.Helper;
import org.mule.tools.apikit.model.ConfigurationPropertiesConfig;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class ConfigurationPropertiesScopeTest {

  @Test
  public void test() throws IOException, SAXException {
    ConfigurationPropertiesConfig config = new ConfigurationPropertiesConfig("file.txt");
    ConfigurationPropertiesScope configurationPropertiesScope = new ConfigurationPropertiesScope(config);

    Document doc = new Document();
    Element mule = new Element("mule");
    mule.addContent(configurationPropertiesScope.generate());
    doc.setContent(mule);
    String output = Helper.nonSpaceOutput(doc);

    Diff diff = XMLUnit.compareXML(
                                   "<configuration-properties xmlns=\"http://www.mulesoft.org/schema/mule/core\" file=\"file.txt\" />",
                                   output);
    assertTrue(diff.toString(), diff.similar());
  }
}
