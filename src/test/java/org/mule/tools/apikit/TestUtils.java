package org.mule.tools.apikit;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.xml.sax.SAXException;

public class TestUtils {

  private static final SAXBuilder BUILDER = new SAXBuilder(XMLReaders.NONVALIDATING);

  public static Document getDocumentFromStream(InputStream xmlWithFlows) throws JDOMException, IOException {
    Document document = BUILDER.build(xmlWithFlows);
    xmlWithFlows.close();
    return document;
  }

  public static String getResourceAsString(String res) {
    try {
      return IOUtils.toString(getResourceAsStream(res));
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
}
