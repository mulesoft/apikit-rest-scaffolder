/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.apikit.model.ActionType.DELETE;
import static org.mule.apikit.model.ActionType.GET;
import static org.mule.apikit.model.ActionType.OPTIONS;
import static org.mule.apikit.model.ActionType.POST;
import static org.mule.apikit.model.ActionType.PUT;

import org.mule.apikit.model.Action;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.Response;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.output.GenerationModel;

import java.util.HashMap;

import org.junit.Test;

public class GenerationModelTest {

  public static final String VERSION = "v1";

  @Test
  public void testGetVerb() throws Exception {
    Action action = mock(Action.class);
    when(action.getType()).thenReturn(GET);
    Resource resource = mock(Resource.class);
    when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
    ApikitMainFlowContainer api = mock(ApikitMainFlowContainer.class);
    assertEquals("GET", new GenerationModel(api, VERSION, resource, action).getVerb());
  }

  @Test
  public void testGetStringFromActionType() throws Exception {
    Resource resource = mock(Resource.class);
    when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
    ApikitMainFlowContainer api = mock(ApikitMainFlowContainer.class);

    Action action = mock(Action.class);
    when(action.getType()).thenReturn(GET);
    assertEquals("retrieve", new GenerationModel(api, VERSION, resource, action).getStringFromActionType());

    action = mock(Action.class);
    when(action.getType()).thenReturn(PUT);
    assertEquals("create", new GenerationModel(api, VERSION, resource, action).getStringFromActionType());

    action = mock(Action.class);
    when(action.getType()).thenReturn(POST);
    assertEquals("update", new GenerationModel(api, VERSION, resource, action).getStringFromActionType());

    action = mock(Action.class);
    when(action.getType()).thenReturn(DELETE);
    assertEquals("delete", new GenerationModel(api, VERSION, resource, action).getStringFromActionType());

    action = mock(Action.class);
    when(action.getType()).thenReturn(OPTIONS);
    assertEquals("options", new GenerationModel(api, VERSION, resource, action).getStringFromActionType());
  }

  @Test
  public void testGetExample() throws Exception {
    Action action = mock(Action.class);
    HashMap<String, Response> stringResponseHashMap = new HashMap<>();
    Response response = mock(Response.class);
    final HashMap<String, String> examples = new HashMap<>();
    examples.put("application/json", "{\n\"hello\": \">world<\"\n}");
    when(response.getExamples()).thenReturn(examples);
    HashMap<String, MimeType> stringMimeTypeHashMap = new HashMap<>();
    MimeType mimeType = mock(MimeType.class);
    stringMimeTypeHashMap.put("application/json", mimeType);
    when(response.getBody()).thenReturn(stringMimeTypeHashMap);
    stringResponseHashMap.put("200", response);
    when(action.getResponses()).thenReturn(stringResponseHashMap);
    when(action.getType()).thenReturn(GET);
    Resource resource = mock(Resource.class);
    when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
    ApikitMainFlowContainer api = mock(ApikitMainFlowContainer.class);
    assertEquals("{\n\"hello\": \">world<\"\n}",
                 new GenerationModel(api, VERSION, resource, action).getExampleWrapper());
  }

  @Test
  public void testGetExample200Complex() throws Exception {
    Action action = mock(Action.class);
    HashMap<String, Response> stringResponseHashMap = new HashMap<>();
    Response response = mock(Response.class);
    final HashMap<String, String> examples = new HashMap<>();
    examples.put("application/xml", "<hello>world</hello>");
    when(response.getExamples()).thenReturn(examples);
    HashMap<String, MimeType> stringMimeTypeHashMap = new HashMap<>();
    MimeType mimeType = mock(MimeType.class);
    stringMimeTypeHashMap.put("application/xml", mimeType);
    when(response.getBody()).thenReturn(stringMimeTypeHashMap);
    stringResponseHashMap.put("200", response);
    when(action.getResponses()).thenReturn(stringResponseHashMap);
    when(action.getType()).thenReturn(GET);
    Resource resource = mock(Resource.class);
    when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
    ApikitMainFlowContainer api = mock(ApikitMainFlowContainer.class);
    assertEquals("<hello>world</hello>",
                 new GenerationModel(api, VERSION, resource, action).getExampleWrapper());
  }

  @Test
  public void testGetExampleComplex() throws Exception {
    Action action = mock(Action.class);
    HashMap<String, Response> stringResponseHashMap = new HashMap<>();
    Response response = mock(Response.class);
    final HashMap<String, String> examples = new HashMap<>();
    examples.put("application/xml", "<hello>world</hello>");
    when(response.getExamples()).thenReturn(examples);
    HashMap<String, MimeType> stringMimeTypeHashMap = new HashMap<>();
    MimeType mimeType = mock(MimeType.class);
    stringMimeTypeHashMap.put("application/xml", mimeType);
    when(response.getBody()).thenReturn(stringMimeTypeHashMap);
    stringResponseHashMap.put("403", response);
    when(action.getResponses()).thenReturn(stringResponseHashMap);
    when(action.getType()).thenReturn(GET);
    Resource resource = mock(Resource.class);
    when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
    ApikitMainFlowContainer api = mock(ApikitMainFlowContainer.class);
    assertEquals("<hello>world</hello>",
                 new GenerationModel(api, VERSION, resource, action).getExampleWrapper());
  }

  @Test
  public void testGetExampleNull() throws Exception {
    Action action = mock(Action.class);
    when(action.getType()).thenReturn(GET);
    Resource resource = mock(Resource.class);
    when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
    ApikitMainFlowContainer api = mock(ApikitMainFlowContainer.class);
    assertEquals(null, new GenerationModel(api, VERSION, resource, action).getExampleWrapper());
  }

  @Test
  public void testGetMadeUpName() throws Exception {
    Action action = mock(Action.class);
    when(action.getType()).thenReturn(GET);
    Resource resource = mock(Resource.class);
    when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
    ApikitMainFlowContainer api = mock(ApikitMainFlowContainer.class);
    assertEquals("retrievePet", new GenerationModel(api, VERSION, resource, action).getName());
  }

  @Test
  public void testGetRealName() throws Exception {
    Action action = mock(Action.class);
    when(action.getType()).thenReturn(GET);
    Resource resource = mock(Resource.class);
    when(resource.getDisplayName()).thenReturn("Animal");
    when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
    ApikitMainFlowContainer api = mock(ApikitMainFlowContainer.class);
    assertEquals("retrieveAnimal", new GenerationModel(api, VERSION, resource, action).getName());
  }

  @Test
  public void testGetMadeUpNameWithMimeTypes() throws Exception {
    Action action = mock(Action.class);
    when(action.getType()).thenReturn(POST);
    Resource resource = mock(Resource.class);
    when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
    ApikitMainFlowContainer api = mock(ApikitMainFlowContainer.class);
    GenerationModel model1 = new GenerationModel(api, VERSION, resource, action, "text/xml");
    GenerationModel model2 = new GenerationModel(api, VERSION, resource, action, "application/json");
    assertTrue(model1.compareTo(model2) != 0);
    assertEquals("updatePetTextXml", model1.getName());
    assertEquals("updatePetApplicationJson", model2.getName());
  }

  @Test
  public void testGetRelativeURI() throws Exception {
    Action action = mock(Action.class);
    when(action.getType()).thenReturn(GET);
    Resource resource = mock(Resource.class);
    when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
    ApikitMainFlowContainer api = mock(ApikitMainFlowContainer.class);
    assertEquals("/pet", new GenerationModel(api, VERSION, resource, action).getRelativeURI());
  }
}
