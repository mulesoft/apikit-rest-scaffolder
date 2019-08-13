/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.munit;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mule.tools.apikit.model.ScaffolderResource;
import org.mule.tools.apikit.model.ScaffoldingResult;

import java.util.List;

import static junit.framework.TestCase.*;

public class MunitScaffolderResourcesTest extends AbstractMunitScaffolderTest {

  private ScaffoldingResult simpleGenerationWithResource(String name) throws Exception {
    return simpleGeneration(name, "scaffolder", true);
  }

  @Test
  public void testCorrectAssertPayloadGeneration() throws Exception {
    ScaffoldingResult result = simpleGenerationWithResource("example");
    String expectedContent =
        "expression=\"#[output application/java ---write(payload, 'text/xml') as String]\" is=\"#[MunitTools::equalTo(MunitTools::getResourceAsString('scaffolder/response/get_200_pet_text_xml.xml'))]\"";
    String generatedConfigContent = IOUtils.toString(result.getGeneratedConfigs().get(0).getContent());
    assertTrue(generatedConfigContent.contains(expectedContent));

    ScaffolderResource generatedResource = getResourceByName(result.getGeneratedResources(), "get_200_pet_application_json.json");
    assertNotNull(generatedResource);
    assertEquals("{\"name\":\"Bobby\",\"food\":\"Ice Cream\"}", IOUtils.toString(generatedResource.getContent()));
  }

  @Test
  public void testCorrectSetPayloadGeneration() throws Exception {
    ScaffoldingResult result = simpleGenerationWithResource("simple-post-body");
    String expectedAssertion =
        "expression=\"#[output application/java ---write(payload, 'application/json') as String]\" is=\"#[MunitTools::equalTo(MunitTools::getResourceAsString('scaffolder/response/post_200_albums_application_json.json'))]\"";
    String expectedSetPayload =
        "<set-payload value=\"#[MunitTools::getResourceAsString('scaffolder/request/post_albums_application_json.json')]\" />";

    String generatedConfigContent = IOUtils.toString(result.getGeneratedConfigs().get(0).getContent());
    assertTrue(generatedConfigContent.contains(expectedAssertion));
    assertTrue(generatedConfigContent.contains(expectedSetPayload));

    ScaffolderResource requestResource = getResourceByName(result.getGeneratedResources(), "post_albums_application_json.json");
    ScaffolderResource responseResource = getResourceByName(result.getGeneratedResources(),
                                                            "post_200_albums_application_json.json");
    String expectedGeneratedRequestResourceContent = "{\"message\":\"This is an album json post\"}";
    String expectedGeneratedResponseResourceContent = "{\"message\":\"The response is json\"}";

    assertNotNull(requestResource);
    assertNotNull(responseResource);

    assertEquals(expectedGeneratedRequestResourceContent, IOUtils.toString(requestResource.getContent()));
    assertEquals(expectedGeneratedResponseResourceContent, IOUtils.toString(responseResource.getContent()));
  }

  @Test
  public void testMultiplePostBodyFileGeneration() throws Exception {
    ScaffoldingResult result = simpleGenerationWithResource("multiple-post-body-combination");
    assertEquals(4, result.getGeneratedResources().size());

    String expectedJsonResponse = "{\"message\":\"The response is json\"}";
    String expectedXmlResponse = "<test>The response is xml</test>";
    ScaffolderResource jsonResponseResource = getResourceByName(result.getGeneratedResources(),
                                                                "post_200_albums_application_json.json");
    ScaffolderResource xmlResponseResource = getResourceByName(result.getGeneratedResources(), "post_200_albums_text_xml.xml");

    assertEquals(expectedJsonResponse, IOUtils.toString(jsonResponseResource.getContent()));
    assertEquals(expectedXmlResponse, IOUtils.toString(xmlResponseResource.getContent()));
  }

  private ScaffolderResource getResourceByName(List<ScaffolderResource> resources, String name) {
    return resources.stream().filter(resource -> resource.getName().equals(name)).findFirst().orElse(null);
  }

}
