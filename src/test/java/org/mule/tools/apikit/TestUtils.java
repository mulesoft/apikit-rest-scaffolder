/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaders;
import org.mule.tools.apikit.misc.APIKitTools;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertTrue;

public class TestUtils {

  public static final String ENABLE_FLOW_SOURCES_TEMPLATE = "<munit:enable-flow-source value=\"%s\" />";
  private static final SAXBuilder BUILDER = TestUtils.getSaxBuilder(XMLReaders.NONVALIDATING);

  public static Document getDocumentFromStream(InputStream xmlWithFlows) throws JDOMException, IOException {
    Document document = BUILDER.build(xmlWithFlows);
    xmlWithFlows.close();
    return document;
  }

  public static String getResourceAsString(String res) {
    try (InputStream stream = getResourceAsStream(res)) {
      return APIKitTools.readContents(stream);
    } catch (Exception e) {
      throw new RuntimeException("cannot fetch resource " + res);
    }
  }

  public static InputStream getResourceAsStream(String res) {
    try {
      return TestUtils.class.getClassLoader().getResourceAsStream(res);
    } catch (Exception e) {
      throw new RuntimeException("cannot fetch resource " + res);
    }
  }

  public static URL getResourceAsUrl(String res) {
    try {
      return TestUtils.class.getClassLoader().getResource(res);
    } catch (Exception e) {
      throw new RuntimeException("cannot fetch resource " + res);
    }
  }

  public static void assertXmls(String actual, String expected) {
    XMLUnit.setIgnoreWhitespace(true);
    Diff diff = null;

    try {
      diff = XMLUnit.compareXML(actual, expected);
    } catch (SAXException | IOException e) {
      throw new RuntimeException(e);
    }

    if (!diff.identical()) {
      System.out.println("ACTUAL: \n");
      System.out.println(actual);
      System.out.println("\n ########### \n");
      System.out.println("EXPECTED: \n");
      System.out.println(expected);
    }

    assertTrue(diff.identical());
  }

  public static String generateMainFlowNameForApi(String name) {
    return name + "-main";
  }

  public static int countOccurrences(String string, String substring) {
    int lastIndex = 0;
    int count = 0;
    while (lastIndex >= 0) {
      lastIndex = string.indexOf(substring, lastIndex);
      if (lastIndex >= 0) {
        count++;
        lastIndex += substring.length();
      }
    }
    return count;
  }

  public static SAXBuilder getSaxBuilder() {
    return getSaxBuilder(null);
  }

  public static SAXBuilder getSaxBuilder(XMLReaderJDOMFactory readerSource) {
    SAXBuilder builder = readerSource != null ? new SAXBuilder(readerSource) : new SAXBuilder();
    builder.setExpandEntities(false);
    builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
    builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    return builder;
  }
}
