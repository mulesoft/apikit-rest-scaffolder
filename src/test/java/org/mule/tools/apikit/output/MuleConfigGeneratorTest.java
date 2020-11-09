/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.apikit.Helper;
import org.mule.tools.apikit.model.APIAutodiscoveryConfig;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.HttpListenerConfig;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleConfigBuilder;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderContextBuilder;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.output.scopes.APIKitFlowScope;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.apikit.TestUtils.getDocumentFromStream;
import static org.mule.tools.apikit.TestUtils.getResourceAsStream;

public class MuleConfigGeneratorTest {

  public static final boolean HIDE_CONSOLE = false;
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void testGenerateFlowWithJsonExample() throws Exception {
    GenerationModel flowEntry = mockGenerationModel("{\"name\": \"John\", \"kind\": \"dog\"}");
    String s = scaffoldFlow(flowEntry);
    final String expected = "<![CDATA[%dw 2.0 output application/json --- { name: \"John\", kind: \"dog\" }]]>";
    assertFlowScope(s, expected);
  }

  @Test
  public void testGenerateFlowWithXmlExample() throws Exception {
    GenerationModel flowEntry = mockGenerationModel("<Pet> <name>John</name> <lastname>Doe</lastname> </Pet>");
    String s = scaffoldFlow(flowEntry);
    final String expected = "<![CDATA[%dw 2.0 output application/xml --- { Pet: { name: \"John\", lastname: \"Doe\" } }]]>";
    assertFlowScope(s, expected);
  }

  @Test
  public void testGenerateFlowWithRamlExample() throws Exception {
    GenerationModel flowEntry = mockGenerationModel("name: John\nkind: dog");
    String s = scaffoldFlow(flowEntry);
    final String expected = "<![CDATA[%dw 2.0 output application/json --- { name: \"John\", kind: \"dog\" }]]>";
    assertFlowScope(s, expected);
  }

  @Test
  public void testGenerateFlowWithTextPlainExample() throws Exception {
    final String example = "# something clever";
    GenerationModel flowEntry = mockGenerationModel(example);
    final String expected = "<![CDATA[%dw 2.0 output application/json --- \"# something clever\"]]>";
    assertFlowScope(scaffoldFlow(flowEntry), expected);
  }

  private void assertFlowScope(String s, String expected) throws SAXException, IOException {
    final String expectedFlowScope =
        "<flow xmlns=\"http://www.mulesoft.org/schema/mule/core\" name=\"get:\\pet\"><ee:transform xmlns:ee=\"http://www.mulesoft.org/schema/mule/ee/core\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd\"><ee:message><ee:set-payload>"
            + expected + "</ee:set-payload></ee:message></ee:transform></flow>";
    Diff diff = XMLUnit.compareXML(expectedFlowScope, s);
    assertTrue(diff.toString(), diff.similar());
  }

  private String scaffoldFlow(GenerationModel flowEntry) {
    Document doc = new Document();
    Element mule = new Element("mule");
    doc.setContent(mule);
    mule.addContent(new APIKitFlowScope(flowEntry).generate());

    return Helper.nonSpaceOutput(doc);
  }

  private GenerationModel mockGenerationModel(String example) {
    GenerationModel flowEntry = mock(GenerationModel.class);
    when(flowEntry.getFlowName()).thenReturn("get:\\pet");
    when(flowEntry.getExampleWrapper()).thenReturn(example);
    return flowEntry;
  }


  @Test
  public void blankDocumentWithExternalizedGlobals() throws JDOMException, IOException {
    String externalConfigurationFile = "globals.xml";
    ScaffoldingConfiguration.Builder builder =
        ScaffoldingConfiguration.builder().withShowConsole(HIDE_CONSOLE).withExternalCommonFile(externalConfigurationFile);
    List<MuleConfig> muleConfigs = scaffoldBlankDocument(builder.build());
    for (MuleConfig muleConfig : muleConfigs) {
      if (externalConfigurationFile.equals(muleConfig.getName())) {
        assertTrue(isEmpty(muleConfig.getApiAutodiscoveryConfig()));
        assertEquals(muleConfig.getHttpListenerConfigs().size(), 1);
        assertEquals(muleConfig.getApikitConfigs().size(), 1);
      } else {
        assertEquals(muleConfig.getMainFlows().size(), 1);
      }
    }
  }

  @Test
  public void blankDocumentWithoutExternalizedGlobals() throws JDOMException, IOException {
    ScaffoldingConfiguration.Builder builder =
        ScaffoldingConfiguration.builder().withShowConsole(HIDE_CONSOLE);
    List<MuleConfig> muleConfigs = scaffoldBlankDocument(builder.build());
    for (MuleConfig muleConfig : muleConfigs) {
      Document document = muleConfig.getContentAsDocument();
      Element rootElement = document.getRootElement();
      assertEquals("mule", rootElement.getName());
      assertConfigurations(rootElement);
    }
  }

  @Test
  public void blankDocumentWithAPIAutodiscovery() throws JDOMException, IOException {
    String apiAutodiscovery = "1234";
    ScaffoldingConfiguration.Builder builder =
        ScaffoldingConfiguration.builder().withShowConsole(HIDE_CONSOLE).withApiId(apiAutodiscovery);
    List<APIAutodiscoveryConfig> expectedApiAutodiscoveryConfig =
        Arrays.asList(new APIAutodiscoveryConfig("1234", true, "hello-main"));
    List<MuleConfig> muleConfigs = scaffoldBlankDocument(builder.build());
    assertEquals(muleConfigs.size(), 1);
    for (MuleConfig muleConfig : muleConfigs) {
      assertEquals(expectedApiAutodiscoveryConfig, muleConfig.getApiAutodiscoveryConfig());
    }
  }

  @Test
  public void blankDocumentWithExternalizedGlobalsAndAPIAutodiscovery() throws JDOMException, IOException {
    String externalConfigurationFile = "globals.xml";
    String apiAutodiscovery = "1234";
    ScaffoldingConfiguration.Builder builder =
        ScaffoldingConfiguration.builder().withShowConsole(HIDE_CONSOLE).withExternalCommonFile(externalConfigurationFile)
            .withApiId(apiAutodiscovery);
    List<APIAutodiscoveryConfig> expectedApiAutodiscoveryConfig =
        Arrays.asList(new APIAutodiscoveryConfig(apiAutodiscovery, true, "hello-main"));
    List<MuleConfig> muleConfigs = scaffoldBlankDocument(builder.build());
    assertEquals(muleConfigs.size(), 2);
    for (MuleConfig muleConfig : muleConfigs) {
      if (externalConfigurationFile.equals(muleConfig.getName())) {
        assertAPIAutodiscovery(expectedApiAutodiscoveryConfig, muleConfig);
        assertEquals(muleConfig.getHttpListenerConfigs().size(), 1);
        assertEquals(muleConfig.getApikitConfigs().size(), 1);
      } else {
        assertEquals(muleConfig.getMainFlows().size(), 1);
      }
    }
  }

  private void assertAPIAutodiscovery(List<APIAutodiscoveryConfig> expectedApiAutodiscoveryConfig, MuleConfig muleConfig) {
    for (APIAutodiscoveryConfig apiAutodiscoveryConfig : expectedApiAutodiscoveryConfig) {
      Optional<APIAutodiscoveryConfig> apiAutodiscoveryConfigOptional = muleConfig.getApiAutodiscoveryConfig().stream()
          .filter(generatedAPIAutodiscovery -> generatedAPIAutodiscovery.getApiId().equals(apiAutodiscoveryConfig.getApiId()))
          .findAny();
      boolean apiAutodiscoveryConfigPresent = apiAutodiscoveryConfigOptional.isPresent();
      assertTrue(apiAutodiscoveryConfigPresent);
      if (apiAutodiscoveryConfigPresent) {
        assertEquals(apiAutodiscoveryConfig.getFlowRef(), apiAutodiscoveryConfigOptional.get().getFlowRef());
        assertEquals(apiAutodiscoveryConfig.getIgnoreBasePath(), apiAutodiscoveryConfigOptional.get().getIgnoreBasePath());
      }
    }
  }

  @Test
  public void blankDocumentWithoutLCInDomain() throws JDOMException, IOException {
    ScaffoldingConfiguration.Builder builder = ScaffoldingConfiguration.builder();
    List<MuleConfig> muleConfigs = scaffoldBlankDocument(builder.build());
    for (MuleConfig muleConfig : muleConfigs) {
      Document document = muleConfig.getContentAsDocument();
      Element rootElement = document.getRootElement();
      assertCommonFlows(rootElement);
      Element consoleFlow = rootElement.getChildren().get(3);
      assertEquals("flow", consoleFlow.getName());
      assertEquals("hello-console", consoleFlow.getAttribute("name").getValue());
      assertEquals("httpListenerConfig", consoleFlow.getChildren().get(0).getAttribute("config-ref").getValue());
      assertEquals("/console/*", consoleFlow.getChildren().get(0).getAttribute("path").getValue());
      assertEquals("console", consoleFlow.getChildren().get(1).getName());
    }
  }

  @Test
  public void blankDocumentWithoutLCInDomainHideConsole() throws JDOMException, IOException {
    ScaffoldingConfiguration.Builder builder = ScaffoldingConfiguration.builder();
    List<MuleConfig> muleConfigs = scaffoldBlankDocument(builder.build());
    for (MuleConfig muleConfig : muleConfigs) {
      Document document = muleConfig.getContentAsDocument();
      Element rootElement = document.getRootElement();
      assertCommonFlows(rootElement);
    }
  }

  private void assertCommonFlows(Element rootElement) {
    assertEquals("mule", rootElement.getName());
    assertConfigurations(rootElement);

    assertMainFlow(rootElement);
  }

  private void assertMainFlow(Element rootElement) {
    Element mainFlow = rootElement.getChildren().get(2);

    assertEquals("flow", mainFlow.getName());
    assertEquals("hello-main", mainFlow.getAttribute("name").getValue());
    assertEquals("httpListenerConfig", mainFlow.getChildren().get(0).getAttribute("config-ref").getValue());
    assertEquals("/api/*", mainFlow.getChildren().get(0).getAttribute("path").getValue());
  }

  private void assertConfigurations(Element rootElement) {
    Element xmlListenerConfig = rootElement.getChildren().get(0);
    assertEquals("listener-config", xmlListenerConfig.getName());

    Element apikitConfig = rootElement.getChildren().get(1);
    assertEquals("hello-config", apikitConfig.getAttribute("name").getValue());
  }

  private List<MuleConfig> scaffoldBlankDocument(ScaffoldingConfiguration configuration) throws JDOMException, IOException {
    HttpListenerConfig listenerConfig =
        new HttpListenerConfig(HttpListenerConfig.DEFAULT_CONFIG_NAME, "localhost", "8080", "HTTP", "");
    InputStream apikitConfig = getResourceAsStream("scaffolder-only-apikit-config/api.xml");
    MuleConfig muleConfig = buildAPIKitMuleConfig(apikitConfig);
    ApikitMainFlowContainer api = mock(ApikitMainFlowContainer.class);
    when(api.getPath()).thenReturn("/api/*");
    when(api.getHttpListenerConfig()).thenReturn(listenerConfig);
    when(mock(MuleConfig.class).getApikitConfigs()).thenReturn(muleConfig.getApikitConfigs());

    File raml = mock(File.class);
    when(raml.getName()).thenReturn("hello.raml");
    when(api.getApiFilePath()).thenReturn("hello.raml");
    when(api.getId()).thenReturn("hello");
    doCallRealMethod().when(api).setId(anyString());
    doCallRealMethod().when(api).setApiFilePath(anyString());
    doCallRealMethod().when(api).setConfig(any(APIKitConfig.class));
    doCallRealMethod().when(api).getConfig();

    api.setId("hello");
    api.setApiFilePath("hello.raml");
    List<ApikitMainFlowContainer> apis = new ArrayList<>();
    apis.add(api);

    MuleConfigGenerator muleConfigGenerator =
        new MuleConfigGenerator(apis, new ArrayList<>(), new ArrayList<>(),
                                ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.CE).build(), configuration);

    return muleConfigGenerator.generate();
  }

  private MuleConfig buildAPIKitMuleConfig(InputStream inputStream) throws JDOMException, IOException {
    Document apikitConfigDoc = null;
    apikitConfigDoc = getDocumentFromStream(inputStream);

    return MuleConfigBuilder.fromDoc(apikitConfigDoc);
  }

}
