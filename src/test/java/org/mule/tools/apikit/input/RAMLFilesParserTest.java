/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.junit.Test;

import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.output.GenerationModel;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RAMLFilesParserTest {

  private ParserService parserService = new ParserService();

  @Test
  public void testCreation() {

    final URL resourceUrl =
        RAMLFilesParserTest.class.getClassLoader().getResource("scaffolder/simple.raml");

    assertNotNull(resourceUrl);

    ApiReference apiRef = ApiReference.create(resourceUrl.toString());

    ParseResult parseResult = parserService.parse(apiRef);
    assertTrue(parseResult.success());

    RAMLFilesParser ramlFilesParser = new RAMLFilesParser(new APIFactory(Collections.emptyList()), parseResult.get());

    Map<ResourceActionMimeTypeTriplet, GenerationModel> entries = ramlFilesParser.getEntries();
    assertNotNull(entries);
    assertEquals(5, entries.size());
    Set<ResourceActionMimeTypeTriplet> ramlEntries = entries.keySet();
    ResourceActionMimeTypeTriplet triplet = (ResourceActionMimeTypeTriplet) CollectionUtils.find(ramlEntries, new Predicate() {

      @Override
      public boolean evaluate(Object property) {
        ResourceActionMimeTypeTriplet triplet = ((ResourceActionMimeTypeTriplet) property);
        return "/api/".equals(triplet.getUri()) && "GET".equals(triplet.getVerb()) && "/api".equals(triplet.getApi().getPath());
      }
    });
    assertEquals("0.0.0.0", triplet.getApi().getHttpListenerConfig().getHost());
    assertEquals("8081", triplet.getApi().getHttpListenerConfig().getPort());
    assertEquals("/", triplet.getApi().getHttpListenerConfig().getBasePath());
    assertEquals("simple-httpListenerConfig", triplet.getApi().getHttpListenerConfig().getName());
    ResourceActionMimeTypeTriplet triplet2 = (ResourceActionMimeTypeTriplet) CollectionUtils.find(ramlEntries, property -> {
      ResourceActionMimeTypeTriplet triplet1 = ((ResourceActionMimeTypeTriplet) property);
      return "/api/pet".equals(triplet1.getUri()) && "GET".equals(triplet1.getVerb())
          && "/api".equals(triplet1.getApi().getPath());
    });
    assertEquals("0.0.0.0", triplet2.getApi().getHttpListenerConfig().getHost());
    assertEquals("8081", triplet2.getApi().getHttpListenerConfig().getPort());
    assertEquals("/", triplet2.getApi().getHttpListenerConfig().getBasePath());
    assertEquals("simple-httpListenerConfig", triplet2.getApi().getHttpListenerConfig().getName());

  }

  @Test
  public void oasCreation() {

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("oas/OpenAPI-Specification/examples/v2.0/json/src/main/resources/api/petstore.json");

    ApiReference apiReference = ApiReference.create(url.toString());
    ParseResult parseResult = parserService.parse(apiReference);

    assertTrue(parseResult.success());

    RAMLFilesParser ramlFilesParser = new RAMLFilesParser(new APIFactory(Collections.emptyList()), parseResult.get());
    Map<ResourceActionMimeTypeTriplet, GenerationModel> entries = ramlFilesParser.getEntries();

    assertNotNull(entries);
    assertEquals(3, entries.size());
    Set<ResourceActionMimeTypeTriplet> ramlEntries = entries.keySet();
    ResourceActionMimeTypeTriplet triplet = (ResourceActionMimeTypeTriplet) CollectionUtils.find(ramlEntries, property -> {
      ResourceActionMimeTypeTriplet triplet1 = ((ResourceActionMimeTypeTriplet) property);
      return "/api/pets".equals(triplet1.getUri()) && "GET".equals(triplet1.getVerb())
          && "/api".equals(triplet1.getApi().getPath());
    });
    assertEquals("0.0.0.0", triplet.getApi().getHttpListenerConfig().getHost());
    assertEquals("8081", triplet.getApi().getHttpListenerConfig().getPort());
    assertEquals("/", triplet.getApi().getHttpListenerConfig().getBasePath());
    assertEquals("petstore-httpListenerConfig", triplet.getApi().getHttpListenerConfig().getName());
    ResourceActionMimeTypeTriplet triplet2 = (ResourceActionMimeTypeTriplet) CollectionUtils.find(ramlEntries, property -> {
      ResourceActionMimeTypeTriplet triplet12 = ((ResourceActionMimeTypeTriplet) property);
      return "/api/pets".equals(triplet12.getUri()) && "GET".equals(triplet12.getVerb())
          && "/api".equals(triplet12.getApi().getPath());
    });
    assertEquals("0.0.0.0", triplet2.getApi().getHttpListenerConfig().getHost());
    assertEquals("8081", triplet2.getApi().getHttpListenerConfig().getPort());
    assertEquals("/", triplet2.getApi().getHttpListenerConfig().getBasePath());
    assertEquals("petstore-httpListenerConfig", triplet2.getApi().getHttpListenerConfig().getName());
  }
}
