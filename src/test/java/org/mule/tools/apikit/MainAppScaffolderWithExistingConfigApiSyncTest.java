/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.apikit.implv2.ParserV2Utils;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;
import org.mule.tools.apikit.model.RuntimeEdition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MainAppScaffolderWithExistingConfigApiSyncTest extends AbstractScaffolderTestCase {

  private final static String ROOT_RAML_RESOURCE_URL = "resource::com.mycompany:raml-api:1.0.0:raml:zip:";
  private final static String ROOT_RAML_RESOURCE_URL_V2 = "resource::com.mycompany:raml-api:2.0.0:raml:zip:";

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Before
  public void setUp() throws IOException {
    folder.newFolder("rescaffolding-apisync-version");
    folder.newFolder("rescaffolding-apisync-version", "v1");
    folder.newFolder("rescaffolding-apisync-version", "v2");
  }

  @Test
  public void reScaffold() throws Exception {
    String raml = "api.raml";
    String ramlFolder = "src/test/resources/rescaffolding-apisync-version/v1";

    ScaffolderContext context = new ScaffolderContext.Builder().withRuntimeEdition(RuntimeEdition.CE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    ResourceLoader testScaffolderResourceLoader = new TestScaffolderResourceLoader(ramlFolder);
    ParseResult parseResult =
        new ParserService().parse(ApiReference.create(ROOT_RAML_RESOURCE_URL + raml, testScaffolderResourceLoader));
    assertTrue(parseResult.success());

    ScaffoldingConfiguration configuration = new ScaffoldingConfiguration.Builder().withApi(parseResult.get()).build();
    ScaffoldingResult result = mainAppScaffolder.run(configuration);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());

    String expected = IOUtils.toString(MainAppScaffolderWithExistingConfigApiSyncTest.class.getClassLoader()
        .getResourceAsStream("rescaffolding-apisync-version/v1/api.xml"));
    String generated = IOUtils.toString(result.getGeneratedConfigs().get(0).getContent());

    XMLUnit.setIgnoreWhitespace(true);
    Diff diff = XMLUnit.compareXML(expected, generated);
    assertTrue(diff.identical());

    List<MuleConfig> muleConfigs = new ArrayList<>(result.getGeneratedConfigs());

    // In the second ScaffoldingConfiguration, we have to include the mule config generated previously
    ScaffoldingConfiguration secondScaffoldingConfiguration =
        new ScaffoldingConfiguration.Builder().withApi(parseResult.get()).withMuleConfigurations(muleConfigs).build();
    ScaffoldingResult secondScaffoldingResult = mainAppScaffolder.run(secondScaffoldingConfiguration);

    assertTrue(secondScaffoldingResult.isSuccess());
    assertEquals(1, secondScaffoldingResult.getGeneratedConfigs().size());
    assertEquals(result.getGeneratedConfigs().get(0), secondScaffoldingResult.getGeneratedConfigs().get(0));
  }

  @Test
  public void reScaffoldDifferntVersions() throws Exception {
    String raml = "api.raml";
    String ramlFolderV1 = "src/test/resources/rescaffolding-apisync-version/v1";
    String ramlFolderV2 = "src/test/resources/rescaffolding-apisync-version/v2";
    XMLUnit.setIgnoreWhitespace(true);

    ScaffoldingResult result = scaffoldApiSync(raml, ramlFolderV1, ROOT_RAML_RESOURCE_URL, null);
    String expected = IOUtils.toString(MainAppScaffolderWithExistingConfigApiSyncTest.class.getClassLoader()
        .getResourceAsStream("rescaffolding-apisync-version/v1/api.xml"));
    String generated = IOUtils.toString(result.getGeneratedConfigs().get(0).getContent());

    Diff diff = XMLUnit.compareXML(expected, generated);
    assertTrue(diff.identical());

    List<MuleConfig> muleConfigs = new ArrayList<>(result.getGeneratedConfigs());
    ScaffoldingResult secondScaffoldingResult = scaffoldApiSync(raml, ramlFolderV2, ROOT_RAML_RESOURCE_URL_V2, muleConfigs);

    String secondScaffoldingExpected = IOUtils.toString(MainAppScaffolderWithExistingConfigApiSyncTest.class.getClassLoader()
        .getResourceAsStream("rescaffolding-apisync-version/v2/api.xml"));
    String secondScaffoldingGenerated = IOUtils.toString(secondScaffoldingResult.getGeneratedConfigs().get(0).getContent());
    Diff secondScaffoldingDiff = XMLUnit.compareXML(secondScaffoldingExpected, secondScaffoldingGenerated);
    assertTrue(secondScaffoldingDiff.identical());
  }

  private ScaffoldingResult scaffoldApiSync(String raml, String ramlFolder, String rootRamlResourceUrl,
                                            List<MuleConfig> muleConfigs) {
    ScaffolderContext context = new ScaffolderContext.Builder().withRuntimeEdition(RuntimeEdition.CE).build();
    MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(context);

    ResourceLoader testScaffolderResourceLoader = new TestScaffolderResourceLoader(ramlFolder);
    ParseResult parseResult =
        new ParserService().parse(ApiReference.create(rootRamlResourceUrl + raml, testScaffolderResourceLoader));
    assertTrue(parseResult.success());

    ScaffoldingConfiguration.Builder configurationBuilder = new ScaffoldingConfiguration.Builder().withApi(parseResult.get());
    if (muleConfigs != null) {
      configurationBuilder.withMuleConfigurations(muleConfigs);
    }

    ScaffoldingResult result = mainAppScaffolder.run(configurationBuilder.build());
    assertTrue(result.isSuccess());
    assertEquals(1, result.getGeneratedConfigs().size());
    return result;
  }

  @After
  public void after() {
    System.clearProperty(ParserV2Utils.PARSER_V2_PROPERTY);
  }
}
