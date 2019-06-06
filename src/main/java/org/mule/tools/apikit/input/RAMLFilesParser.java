/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.Resource;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.output.GenerationModel;

public class RAMLFilesParser {

  public static final String MULE_APIKIT_PARSER = "mule.apikit.parser";
  private final APIFactory apiFactory;
  private final Set<ApikitMainFlowContainer> apis = new HashSet<>();
  private Map<ResourceActionMimeTypeTriplet, GenerationModel> entries = new HashMap<>();

  public RAMLFilesParser(APIFactory apiFactory, ApiSpecification apiSpec) {
    this.apiFactory = apiFactory;
    collectResources(apiSpec.getLocation(), apiSpec.getResources(), ApikitMainFlowContainer.DEFAULT_BASE_URI, apiSpec.getVersion());
  }

  public Set<ApikitMainFlowContainer> getApis() {
    return apis;
  }

  public List<ApikitMainFlowContainer> getApisAsList(){
    return Lists.newArrayList(apis);
  }

  public Map<ResourceActionMimeTypeTriplet, GenerationModel> getEntries() {
    return entries;
  }

  private void collectResources(String filePath, Map<String, Resource> resourceMap, String baseUri, String version) {
    ApikitMainFlowContainer
        api = apiFactory.createAPIBinding(filePath, baseUri, APIKitTools.getPathFromUri(baseUri, false), null,
                                          null, null);
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

  private void addResource(ApikitMainFlowContainer api, Resource resource, Action action, String mimeType, String version) {

    String completePath = APIKitTools
      .getCompletePathFromBasePathAndPath(api.getHttpListenerConfig().getBasePath(), api.getPath());

    ResourceActionMimeTypeTriplet resourceActionTriplet =
      new ResourceActionMimeTypeTriplet(api, completePath + resource.getResolvedUri(version),
                                        action.getType().toString(),
                                        mimeType);
    entries.put(resourceActionTriplet, new GenerationModel(api, version, resource, action, mimeType));
  }
}
