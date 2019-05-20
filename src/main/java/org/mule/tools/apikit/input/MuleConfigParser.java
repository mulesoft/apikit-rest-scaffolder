/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.mule.tools.apikit.input.parsers.APIKitConfigParser;
import org.mule.tools.apikit.input.parsers.APIKitFlowsParser;
import org.mule.tools.apikit.input.parsers.APIKitRoutersParser;
import org.mule.tools.apikit.input.parsers.HttpListener4xConfigParser;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MuleConfigParser {

  private Set<ResourceActionMimeTypeTriplet> entries = new HashSet<>();
  private Map<String, API> includedApis = new HashMap<>();
  private Map<String, APIKitConfig> apikitConfigs = new HashMap<>();
  private final APIFactory apiFactory;

  public MuleConfigParser(APIFactory apiFactory) {
    this.apiFactory = apiFactory;
  }

  public MuleConfigParser parse(String apiLocation, List<InputStream> streams) {
    List<Document> configurations = createListDocument(streams);
    File apiFile = new File(apiLocation);

    for (Document doc : configurations) {
      parseConfigs(doc);
    }

    for (Document doc : configurations) {
      parseApis(apiFile, doc, apiLocation);
    }

    parseFlows(configurations);
    return this;
  }

  private List<Document> createListDocument(List<InputStream> streams) {
    List<Document> result = new ArrayList<>();

    SAXBuilder saxBuilder = new SAXBuilder(XMLReaders.NONVALIDATING);
    for (InputStream stream : streams) {
      try {
        Document doc = saxBuilder.build(stream);
        stream.close();
        result.add(doc);
      } catch (Exception e) {
        // TODO do something
      }
    }
    return result;
  }

  protected void parseConfigs(Document document) {
    apikitConfigs.putAll(new APIKitConfigParser().parse(document));
    apiFactory.getHttpListenerConfigs().addAll(new HttpListener4xConfigParser().parse(document));
  }

  protected void parseApis(File apiFile, Document document, String apiFilePath) {
    includedApis
        .putAll(new APIKitRoutersParser(apikitConfigs, apiFactory.getHttpListenerConfigs(), apiFilePath, apiFile, apiFactory)
            .parse(document));
  }

  protected void parseFlows(Collection<Document> documents) {
    for (Document document : documents) {
      try {
        entries.addAll(new APIKitFlowsParser(includedApis).parse(document));
      } catch (Exception e) {
        // TODO do something
      }
    }
  }

  public Map<String, APIKitConfig> getApikitConfigs() {
    return apikitConfigs;
  }

  public Set<ResourceActionMimeTypeTriplet> getEntries() {
    return entries;
  }

  public Set<API> getIncludedApis() {
    return new HashSet<>(includedApis.values());
  }
}
