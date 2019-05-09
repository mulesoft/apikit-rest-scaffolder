/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import java.util.HashSet;
import java.util.Set;


import org.mule.parser.service.result.ParseResult;
import org.mule.parser.service.result.ParsingIssue;
import org.mule.parser.service.ParserService;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.api.ApiReference;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.model.ScaffolderResourceLoader;
import org.mule.tools.apikit.model.Status;
import org.mule.tools.apikit.output.GenerationModel;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.mule.tools.apikit.model.Status.FAILED;
import static org.mule.tools.apikit.model.Status.SUCCESS;
import static org.mule.tools.apikit.model.Status.SUCCESS_WITH_ERRORS;

public class RAMLFilesParser {

  public static final String MULE_APIKIT_PARSER = "mule.apikit.parser";
  private String vendorId = "RAML";

  private final APIFactory apiFactory;
  private final Status parseStatus;
  private final List<ParsingIssue> parsingErrors = new ArrayList<>();
  private final Set<API> apis = new HashSet<>();

  private Map<ResourceActionMimeTypeTriplet, GenerationModel> entries = new HashMap<>();
  private String ramlVersion;

  private RAMLFilesParser(List<ApiReference> specs, APIFactory apiFactory, ScaffolderResourceLoader scaffolderResourceLoader) {
    this.apiFactory = apiFactory;
    List<ApiReference> processedFiles = new ArrayList<>();
    for (ApiReference spec : specs) {
        ParseResult parseResult = parseApi(spec, scaffolderResourceLoader);
        if (parseResult.success()) {
          final ApiSpecification api = parseResult.get();
          vendorId = api.getApiVendor().toString();
          ramlVersion = api.getVersion();
          collectResources(spec.getLocation(), api.getResources(), API.DEFAULT_BASE_URI, api.getVersion());
          processedFiles.add(spec);
        } else {
          parsingErrors.addAll(parseResult.getWarnings());
          parsingErrors.addAll(parseResult.getErrors());
        }
    }
    if (processedFiles.size() > 0) {
      parseStatus = parsingErrors.size() == 0 ? SUCCESS : SUCCESS_WITH_ERRORS;
    } else {
      parseStatus = FAILED;
    }
  }

  public static RAMLFilesParser create(Map<File, InputStream> fileStreams, APIFactory apiFactory) {
    final List<ApiReference> specs = fileStreams.entrySet().stream()
      .map(e -> ApiReference.create(e.getKey().getAbsolutePath()))
      .collect(toList());

    return new RAMLFilesParser(specs, apiFactory, null);
  }

  public static RAMLFilesParser create(Map<String, InputStream> apis, APIFactory apiFactory,
                                       ScaffolderResourceLoader scaffolderResourceLoader) {
    final List<ApiReference> specs = apis.entrySet().stream()
      .map(e -> ApiReference.create(e.getKey(), scaffolderResourceLoader))
      .collect(toList());

    return new RAMLFilesParser(specs, apiFactory, scaffolderResourceLoader);
  }

  public Status getParseStatus() {
    return parseStatus;
  }

  public String getVendorId() {
    return vendorId;
  }

  public String getRamlVersion() {
    return ramlVersion;
  }

  public List<ParsingIssue> getParsingErrors() {
    return parsingErrors;
  }

  public Set<API> getApis() {
    return apis;
  }

  private void collectResources(String filePath, Map<String, Resource> resourceMap, String baseUri, String version) {
    API api = apiFactory.createAPIBinding(filePath, null, baseUri, APIKitTools.getPathFromUri(baseUri, false), null,
                                          null);
    apis.add(api);
    for (Resource resource : resourceMap.values()) {
      for (Action action : resource.getActions().values()) {

        Map<String, MimeType> mimeTypes = action.getBody();
        boolean addGenericAction = false;
        if (mimeTypes != null && !mimeTypes.isEmpty()) {
          for (MimeType mimeType : mimeTypes.values()) {
            if (mimeType.getSchema() != null
              || (mimeType.getFormParameters() != null && !mimeType.getFormParameters().isEmpty())) {
              addResource(api, resource, action, mimeType.getType(), version);
            } else {
              addGenericAction = true;
            }
          }
        } else {
          addGenericAction = true;
        }

        if (addGenericAction) {
          addResource(api, resource, action, null, version);
        }
      }

      collectResources(filePath, resource.getResources(), baseUri, version);
    }
  }

  private void addResource(API api, Resource resource, Action action, String mimeType, String version) {

    String completePath = APIKitTools
      .getCompletePathFromBasePathAndPath(api.getHttpListenerConfig().getBasePath(), api.getPath());

    ResourceActionMimeTypeTriplet resourceActionTriplet =
      new ResourceActionMimeTypeTriplet(api, completePath + resource.getResolvedUri(version),
                                        action.getType().toString(),
                                        mimeType);
    entries.put(resourceActionTriplet, new GenerationModel(api, version, resource, action, mimeType));
  }

  public Map<ResourceActionMimeTypeTriplet, GenerationModel> getEntries() {
    return entries;
  }

  private ParseResult parseApi(ApiReference apiRef, ScaffolderResourceLoader scaffolderResourceLoader) {
    ParserService parserService = new ParserService();
    return parserService.parse(ApiReference.create(apiRef.getLocation(), scaffolderResourceLoader));
  }
}
