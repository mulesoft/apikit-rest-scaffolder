/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.junit.Ignore;
import org.junit.Test;

import org.mule.parser.service.result.ParsingIssue;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.output.GenerationModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mule.tools.apikit.model.Status.FAILED;

public class RAMLFilesParserTest {

  @Test
  public void testCreation() {

    final URL resourceUrl =
      RAMLFilesParserTest.class.getClassLoader().getResource("scaffolder/simple.raml");

    assertNotNull(resourceUrl);

    final Map<File, InputStream> streams = urlToMapStream(resourceUrl);

    RAMLFilesParser ramlFilesParser = RAMLFilesParser.create(streams, new APIFactory());

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
  @Ignore
  public void apiWithWarningsShouldBeValid() {

    final URL resourceUrl =
      RAMLFilesParserTest.class.getClassLoader().getResource("scaffolder/apiWithWarnings.raml");

    assertNotNull(resourceUrl);

    InputStream resourceAsStream;
    try {
      resourceAsStream = resourceUrl.openStream();
    } catch (IOException e) {
      resourceAsStream = null;
    }

    final HashMap<File, InputStream> streams = new HashMap<>();
    streams.put(new File(resourceUrl.getFile()), resourceAsStream);

    RAMLFilesParser ramlFilesParser = RAMLFilesParser.create(streams, new APIFactory());

    Map<ResourceActionMimeTypeTriplet, GenerationModel> entries = ramlFilesParser.getEntries();
    assertNotNull(entries);
    assertEquals(1, entries.size());
  }


  @Test
  public void oasCreation() {

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("oas/OpenAPI-Specification/examples/v2.0/json/src/main/resources/api/petstore.json");
    Map<File, InputStream> streams = urlToMapStream(url);
    RAMLFilesParser ramlFilesParser = RAMLFilesParser.create(streams, new APIFactory());
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

  @Test
  public void invalidRAML() {
    final URL resourceUrl = RAMLFilesParserTest.class.getClassLoader().getResource("parser/failing-api.raml");
    final Map<File, InputStream> streams = urlToMapStream(resourceUrl);
    RAMLFilesParser ramlFilesParser = RAMLFilesParser.create(streams, new APIFactory());

    assertThat(ramlFilesParser.getParseStatus(), is(FAILED));
    assertThat(ramlFilesParser.getParsingErrors().size(), is(2));
    ParsingIssue amfError = ramlFilesParser.getParsingErrors().get(0);
    ParsingIssue ramlError = ramlFilesParser.getParsingErrors().get(1);

    assertThat(amfError.cause(), containsString("AMF parsing failed, fallback into RAML parser"));
    assertThat(ramlError.cause(), containsString("Invalid reference 'SomeTypo' --"));

  }

  private static Map<File, InputStream> urlToMapStream(final URL url) {
    InputStream resourceAsStream;
    try {
      resourceAsStream = url.openStream();
    } catch (IOException e) {
      resourceAsStream = null;
    }

    final Map<File, InputStream> map = new HashMap<>();
    map.put(new File(url.getFile()), resourceAsStream);

    return map;
  }


}
