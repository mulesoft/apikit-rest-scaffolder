package org.mule.tools.apikit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.apikit.model.MuleConfig;
import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MuleConfigTest {

  SAXBuilder builder;

  @Before
  public void setUp() {
    builder = new SAXBuilder();
  }

  @Test
  public void createsMuleConfig() throws Exception {
    String path = "src/test/resources/test-mule-config/api.xml";
    File file = new File(path);

    InputStream input = FileUtils.openInputStream(file);
    MuleConfig muleConfig = MuleConfig.fromStream(input);
    assertEquals(muleConfig.getFlows().size(), 8);
    assertEquals(muleConfig.getConfigs().size(), 1);
    assertEquals(muleConfig.getTests().size(), 0);
    input.close();
  }

  @Test
  public void createsMuleConfigWithFlowsAndConfigs() throws Exception {
    String path = "src/test/resources/test-mule-config/leagues-flow-config.xml";
    File file = new File(path);

    InputStream input = FileUtils.openInputStream(file);
    MuleConfig muleConfig = MuleConfig.fromStream(input);
    assertEquals(muleConfig.getFlows().size(), 7);
    assertEquals(muleConfig.getConfigs().size(), 1);
    assertEquals(muleConfig.getTests().size(), 0);
    input.close();
  }

  @Test
  public void createsMuleConfigWithFlowsWithoutConfig() throws Exception {
    String path = "src/test/resources/test-mule-config/mule-config-without-apikit-config.xml";
    File file = new File(path);

    InputStream input = FileUtils.openInputStream(file);
    MuleConfig muleConfig = MuleConfig.fromStream(input);
    assertEquals(muleConfig.getFlows().size(), 4);
    assertEquals(muleConfig.getConfigs().size(), 0);
    assertEquals(muleConfig.getTests().size(), 0);
    input.close();
  }

  @Test
  public void createsMuleConfigWithConfigWithoutFlow() throws Exception {
    String path = "src/test/resources/test-mule-config/config-without-flows.xml";
    File file = new File(path);

    InputStream input = FileUtils.openInputStream(file);
    MuleConfig muleConfig = MuleConfig.fromStream(input);
    assertEquals(muleConfig.getFlows().size(), 0);
    assertEquals(muleConfig.getConfigs().size(), 1);
    assertEquals(muleConfig.getTests().size(), 0);
    input.close();
  }

  @Test
  public void deserializationReturnsSameContent() throws Exception {
    String path = "src/test/resources/test-mule-config/api.xml";
    File file = new File(path);

    InputStream fileAsInputStream = FileUtils.openInputStream(file);
    MuleConfig muleConfig = MuleConfig.fromStream(fileAsInputStream);

    String originalFileAsString = FileUtils.readFileToString(file);
    String muleConfigContentAsString = IOUtils.toString(muleConfig.getContent());
    Diff diff = XMLUnit.compareXML(originalFileAsString, muleConfigContentAsString);
    assertTrue(diff.identical());
  }

}
