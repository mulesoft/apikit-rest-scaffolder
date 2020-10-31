/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleDomain;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderContextBuilder;
import org.mule.tools.apikit.model.ScaffoldingAccessories;
import org.mule.tools.apikit.model.ScaffoldingConfiguration;
import org.mule.tools.apikit.model.ScaffoldingResult;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.AbstractScaffolderTestCase.createMuleConfigsFromLocations;
import static org.mule.tools.apikit.AbstractScaffolderTestCase.createMuleDomainFromLocation;

public class ScaffolderExecutionBuilder {

  private ScaffolderGenerator scaffolderGenerator = new ScaffolderGenerator();
  private ScaffoldingResult scaffoldingResult;
  private String configContent;

  public static ScaffolderExecutionBuilder when() {
    return new ScaffolderExecutionBuilder();
  }

  public ScaffolderExecutionBuilder api(String apiLocation) {
    ApiReference apiReference = ApiReference.create(Paths.get(apiLocation).toString());
    return api(apiReference);
  }

  public ScaffolderExecutionBuilder api(ApiReference apiReference) {
    ParseResult parseResult = new ParserService().parse(apiReference);
    scaffolderGenerator.setApi(parseResult.get());

    return this;
  }

  public ScaffolderExecutionBuilder showConsole(boolean showConsole) {
    scaffolderGenerator.setShowConsole(showConsole);
    return this;
  }

  public ScaffolderExecutionBuilder runtimeEdition(RuntimeEdition runtimeEdition) {
    scaffolderGenerator.setRuntimeEdition(runtimeEdition);
    return this;
  }

  public ScaffolderExecutionBuilder domain(String muleDomainLocation) throws Exception {
    MuleDomain muleDomain = createMuleDomainFromLocation(muleDomainLocation);
    return domain(muleDomain);
  }

  public ScaffolderExecutionBuilder domain(MuleDomain muleDomain) {
    if (muleDomain != null) {
      scaffolderGenerator.setDomain(muleDomain);
    }
    return this;
  }

  public ScaffolderExecutionBuilder configs(String... muleConfigsLocations) throws Exception {
    List<MuleConfig> existingMuleConfigs = createMuleConfigsFromLocations(Arrays.asList(muleConfigsLocations));
    return configs(existingMuleConfigs);
  }

  public ScaffolderExecutionBuilder configs(List<MuleConfig> existingMuleConfig) {
    scaffolderGenerator.setExistingMuleConfigs(existingMuleConfig);
    return this;
  }

  public ScaffolderExecutionBuilder then() {
    if (scaffoldingResult == null) {
      this.scaffoldingResult = scaffolderGenerator.generate();
    }

    return this;
  }

  public ScaffolderExecutionBuilder assertSuccess() {
    assertTrue(scaffoldingResult.isSuccess());
    return this;
  }

  public ScaffolderExecutionBuilder assertXmlOccurrences(XmlOccurrencesAsserterBuilder.XmlOccurrencesAsserter xmlOccurrencesAsserter)
      throws IOException {
    xmlOccurrencesAsserter.assertOccurrences(getConfigContent());
    return this;
  }

  public ScaffolderExecutionBuilder assertConfigsSize(int configsSize) {
    assertEquals(configsSize, scaffoldingResult.getGeneratedConfigs().size());
    return this;
  }


  ScaffoldingResult getScaffoldingResult() {
    then();
    return scaffoldingResult;
  }

  public String getConfigContent() throws IOException {
    if (configContent == null) {
      configContent = APIKitTools.readContents(scaffoldingResult.getGeneratedConfigs().get(0).getContent());
    }

    return configContent;
  }

  public List<MuleConfig> getGeneratedConfigs() {
    return scaffoldingResult.getGeneratedConfigs();
  }

  private class ScaffolderGenerator {

    ScaffoldingConfiguration.Builder scaffoldingConfigurationBuilder = new ScaffoldingConfiguration.Builder();
    ScaffolderContextBuilder scaffolderContextBuilder = ScaffolderContextBuilder.builder().withRuntimeEdition(RuntimeEdition.EE);

    public void setExistingMuleConfigs(List<MuleConfig> existingMuleConfigs) {
      scaffoldingConfigurationBuilder.withMuleConfigurations(existingMuleConfigs);
    }

    public void setApi(ApiSpecification apiSpecification) {
      scaffoldingConfigurationBuilder.withApi(apiSpecification);
    }

    public void setShowConsole(boolean showConsole) {
      ScaffoldingAccessories scaffoldingAccessories = new ScaffoldingAccessories(showConsole, null, null, null);
      scaffoldingConfigurationBuilder.withShowConsole(showConsole);
    }

    public void setRuntimeEdition(RuntimeEdition runtimeEdition) {
      scaffolderContextBuilder.withRuntimeEdition(runtimeEdition);
    }

    public void setDomain(MuleDomain muleDomain) {
      scaffoldingConfigurationBuilder.withDomain(muleDomain);
    }

    public ScaffoldingResult generate() {
      MainAppScaffolder mainAppScaffolder = new MainAppScaffolder(scaffolderContextBuilder.build());
      ScaffoldingResult scaffoldingResult = mainAppScaffolder.run(scaffoldingConfigurationBuilder.build());
      return scaffoldingResult;
    }
  }

}
