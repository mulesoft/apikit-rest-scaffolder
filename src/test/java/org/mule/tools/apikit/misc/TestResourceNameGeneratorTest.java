/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.misc;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.model.Resource;
import org.mule.tools.apikit.output.GenerationModel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestResourceNameGeneratorTest {

  private GenerationModel flowEntryMock;
  private Resource resourceMock;

  @Before
  public void setUp() {
    this.flowEntryMock = mock(GenerationModel.class);
    this.resourceMock = mock(Resource.class);
    when(flowEntryMock.getResource()).thenReturn(resourceMock);
  }

  @Test
  public void testSimpleName() {
    when(resourceMock.getUri()).thenReturn("/albums");
    when(flowEntryMock.getVerb()).thenReturn("get");
    assertEquals("get_albums_text_xml.xml", TestResourceNameGenerator.generate(flowEntryMock, "text/xml", StringUtils.EMPTY));
  }

  @Test
  public void testVerbNameToLowerCase() {
    when(resourceMock.getUri()).thenReturn("/albums");
    when(flowEntryMock.getVerb()).thenReturn("POST");
    assertEquals("post_albums_application_json.json",
                 TestResourceNameGenerator.generate(flowEntryMock, "application/json", StringUtils.EMPTY));
  }

  @Test
  public void testUriNameWithParentUri() {
    when(resourceMock.getUri()).thenReturn("/albums/{albumId}");
    when(flowEntryMock.getVerb()).thenReturn("POST");
    assertEquals("post_albums_{albumid}_application_json.json",
                 TestResourceNameGenerator.generate(flowEntryMock, "application/json", StringUtils.EMPTY));
  }

  @Test
  public void testStatusCode() {
    when(resourceMock.getUri()).thenReturn("/albums/{albumId}");
    when(flowEntryMock.getVerb()).thenReturn("GET");
    assertEquals("get_200_albums_{albumid}_application_json.json",
                 TestResourceNameGenerator.generate(flowEntryMock, "application/json", "200"));
  }

  @Test
  public void getExtensionJson() {
    assertEquals(".json", TestResourceNameGenerator.getExtension("application/json"));
    assertEquals(".json", TestResourceNameGenerator.getExtension("text/json"));
  }

  @Test
  public void getExtensionXml() {
    assertEquals(".xml", TestResourceNameGenerator.getExtension("application/xml"));
    assertEquals(".xml", TestResourceNameGenerator.getExtension("text/xml"));
  }

  @Test
  public void getExtensionTxt() {
    assertEquals(".txt", TestResourceNameGenerator.getExtension("text/plain"));
  }
}
