/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.amf.impl.DocumentParser;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.ScaffolderContext;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mule.amf.impl.DocumentParser.VendorEx.OAS20_JSON;
import static org.mule.amf.impl.DocumentParser.VendorEx.OAS20_YAML;
import static org.mule.tools.apikit.model.RuntimeEdition.EE;

@RunWith(Parameterized.class)
public class ScaffolderOASTest {

  private Path api;

  private static final PathMatcher API_MATCHER = FileSystems.getDefault().getPathMatcher("glob:*.{json,yaml, yml}");

  public ScaffolderOASTest(final String folderName, final Path api) {
    this.api = api;
  }

  @Before
  public void beforeTest() throws IOException {
    final File outputFolder = outputFolder(api).toFile();

    if (outputFolder.exists())
      FileUtils.deleteDirectory(outputFolder);
  }

  @Test
  public void scaffolder() throws Exception {
    MuleConfig generatedMuleConfig = scaffoldApi(api);
    final String current = IOUtils.toString(generatedMuleConfig.getContent());
    if (current.trim().isEmpty()) {
      Assert.fail(format("Scaffolder generation fail parsing ApikitMainFlowContainer '%s'", api.getFileName()));
    }

    final Path goldenPath = goldenFile();

    // When Golden file is missing we create it but test fail
    if (!goldenPath.toFile().exists()) {
      createGoldenFile(goldenPath, current);
      Assert.fail(format("Golden file missing. Created for ApikitMainFlowContainer '%s'", api.getFileName()));
    }

    // When Golden file existe we comparate both Scaffolder versions
    final String expected = readFile(goldenPath);

    XMLUnit.setIgnoreWhitespace(true);
    Diff diff = XMLUnit.compareXML(current, expected);
    assertTrue(format("Scaffolder differs for ApikitMainFlowContainer '%s'", api.getFileName()), diff.identical());
  }

  private MuleConfig scaffoldApi(Path api) {
    ScaffolderContext context = new ScaffolderContext.Builder().withRuntimeEdition(EE).build();
    MuleScaffolder muleScaffolder = new MuleScaffolder(context);

    ApiReference apiReference = ApiReference.create(api.toUri());
    ParseResult parseResult = new ParserService().parse(apiReference);
    assertTrue(parseResult.success());
    ScaffoldingConfiguration configuration = new ScaffoldingConfiguration.Builder().withApi(parseResult.get()).build();
    ScaffoldingResult scaffolderResult = muleScaffolder.run(configuration);
    assertEquals(1, scaffolderResult.getGeneratedConfigs().size());
    return scaffolderResult.getGeneratedConfigs().get(0);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> getData() throws IOException, URISyntaxException {

    final List<Object[]> parameters = new ArrayList<>();
    final Path basePath = Paths.get(ScaffolderOASTest.class.getResource("/oas").toURI());

    scan(basePath).forEach(path -> {
      try {
        final Path folderName = basePath.relativize(path);
        parameters.add(new Object[] {folderName.toString(), path});
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    return parameters;
  }

  private Path goldenFile() {
    return muleApp(api.getParent(), fileNameWithOutExtension(api));
  }

  private static Path muleApp(final Path folder, final String apiName) {
    return Paths.get(folder.toString(), apiName + ".xml");
  }

  private static List<Path> scan(final Path root) throws IOException {
    return Files.walk(root)
        .peek(path -> System.out.println("Path:" + path + " isApi:" + isOas(path)))
        .filter(ScaffolderOASTest::isOas)
        .collect(toList());
  }

  private static boolean isOas(final Path path) {
    final Path fileName = path.getFileName();
    final boolean isOas =
        Files.isRegularFile(path) && API_MATCHER.matches(fileName) && !"mule-artifact.json".equals(fileName.toString());

    if (!isOas)
      return false;

    final DocumentParser.VendorEx vendor = DocumentParser.getVendor(path.toUri());
    return OAS20_JSON.equals(vendor) || OAS20_YAML.equals(vendor);
  }

  private static String fileNameWithOutExtension(final Path path) {
    return FilenameUtils.removeExtension(path.getFileName().toString());
  }

  private static Path outputFolder(final Path api) throws IOException {

    final String apiName = fileNameWithOutExtension(api);
    final Path apiFolder = api.getParent();
    return Paths.get(apiFolder.toString(), apiName);
  }

  private static Path createGoldenFile(final Path goldenFile, final String content) throws IOException {

    final String srcPath = goldenFile.toFile().getPath().replace("target/test-classes", "src/test/resources");
    final Path goldenPath = Paths.get(srcPath);
    System.out.println("*** Create Golden " + goldenPath);

    // Write golden files  with current values
    final Path parent = goldenPath.getParent();
    if (!Files.exists(parent))
      Files.createDirectory(parent);
    return Files.write(goldenPath, content.getBytes("UTF-8"));
  }

  private static String readFile(final Path path) {
    try {
      return new String(Files.readAllBytes(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
